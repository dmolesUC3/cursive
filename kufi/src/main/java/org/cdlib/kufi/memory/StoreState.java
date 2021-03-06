package org.cdlib.kufi.memory;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;
import io.vavr.Tuple;
import io.vavr.collection.*;
import io.vavr.control.Option;
import org.cdlib.kufi.*;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.function.Function;

import static org.cdlib.kufi.LinkType.CHILD_OF;
import static org.cdlib.kufi.LinkType.PARENT_OF;
import static org.cdlib.kufi.ResourceType.WORKSPACE;
import static org.cdlib.kufi.Transaction.initTransaction;

class StoreState {

  // ------------------------------------------------------------
  // Class fields

  /**
   * Separate SecureRandom instance per thread to avoid contention
   */
  private static final ThreadLocal<NoArgGenerator> generator = ThreadLocal.withInitial(
    () -> Generators.randomBasedGenerator(new SecureRandom())
  );

  // ------------------------------------------------------------
  // Instance fields

  private final Transaction tx;

  private final Map<UUID, MemoryResource<?>> resources;

  private final Multimap<UUID, MemoryLink> linksBySource;
  private final Multimap<UUID, MemoryLink> linksByTarget;

  // ------------------------------------------------------------
  // Constructors

  StoreState() {
    this(initTransaction(), HashMap.empty(), HashMultimap.withSet().empty(), HashMultimap.withSet().empty());
  }

  private StoreState(Transaction tx, Map<UUID, MemoryResource<?>> resources, Multimap<UUID, MemoryLink> linksBySource, Multimap<UUID, MemoryLink> linksByTarget) {
    this.tx = tx;
    this.resources = resources;
    this.linksBySource = linksBySource;
    this.linksByTarget = linksByTarget;
  }

  // ------------------------------------------------------------
  // Package-private instance methods

  Transaction transaction() {
    return tx;
  }

  // ------------------------------
  // Finders

  Option<MemoryResource<?>> find(UUID id) {
    return resources.get(id).filter(Resource::isLive);
  }

  Option<MemoryResource<?>> findTombstone(UUID id) {
    return resources.get(id).filter(Resource::isDeleted);
  }

  <R extends Resource<R>> Set<R> findChildrenOfType(UUID id, ResourceType<R> type) {
    return findChildren(id)
      .flatMap(r -> r.as(type))
      .toSet();
  }

  Option<Resource<?>> findParent(Resource<?> child) {
    return Option.narrow(linksBySource(child.id())
      .filter(l -> l.type() == CHILD_OF)
      .filter(Link::isLive)
      .map(Link::target)
      .headOption());
  }

  // ------------------------------
  // Creators & Deletors

  StoreUpdate<Workspace> createWorkspace(MemoryStore store) {
    var id = newId();
    var txNext = tx.next();
    var ws = store.createNew(WORKSPACE, id, txNext);
    var lrNext = resources.put(id, ws);

    var storeNext = new StoreState(txNext, lrNext, linksBySource, linksByTarget);
    return StoreUpdate.of(ws, storeNext);
  }

  <P extends Resource<P>, C extends Resource<C>> StoreUpdate<C> createChild(MemoryStore store, MemoryResource<P> parent, ResourceType<C> childType) {
    var parentId = parent.id();
    var parentCurrent = current(parent);

    var childId = newId();
    var txNext = tx.next();

    var child = store.createNew(childType, childId, txNext);
    var parentNext = store.nextVersion(parentCurrent, txNext);

    var p2c = MemoryLink.create(parentNext, PARENT_OF, child, txNext);
    var c2p = MemoryLink.create(child, CHILD_OF, parentNext, txNext);

    var lrNext = resources
      .put(parentId, parentNext)
      .put(childId, child);

    var lbsNext = linksBySource
      .put(parentId, p2c)
      .put(childId, c2p);

    var lbtNext = linksByTarget
      .put(childId, p2c)
      .put(parentId, c2p);

    var stateNext = new StoreState(txNext, lrNext, lbsNext, lbtNext);
    return StoreUpdate.of(child, stateNext);
  }

  <R extends Resource<R>> StoreUpdate<R> delete(MemoryResource<R> r, boolean recursive) {
    if (!recursive) {
      var childCount = countChildren(r.id());
      if (childCount > 0) {
        throw new IllegalStateException("Can't delete " + r + "; " + childCount + " children");
      }
    }
    var txNext = tx.next();
    return StoreUpdate.of(r.store().delete(r, txNext), deleteRecursive(r, txNext));
  }

  Traversable<MemoryLink> linksBySource(UUID id) {
    return linksBySource.getOrElse(id, HashSet.empty());
  }

  Traversable<MemoryLink> linksByTarget(UUID id) {
    return linksByTarget.getOrElse(id, HashSet.empty());
  }

  // ------------------------------------------------------------
  // Private instance methods

  private Traversable<MemoryResource<?>> findChildren(UUID id) {
    return linksBySource(id)
      .filter(l -> l.type() == PARENT_OF)
      .filter(Link::isLive)
      .map(MemoryLink::target);
  }

  /**
   * Recursively delete the specified resource and its children without incrementing
   * the transaction.
   *
   * @param r The resource to delete.
   * @param txNext The final transaction.
   * @return The final state.
   */
  private StoreState deleteRecursive(MemoryResource<?> r, Transaction txNext) {
    var current = current(r);
    var id = current.id();
    var tombstone = current.store().delete(current, txNext);

    var liveBySource = linksBySource(id).filter(Link::isLive);
    var liveByTarget = linksByTarget(id).filter(Link::isLive);

    var rsNext = resources.put(id, tombstone);

    var l2dBySource = liveBySource.map(l -> Tuple.of(l, l.deleted(tombstone, l.target().store().nextVersion(l.target(), txNext), txNext)));
    var l2dByTarget = liveByTarget.map(l -> Tuple.of(l, l.deleted(l.source().store().nextVersion(l.source(), txNext), tombstone, txNext)));
    var liveToDead = List.of(l2dBySource, l2dByTarget).flatMap(Function.identity());

    var lbsNext = liveToDead.foldLeft(linksBySource, (lbs, t) -> lbs.replace(t._1.sourceId(), t._1, t._2));
    var lbtNext = liveToDead.foldLeft(linksByTarget, (lbt, t) -> lbt.replace(t._1.targetId(), t._1, t._2));

    var stateNext = new StoreState(txNext, rsNext, lbsNext, lbtNext);

    var children = liveBySource.filter(l -> l.type() == PARENT_OF).map(MemoryLink::target);
    return children.foldLeft(stateNext, (storeState, r1) -> storeState.deleteRecursive(r1, txNext));
  }

  private MemoryResource<?> current(MemoryResource<?> resource) {
    return resources.get(resource.id()).filter(r -> r.hasType(resource.type()))
      .getOrElseThrow(() ->
        new ResourceNotFoundException(resource.id(), resource.type())
      );
  }

  private int countChildren(UUID id) {
    return findChildren(id).size();
  }

  // ------------------------------------------------------------
  // Private class methods

  private static UUID newId() {
    return generator.get().generate();
  }

}

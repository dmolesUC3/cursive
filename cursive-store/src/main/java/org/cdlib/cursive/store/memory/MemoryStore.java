package org.cdlib.cursive.store.memory;

import io.vavr.Lazy;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import io.vavr.collection.Traversable;
import io.vavr.collection.Vector;
import io.vavr.control.Option;
import org.cdlib.cursive.core.Resource;
import org.cdlib.cursive.core.Store;
import org.cdlib.cursive.core.Workspace;
import org.cdlib.cursive.pcdm.PcdmCollection;
import org.cdlib.cursive.pcdm.PcdmFile;
import org.cdlib.cursive.pcdm.PcdmObject;
import org.cdlib.cursive.pcdm.PcdmRelation;
import org.cdlib.cursive.store.Identifiers;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

// TODO: compare performance with mutable fastutil collections
public class MemoryStore implements Store {

  // ------------------------------------------------------------
  // Data

  private final AtomicReference<Vector<Workspace>> workspaces = new AtomicReference<>(Vector.empty());
  private final AtomicReference<Vector<PcdmCollection>> collections = new AtomicReference<>(Vector.empty());
  private final AtomicReference<Vector<PcdmObject>> objects = new AtomicReference<>(Vector.empty());
  private final AtomicReference<Vector<PcdmFile>> files = new AtomicReference<>(Vector.empty());
  private final AtomicReference<Vector<PcdmRelation>> relations = new AtomicReference<>(Vector.empty());

  private final AtomicReference<Map<UUID, Resource>> identifiers = new AtomicReference<>(HashMap.empty());

  // ------------------------------------------------------------
  // Store

  private UUID mintIdentifier() {
    return Identifiers.mintIdentifier();
  }

  private <T extends Resource> void register(AtomicReference<Vector<T>> registry, Lazy<T> lazyValue) {
    registry.updateAndGet(v -> v.append(lazyValue.get()));
    T value = lazyValue.get();
    UUID identifier = value.id();
    identifiers.updateAndGet(m -> m.put(identifier, value));
  }

  @Override
  public Option<Resource> find(UUID id) {
    return identifiers.get().get(id);
  }

  // --------------------
  // Workspaces

  @Override
  public Traversable<Workspace> workspaces() {
    return workspaces.get();
  }

  @Override
  public Workspace createWorkspace() {
    Lazy<Workspace> newWorkspace = Lazy.of(() -> new MemoryWorkspace(this, mintIdentifier()));
    register(workspaces, newWorkspace);
    return newWorkspace.get();
  }

  // --------------------
  // Collections

  @Override
  public Traversable<PcdmCollection> allCollections() {
    return collections.get();
  }

  @Override
  public PcdmCollection createCollection() {
    Lazy<PcdmCollection> newCollection = Lazy.of(() -> new MemoryCollection(this, mintIdentifier()));
    register(collections, newCollection);
    return newCollection.get();
  }

  PcdmCollection createCollection(MemoryWorkspace parent) {
    Lazy<PcdmCollection> newCollection = Lazy.of(() -> new MemoryCollection(this, mintIdentifier(), parent));
    register(collections, newCollection);
    return newCollection.get();
  }

  PcdmCollection createCollection(MemoryCollection parent) {
    Lazy<PcdmCollection> newCollection = Lazy.of(() -> new MemoryCollection(this, mintIdentifier(), parent));
    register(collections, newCollection);
    return newCollection.get();
  }

  // --------------------
  // Objects

  @Override
  public Traversable<PcdmObject> allObjects() {
    return objects.get();
  }

  @Override
  public PcdmObject createObject() {
    Lazy<PcdmObject> newObject = Lazy.of(() -> new MemoryObject(this, mintIdentifier()));
    register(objects, newObject);
    return newObject.get();
  }

  PcdmObject createObject(MemoryObject parent) {
    Lazy<PcdmObject> newObject = Lazy.of(() -> new MemoryObject(this, mintIdentifier(), parent));
    register(objects, newObject);
    return newObject.get();
  }

  PcdmObject createObject(MemoryCollection parent) {
    Lazy<PcdmObject> newObject = Lazy.of(() -> new MemoryObject(this, mintIdentifier(), parent));
    register(objects, newObject);
    return newObject.get();
  }

  // --------------------
  // Files

  @Override
  public Traversable<PcdmFile> allFiles() {
    return files.get();
  }

  // TODO: create files in objects, replace this with recordFile() or similar
  PcdmFile createFile(MemoryObject parent) {
    Lazy<PcdmFile> newFile = Lazy.of(() -> new MemoryFile(parent, mintIdentifier()));
    register(files, newFile);
    return newFile.get();
  }

  // --------------------
  // Relationships

  @Override
  public Traversable<PcdmRelation> allRelations() {
    return relations.get();
  }

  void recordRelation(MemoryRelation relation) {
    relations.updateAndGet(v -> v.append(relation));
  }

}

package org.cdlib.kufi.memory;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vavr.control.Option;
import org.cdlib.kufi.*;

import java.util.NoSuchElementException;
import java.util.UUID;

import static io.reactivex.Single.just;

public class MemoryStore implements Store {

  // ------------------------------------------------------------
  // Instance fields

  private final Object mutex = new Object();
  private volatile StoreState state = new StoreState();

  // ------------------------------------------------------------
  // Store

  @Override
  public Single<Transaction> transaction() {
    return just(state.transaction());
  }

  @Override
  public Single<Workspace> createWorkspace() {
    synchronized (mutex) {
      try {
        var result = state.createWorkspace(this);
        state = result.stateNext();
        return just(result.resource());
      } catch (Exception e) {
        return Single.error(e);
      }
    }
  }

  @Override
  public Completable deleteWorkspace(Workspace ws, boolean recursive) {
    synchronized (mutex) {
      try {
        if (recursive) {
          state = state.deleteRecursive(ws);
          return Completable.complete();
        } else {
          state = state.delete(ws);
          return Completable.complete();
        }
      } catch (Exception e) {
        return Completable.error(e);
      }
    }
  }

  @Override
  public Single<Collection> createCollection(Workspace parent) {
    synchronized (mutex) {
      try {
        var result = state.createChild(this, parent, MemoryCollection::new, MemoryWorkspace::new);
        state = result.stateNext();
        return just(result.resource());
      } catch (Exception e) {
        return Single.error(e);
      }
    }
  }

  @Override
  public Single<Collection> createCollection(Collection parent) {
    synchronized (mutex) {
      try {
        var result = state.createChild(this, parent, MemoryCollection::new, MemoryCollection::new);
        state = result.stateNext();
        return just(result.resource());
      } catch (Exception e) {
        return Single.error(e);
      }
    }
  }

  @Override
  public Completable deleteCollection(Collection ws, boolean recursive) {
    synchronized (mutex) {
      try {
        if (recursive) {
          state = state.deleteRecursive(ws);
          return Completable.complete();
        } else {
          state = state.delete(ws);
          return Completable.complete();
        }
      } catch (Exception e) {
        return Completable.error(e);
      }
    }
  }

  @Override
  public <R extends Resource<R>> Maybe<R> find(UUID id, ResourceType<R> type) {
    try {
      Option<Resource<?>> r = state.find(id);
      Option<R> r2 = r.flatMap(r1 -> r1.as(type));
      Option<Maybe<R>> m = r2.map(Maybe::just);
      Maybe<R> m1 = m.getOrElse(Maybe::empty);

      return m1;
    } catch (Exception e) {
      return Maybe.error(e);
    }
  }

  // ------------------------------------------------------------
  // Package-private

  <R extends Resource<R>> Observable<R> findChildrenOfType(Resource<?> parent, ResourceType<R> type) {
    var children = state.findChildrenOfType(parent.id(), type);
    return Observable.fromIterable(children);
  }

  Single<? extends Resource<?>> findParentOf(Resource<?> child) {
    return state.findParent(child).map(Single::just)
      .getOrElse(() -> Single.error(new NoSuchElementException("No parent found for resource: " + child)));
  }
}

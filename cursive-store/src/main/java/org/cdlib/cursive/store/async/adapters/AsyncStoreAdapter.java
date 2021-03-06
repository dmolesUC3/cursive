package org.cdlib.cursive.store.async.adapters;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.cdlib.cursive.core.Store;
import org.cdlib.cursive.core.async.AsyncResource;
import org.cdlib.cursive.core.async.AsyncStore;
import org.cdlib.cursive.core.async.AsyncWorkspace;
import org.cdlib.cursive.pcdm.async.AsyncPcdmCollection;
import org.cdlib.cursive.pcdm.async.AsyncPcdmFile;
import org.cdlib.cursive.pcdm.async.AsyncPcdmObject;
import org.cdlib.cursive.pcdm.async.AsyncPcdmRelation;
import org.cdlib.cursive.store.util.RxUtils;

import java.util.Objects;
import java.util.UUID;

// TODO: Figure out where the work should happen for each call, & what combination of observeOn()/subscribeOn() makes sense
public class AsyncStoreAdapter<S extends Store> implements AsyncStore {

  // ------------------------------
  // Fields

  private final S store;

  // ------------------------------
  // Constructors

  public AsyncStoreAdapter(S store) {
    Objects.requireNonNull(store);
    this.store = store;
  }

  // ------------------------------
  // Factory methods

  public static <S extends Store> AsyncStoreAdapter<S> toAsync(S store) {
    return new AsyncStoreAdapter<>(store);
  }

  // ------------------------------
  // AsyncStore

  @Override
  public Observable<AsyncWorkspace> workspaces() {
    return Observable.fromIterable(store.workspaces()).map(AsyncWorkspaceAdapter::new);
  }

  @Override
  public Single<AsyncWorkspace> createWorkspace() {
    return Single.just(store.createWorkspace()).map(AsyncWorkspaceAdapter::new);
  }

  @Override
  public Observable<AsyncPcdmCollection> collections() {
    return Observable.fromIterable(store.allCollections()).map(AsyncPcdmCollectionAdapter::new);
  }

  @Override
  public Single<AsyncPcdmCollection> createCollection() {
    return Single.just(store.createCollection()).map(AsyncPcdmCollectionAdapter::new);
  }

  @Override
  public Observable<AsyncPcdmObject> objects() {
    return Observable.fromIterable(store.allObjects()).map(AsyncPcdmObjectAdapter::new);
  }

  @Override
  public Single<AsyncPcdmObject> createObject() {
    return Single.just(store.createObject()).map(AsyncPcdmObjectAdapter::new);
  }

  @Override
  public Observable<AsyncPcdmFile> files() {
    return Observable.fromIterable(store.allFiles()).map(AsyncPcdmFileAdapter::new);
  }

  @Override
  public Observable<AsyncPcdmRelation> relations() {
    return Observable.fromIterable(store.allRelations()).map(AsyncPcdmRelationAdapter::new);
  }

  @Override
  public Maybe<AsyncResource> find(UUID identifier) {
    return store.find(identifier)
      .map(AsyncResourceImpl::from)
      .map(RxUtils::toMaybe)
      .getOrElse(Maybe.empty());
  }
}

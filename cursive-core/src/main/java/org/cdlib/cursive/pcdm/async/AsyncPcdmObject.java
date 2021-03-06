package org.cdlib.cursive.pcdm.async;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.cdlib.cursive.core.ResourceType;
import org.cdlib.cursive.core.async.AsyncResource;
import org.cdlib.cursive.core.async.AsyncStore;

import java.util.concurrent.atomic.AtomicReference;

public interface AsyncPcdmObject extends AsyncResource {
  Maybe<AsyncPcdmObject> parentObject();

  Maybe<AsyncPcdmCollection> parentCollection();

  @SuppressWarnings("unchecked")
  default Maybe<AsyncResource> parent() {
    Maybe<AsyncPcdmCollection> parentCol = parentCollection();
    Maybe<AsyncPcdmObject> parentObj = parentObject();
    return Maybe.mergeArray(parentCol, parentObj).firstElement();
  }

  Observable<AsyncPcdmFile> memberFiles();

  Single<AsyncPcdmFile> createFile();

  Observable<AsyncPcdmObject> memberObjects();

  Single<AsyncPcdmObject> createObject();

  Observable<AsyncPcdmObject> relatedObjects();

  /**
   * @throws NullPointerException     if {@code toObject} is null
   * @throws IllegalArgumentException if {@code toObject} belongs to
   *                                  a different {@link AsyncStore} than this object
   */
  Single<AsyncPcdmRelation> relateTo(AsyncPcdmObject toObject);

  Observable<AsyncPcdmRelation> outgoingRelations();

  Observable<AsyncPcdmRelation> incomingRelations();

  @Override
  default ResourceType type() {
    return ResourceType.OBJECT;
  }
}

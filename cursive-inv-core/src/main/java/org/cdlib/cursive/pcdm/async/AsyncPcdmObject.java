package org.cdlib.cursive.pcdm.async;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.cdlib.cursive.core.async.AsyncStore;

public interface AsyncPcdmObject extends AsyncPcdmResource {
  Maybe<AsyncPcdmObject> parentObject();
  Maybe<AsyncPcdmCollection> parentCollection();

  Observable<AsyncPcdmFile> memberFiles();
  Single<AsyncPcdmFile> createFile();

  Observable<AsyncPcdmObject> memberObjects();
  Single<AsyncPcdmObject> createObject();

  Observable<AsyncPcdmObject> relatedObjects();

  /**
   * @throws NullPointerException if {@code toObject} is null
   * @throws IllegalArgumentException if {@code toObject} belongs to
   *   a different {@link AsyncStore} than this object
   */
  Single<AsyncPcdmRelation> relateTo(AsyncPcdmObject toObject);

  Observable<AsyncPcdmRelation> outgoingRelations();
  Observable<AsyncPcdmRelation> incomingRelations();
}
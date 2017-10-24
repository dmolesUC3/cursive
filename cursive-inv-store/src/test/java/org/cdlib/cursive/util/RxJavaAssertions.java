package org.cdlib.cursive.util;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.vavr.collection.List;
import org.assertj.core.api.Assertions;

public class RxJavaAssertions extends Assertions {
  public static <T> TestObserverAssert<T> assertThat(TestObserver<T> actual) {
    return TestObserverAssert.assertThat(actual);
  }

  public static <T> T valueEmittedBy(Single<T> observable) {
    TestObserver<T> observer = observable.test();
    assertThat(observer).observedNoErrors();
    assertThat(observer).observedSingleValue();
    return firstValueObservedBy(observer);
  }

  public static <T> List<T> valuesEmittedBy(Observable<T> observable) {
    return valuesObservedBy(observable.test());
  }

  public static <T> List<T> valuesObservedBy(TestObserver<T> observer) {
    assertThat(observer).isNotNull();
    assertThat(observer).observedNoErrors();
    return List.ofAll(observer.values());
  }

  public static <T> T firstValueObservedBy(TestObserver<T> observer) {
    assertThat(observer).isNotNull();
    assertThat(observer).observedNoErrors();
    return valuesObservedBy(observer).head();
  }
}
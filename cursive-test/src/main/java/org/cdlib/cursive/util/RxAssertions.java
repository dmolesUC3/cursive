package org.cdlib.cursive.util;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.vavr.collection.List;
import org.assertj.core.api.Assertions;

public class RxAssertions extends Assertions {
  public static <T> TestObserverAssert<T> assertThat(TestObserver<T> actual) {
    return TestObserverAssert.assertThat(actual);
  }

  public static <T> T valueEmittedBy(Single<T> single) {
    assertThat(single).isNotNull();
    return valueObservedBy(single.test());
  }

  public static <T> T valueEmittedBy(Maybe<T> maybe) {
    assertThat(maybe).isNotNull();
    return valueObservedBy(maybe.test());
  }

  public static Throwable errorEmittedBy(Maybe<?> maybe) {
    assertThat(maybe).isNotNull();
    return errorObservedBy(maybe.test());
  }

  private static Throwable errorObservedBy(TestObserver<?> observer) {
    assertThat(observer).isNotNull();
    assertThat(observer.errorCount()).isEqualTo(1);
    List<Throwable> errors = List.ofAll(observer.errors());
    return errors.head();
  }

  private static <T> T valueObservedBy(TestObserver<T> observer) {
    assertThat(observer).isNotNull();
    assertThat(observer).observedNoErrors();
    assertThat(observer).hasValueCount(1);
    return firstValueObservedBy(observer);
  }

  public static <T> List<T> valuesEmittedBy(Observable<T> observable) {
    return valuesObservedBy(observable.test());
  }

  private static <T> List<T> valuesObservedBy(TestObserver<T> observer) {
    assertThat(observer).isNotNull();
    assertThat(observer).observedNoErrors();
    return List.ofAll(observer.values());
  }

  private static <T> T firstValueObservedBy(TestObserver<T> observer) {
    assertThat(observer).isNotNull();
    assertThat(observer).observedNoErrors();
    return valuesObservedBy(observer).head();
  }
}

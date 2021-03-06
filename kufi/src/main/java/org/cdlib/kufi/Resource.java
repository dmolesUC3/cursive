package org.cdlib.kufi;

import io.vavr.control.Option;

import java.util.UUID;

public interface Resource<R extends Resource<R>> {

  UUID id();

  Version currentVersion();

  Option<Version> deletedAt();

  default Option<Transaction> deletedAtTransaction() {
    return deletedAt().map(Version::transaction);
  }

  boolean isLive();

  ResourceType<R> type();

  <R1 extends Resource<R1>> boolean hasType(ResourceType<R1> type);

  <R1 extends Resource<R1>> Option<R1> as(ResourceType<R1> type);

  boolean isDeleted();

  default boolean isLaterVersionOf(Resource<?> r) {
    return id().equals(r.id()) && currentVersion().greaterThan(r.currentVersion());
  }

  default boolean isEarlierVersionOf(Resource<?> r) {
    return id().equals(r.id()) && currentVersion().lessThan(r.currentVersion());
  }

  @SuppressWarnings("unchecked")
  default R self() {
    return (R) this;
  }
}

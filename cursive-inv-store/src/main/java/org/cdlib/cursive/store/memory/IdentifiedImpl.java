package org.cdlib.cursive.store.memory;

import java.util.Objects;

class IdentifiedImpl {
  private final String identifier;

  IdentifiedImpl(String identifier) {
    Objects.requireNonNull(identifier, () -> String.format("%s must have a Store", getClass().getSimpleName()));
    this.identifier = identifier;
  }

  public String identifier() {
    return identifier;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    IdentifiedImpl that = (IdentifiedImpl) o;
    return identifier.equals(that.identifier);
  }

  @Override
  public int hashCode() {
    return identifier.hashCode();
  }
}

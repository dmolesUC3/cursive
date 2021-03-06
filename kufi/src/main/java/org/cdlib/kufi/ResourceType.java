package org.cdlib.kufi;

import io.vavr.collection.*;

import java.util.Objects;

/**
 * Enumerated list of resource types. Should be an enum, but
 * until we get <a href="http://openjdk.java.net/jeps/301">JEP 301</a>
 * generic enums, a final class is the best we can do.
 *
 * @param <R>
 */
public final class ResourceType<R extends Resource<R>> {

  // ------------------------------------------------------------
  // Constants

  public static final ResourceType<Workspace> WORKSPACE = new ResourceType<>(Workspace.class);
  public static final ResourceType<Collection> COLLECTION = new ResourceType<>(Collection.class);

  private static final Multimap<ResourceType<?>, ResourceType<?>> allowableChildren = HashMultimap.withSet().of(
    WORKSPACE, COLLECTION,
    COLLECTION, COLLECTION
  );

  // ------------------------------------------------------------
  // Fields

  private final Class<R> implType;

  // ------------------------------------------------------------
  // Constructor

  private ResourceType(Class<R> implType) {
    this.implType = Objects.requireNonNull(implType);
    Registry.register(this);
  }

  // ------------------------------------------------------------
  // Class methods

  public static Seq<ResourceType<?>> values() {
    return Registry.values();
  }

  // ------------------------------------------------------------
  // Instance methods

  public R cast(Resource<?> resource) {
    if (!resource.hasType(this)) {
      throw new IllegalArgumentException(String.format("Expected %s, was %s", this, resource.type()));
    }
    return implType.cast(resource);
  }

  public Set<ResourceType<?>> allowableChildren() {
    return allowableChildren.getOrElse(this, HashSet.empty()).toSet();
  }

  public Class<R> implType() {
    return implType;
  }

  // ------------------------------------------------------------
  // Object

  @Override
  public String toString() {
    return implType.getSimpleName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }

    var that = (ResourceType<?>) o;
    return implType.equals(that.implType);
  }

  @Override
  public int hashCode() {
    return implType.hashCode();
  }

  // ------------------------------------------------------------
  // Helper classes

  private static final class Registry {
    private static Map<Class<?>, ResourceType<?>> registry = LinkedHashMap.empty();

    private static void register(ResourceType<?> rt) {
      Class<?> implType = rt.implType;
      // TODO: uncomment this once we can exclude it from test coverage
//      registry.get(implType).forEach(existing -> {
//        var msg = String.format("A %s for %s already exists: %s", ResourceType.class.getSimpleName(), implType.getSimpleName(), existing);
//        throw new IllegalStateException(msg);
//      });
      registry = registry.put(implType, rt);
    }

    private static Seq<ResourceType<?>> values() {
      return registry.values();
    }

    private Registry() {
      // private to prevent instantiation
    }
  }
}

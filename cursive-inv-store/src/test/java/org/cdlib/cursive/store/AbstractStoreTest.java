package org.cdlib.cursive.store;

import io.vavr.collection.Traversable;
import org.cdlib.cursive.core.CFile;
import org.cdlib.cursive.core.CObject;
import org.cdlib.cursive.core.CCollection;
import org.cdlib.cursive.core.CWorkspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.cdlib.cursive.util.VavrAssertions.assertThat;

public abstract class AbstractStoreTest<S extends Store> {

  // ------------------------------------------------------------
  // Fixture

  private S store;

  protected abstract S newStore();

  @BeforeEach
  void setUp() {
    store = newStore();
  }

  // ------------------------------------------------------------
  // Tests

  @Nested
  @SuppressWarnings("unused")
  class Workspaces {
    @Test
    void workspacesEmptyByDefault() {
      Traversable<CWorkspace> workspaces = store.workspaces();
      assertThat(workspaces).isEmpty();
    }

    @Test
    void createWorkspaceCreatesAWorkspace() {
      CWorkspace workspace = store.createWorkspace();
      assertThat(workspace).isNotNull();
      assertThat(store.workspaces()).contains(workspace);
    }

    @Test
    void newWorkspaceIsEmpty() {
      CWorkspace workspace = store.createWorkspace();
      assertThat(workspace.memberCollections()).isEmpty();
    }

    @Test
    void createCollectionCreatesACollection() {
      CWorkspace workspace = store.createWorkspace();
      CCollection collection = workspace.createCollection();
      assertThat(workspace.memberCollections()).contains(collection);
      assertThat(collection.parentWorkspace()).contains(workspace);
      assertThat(store.collections()).contains(collection);
    }
  }

  @Nested
  @SuppressWarnings("unused")
  class Collections {
    @Test
    void collectionsEmptyByDefault() {
      Traversable<CCollection> collections = store.collections();
      assertThat(collections).isEmpty();
    }

    @Test
    void createCollectionCreatesACollection() {
      CCollection collection = store.createCollection();
      assertThat(collection).isNotNull();
      assertThat(store.collections()).contains(collection);
    }
  }

  @Nested
  @SuppressWarnings("unused")
  class Objects {
    @Test
    void objectsEmptyByDefault() {
      Traversable<CObject> objects = store.objects();
      assertThat(objects).isEmpty();
    }
  }

  @Nested
  @SuppressWarnings("unused")
  class Files {
    @Test
    void filesEmptyByDefault() {
      Traversable<CFile> files = store.files();
      assertThat(files).isEmpty();
    }
  }
}

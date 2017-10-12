package org.cdlib.cursive.store;

import io.vavr.collection.Traversable;
import org.cdlib.cursive.core.*;

public interface Store {
  Traversable<CWorkspace> workspaces();
  CWorkspace createWorkspace();

  Traversable<CCollection> collections();
  CCollection createCollection();

  Traversable<CObject> objects();
  CObject createObject();

  Traversable<CFile> files();
}

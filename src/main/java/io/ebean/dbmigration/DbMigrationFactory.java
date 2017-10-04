package io.ebean.dbmigration;

import java.util.Iterator;
import java.util.ServiceLoader;

class DbMigrationFactory {

  static DbMigration create() {

    Iterator<DbMigration> loader = ServiceLoader.load(DbMigration.class).iterator();
    if (loader.hasNext()) {
      return loader.next();
    }
    throw new IllegalStateException("No service implementation found for DbMigration?");
  }
}

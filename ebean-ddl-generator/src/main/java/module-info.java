module io.ebean.ddl.generator {

  exports io.ebean.dbmigration;

  provides io.ebean.plugin.Plugin with io.ebeaninternal.dbmigration.DbMigrationPlugin,io.ebeaninternal.dbmigration.DdlPlugin;

  requires transitive io.ebean.ddl.runner;
  requires transitive io.ebean.core;
  requires transitive java.xml.bind;
  requires io.ebean.core.type;
  requires io.ebean.migration;

  uses io.ebean.dbmigration.DbMigration;

  // support existing tests
  exports io.ebeaninternal.extraddl.model to io.ebean.test;
}

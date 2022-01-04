open module io.ebean.dbmigration.runner {

  provides io.ebean.plugin.Plugin with io.ebeaninternal.dbmigration.run.DbRunMigrationPlugin;

  requires transitive io.ebean.ddl.runner;
  requires transitive io.ebean.core;
  requires transitive java.xml.bind;
  requires io.ebean.core.type;
  requires io.ebean.migration;

}

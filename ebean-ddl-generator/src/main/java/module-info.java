module io.ebean.ddl.generator {


  uses io.ebean.dbmigration.DbMigration;
  uses io.ebean.plugin.Plugin;
  uses io.ebean.config.dbplatform.DatabasePlatformProvider;
  
  
  exports io.ebean.dbmigration;

  provides io.ebean.plugin.Plugin with io.ebeaninternal.dbmigration.DbMigrationPlugin,io.ebeaninternal.dbmigration.DdlPlugin;
  provides io.ebean.dbmigration.DbMigration with io.ebeaninternal.dbmigration.DefaultDbMigration;

  requires transitive io.ebean.ddl.runner;
  requires transitive io.ebean.core;
  requires transitive jakarta.xml.bind;
  requires io.ebean.core.type;
  requires io.ebean.migration;


  // support existing tests
  exports io.ebeaninternal.extraddl.model to io.ebean.test;
  opens io.ebeaninternal.extraddl.model to java.xml.bind;
  opens io.ebeaninternal.dbmigration.migration to java.xml.bind;
}

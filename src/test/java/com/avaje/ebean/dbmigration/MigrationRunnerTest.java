package com.avaje.ebean.dbmigration;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.config.DbMigrationConfig;
import org.junit.Test;


public class MigrationRunnerTest {


  @Test
  public void test() {

    EbeanServer server = Ebean.getDefaultServer();

    DbMigrationConfig config = new DbMigrationConfig();
    config.setMigrationPath("test-dbmigration");
    config.setRunMigration(true);
    config.setDbUser("sa");
    config.setDbPassword("");

    MigrationRunner runner = new MigrationRunner(server, config);

    runner.run();
  }
}
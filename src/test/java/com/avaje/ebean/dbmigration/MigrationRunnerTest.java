package com.avaje.ebean.dbmigration;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.config.DbMigrationConfig;
import org.junit.Test;


public class MigrationRunnerTest extends BaseTestCase {


  @Test
  public void test() {

    if (!isH2()) {
      return;
    }

    EbeanServer server = Ebean.getDefaultServer();

    DbMigrationConfig config = new DbMigrationConfig();
    config.setMigrationPath("test-dbmigration");
    config.setRunMigration(true);
    config.setDbUser("sa");
    config.setDbPassword("");

    new MigrationRunner(server, config).run();
  }
}
package io.ebeaninternal.dbmigration;

import java.io.IOException;

import io.ebean.config.DatabaseConfig;
import io.ebean.plugin.Plugin;
import io.ebean.plugin.SpiServer;

public class DbMigrationPlugin implements Plugin {

  private DefaultDbMigration dbMigration;

  private static String lastMigration;
  private static String lastInit;

  @Override
  public void configure(SpiServer server) {
    dbMigration = new DefaultDbMigration();
    dbMigration.setServer(server);
  }

  @Override
  public void online(boolean online) {
    try {
      lastInit = null;
      lastMigration = null;
      if (dbMigration.generate) {
        String tmp = lastMigration = dbMigration.generateMigration();
        if (tmp == null) {
          return;
        }
      }
      if (dbMigration.generateInit) {
        lastInit = dbMigration.generateInitMigration();
      }
    } catch (IOException e) {
      throw new RuntimeException("Error while generating migration");
    }
  }

  @Override
  public void shutdown() {
    dbMigration = null;
  }

  public static String getLastInit() {
    return lastInit;
  }
  
  public static String getLastMigration() {
    return lastMigration;
  }
}

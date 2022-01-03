package io.ebeaninternal.dbmigration;

import java.io.IOException;

import io.ebean.plugin.Plugin;
import io.ebean.plugin.SpiServer;

public class DbMigrationPlugin implements Plugin {

  private DefaultDbMigration dbMigration;

  @Override
  public void configure(SpiServer server) {
    dbMigration = new DefaultDbMigration();
    dbMigration.setServer(server);
  }

  @Override
  public void online(boolean online) {
    try {
      if (dbMigration.generate) {
        dbMigration.generateMigration();
      }
      if (dbMigration.generateInit) {
        dbMigration.generateInitMigration();
      }
    } catch (IOException e) {
      throw new RuntimeException("Error while generating migration");
    }
  }

  @Override
  public void shutdown() {
    dbMigration = null;
  }

}

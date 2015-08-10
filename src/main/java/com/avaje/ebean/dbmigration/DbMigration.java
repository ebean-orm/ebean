package com.avaje.ebean.dbmigration;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.config.DbMigrationConfig;
import com.avaje.ebean.dbmigration.model.CurrentModel;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 *
 */
public class DbMigration {

  private static final Logger logger = LoggerFactory.getLogger(DbMigration.class);

  private final SpiEbeanServer server;

  private final DbMigrationConfig migrationConfig;

  public DbMigration() {
    this(Ebean.getDefaultServer());
  }

  public DbMigration(EbeanServer ebeanServer) {
    this.server = (SpiEbeanServer) ebeanServer;
    this.migrationConfig = server.getServerConfig().getMigrationConfig();
  }

  public void writeCurrent() {

    CurrentModel currentModel = new CurrentModel(server);

    File writeTo = getWritePath();
    logger.info("... write to {}", writeTo.getAbsolutePath());
    currentModel.writeMigration(writeTo);
  }

  public File getWritePath() {
    File resourceRootDir = new File("./dbmigration-test/resources");

    // expect to be a relative path
    String resourcePath = migrationConfig.getResourcePath();

    File path = new File(resourceRootDir, resourcePath);
    if (!path.exists()) {
      path.mkdirs();
    }
    return new File(path, "migration-current.xml");
  }
}

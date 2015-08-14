package com.avaje.ebean.dbmigration;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.config.DbConstraintNaming;
import com.avaje.ebean.config.DbMigrationConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DB2Platform;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.config.dbplatform.DbPlatformName;
import com.avaje.ebean.config.dbplatform.H2Platform;
import com.avaje.ebean.config.dbplatform.MsSqlServer2005Platform;
import com.avaje.ebean.config.dbplatform.MySqlPlatform;
import com.avaje.ebean.config.dbplatform.Oracle10Platform;
import com.avaje.ebean.config.dbplatform.PostgresPlatform;
import com.avaje.ebean.config.dbplatform.SQLitePlatform;
import com.avaje.ebean.dbmigration.ddlgeneration.DdlWrite;
import com.avaje.ebean.dbmigration.migration.Migration;
import com.avaje.ebean.dbmigration.model.CurrentModel;
import com.avaje.ebean.dbmigration.model.MConfiguration;
import com.avaje.ebean.dbmigration.model.MigrationModel;
import com.avaje.ebean.dbmigration.model.ModelContainer;
import com.avaje.ebean.dbmigration.model.ModelDdlWriter;
import com.avaje.ebean.dbmigration.model.ModelDiff;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class DbMigration {

  private static final Logger logger = LoggerFactory.getLogger(DbMigration.class);

  private SpiEbeanServer server;

  private DbMigrationConfig migrationConfig;

  private String pathToResources = "src/main/resources";

  private DatabasePlatform databasePlatform;

  private ServerConfig serverConfig;

  private DbConstraintNaming constraintNaming;

  public DbMigration() {
    DbOffline.asH2();
  }

  public void setPathToResources(String pathToResources) {
    this.pathToResources = pathToResources;
  }

  public void setServer(EbeanServer ebeanServer) {
    this.server = (SpiEbeanServer) ebeanServer;
    setServerConfig(server.getServerConfig());
  }

  public void setServerConfig(ServerConfig config) {
    if (this.serverConfig == null) {
      this.serverConfig = config;
    }
    if (migrationConfig == null) {
      this.migrationConfig = serverConfig.getMigrationConfig();
    }
    if (constraintNaming == null) {
      this.constraintNaming = serverConfig.getConstraintNaming();
    }
  }

  public void setPlatform(DbPlatformName platform) {
    setPlatform(getPlatform(platform));
  }

  public void setPlatform(DatabasePlatform databasePlatform) {
    this.databasePlatform = databasePlatform;
    DbOffline.setPlatform(databasePlatform.getName());
  }


  public void runMigration() throws IOException {

    // use this flag to stop other plugins like full DDL generation
    DbOffline.setRunningMigration();

    setDefaults();

    try {
      MigrationModel migrationModel = new MigrationModel(migrationConfig.getResourcePath());
      ModelContainer migrated = migrationModel.read();
      int nextMajorVersion = migrationModel.getNextMajorVersion();

      logger.info("next migration version {}", nextMajorVersion);

      CurrentModel currentModel = new CurrentModel(server, constraintNaming);
      ModelContainer current = currentModel.read();

      ModelDiff diff = new ModelDiff(migrated);
      diff.compareTo(current);
      Migration dbMigration = diff.getMigration();

      // writer needs the current model to provide table/column details for
      // history ddl generation (triggers, history tables etc)
      DdlWrite write = new DdlWrite(new MConfiguration(), currentModel.read());

      ModelDdlWriter writer = new ModelDdlWriter(databasePlatform, serverConfig);
      if (!writer.processMigration(dbMigration, write)) {
        logger.info("no changes detected - no migration written");

      } else {
        // there were actually changes to write
        File writePath = getWritePath();
        logger.info("migration writing version {} to {}", nextMajorVersion, writePath.getAbsolutePath());
        writer.writeMigration(writePath, nextMajorVersion);
      }

    } finally {
      DbOffline.reset();
    }
  }

  protected void setDefaults() {
    if (server == null) {
      setServer(Ebean.getDefaultServer());
    }
    if (databasePlatform == null) {
      databasePlatform = server.getDatabasePlatform();
    }
  }

  protected File getWritePath() {

    // path to src/main/resources in typical maven project
    File resourceRootDir = new File(pathToResources);

    String resourcePath = migrationConfig.getResourcePath();

    // expect to be a path to something like - src/main/resources/dbmigration/myapp
    File path = new File(resourceRootDir, resourcePath);
    if (!path.exists()) {
      if (!path.mkdirs()) {
        logger.debug("Unable to ensure migration directory exists at {}", path.getAbsolutePath());
      }
    }
    return path;
  }

  protected DatabasePlatform getPlatform(DbPlatformName platform) {
    switch (platform) {
      case H2:
        return new H2Platform();
      case POSTGRES:
        return new PostgresPlatform();
      case MYSQL:
        return new MySqlPlatform();
      case ORACLE:
        return new Oracle10Platform();
      case SQLSERVER:
        return new MsSqlServer2005Platform();
      case DB2:
        return new DB2Platform();
      case SQLITE:
        return new SQLitePlatform();

      default:
        throw new IllegalArgumentException("Platform missing? " + platform);
    }
  }

}

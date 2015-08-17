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
import com.avaje.ebean.dbmigration.migrationreader.MigrationXmlWriter;
import com.avaje.ebean.dbmigration.model.CurrentModel;
import com.avaje.ebean.dbmigration.model.MConfiguration;
import com.avaje.ebean.dbmigration.model.MigrationModel;
import com.avaje.ebean.dbmigration.model.ModelContainer;
import com.avaje.ebean.dbmigration.model.PlatformDdlWriter;
import com.avaje.ebean.dbmigration.model.ModelDiff;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DbMigration {

  protected static final Logger logger = LoggerFactory.getLogger(DbMigration.class);

  protected SpiEbeanServer server;

  protected DbMigrationConfig migrationConfig;

  protected String pathToResources = "src/main/resources";

  protected DatabasePlatform databasePlatform;

  protected List<Pair> platforms = new ArrayList<Pair>();

  protected ServerConfig serverConfig;

  protected DbConstraintNaming constraintNaming;

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

  /**
   * Add an additional platform to write the migration DDL.
   */
  public void addPlatform(DbPlatformName platform, String prefix) {
    if (!prefix.endsWith("-")) {
      prefix+="-";
    }
    platforms.add(new Pair(getPlatform(platform), prefix));
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

      if (diff.isEmpty()) {
        logger.info("no changes detected - no migration written");
        return;
      }

      // there were actually changes to write
      Migration dbMigration = diff.getMigration();

      File writePath = getWritePath();
      logger.info("migration writing version {} to {}", nextMajorVersion, writePath.getAbsolutePath());
      writeMigrationXml(dbMigration, writePath, nextMajorVersion);

      if (databasePlatform != null) {
        // writer needs the current model to provide table/column details for
        // history ddl generation (triggers, history tables etc)
        DdlWrite write = new DdlWrite(new MConfiguration(), currentModel.read());
        PlatformDdlWriter writer = new PlatformDdlWriter(databasePlatform, serverConfig);
        writer.processMigration(dbMigration, write, writePath, nextMajorVersion);
      }

      writeExtraPlatformDdl(nextMajorVersion, currentModel, dbMigration, writePath);

    } finally {
      DbOffline.reset();
    }
  }

  /**
   * Write any extra platform ddl.
   */
  protected void writeExtraPlatformDdl(int nextMajorVersion, CurrentModel currentModel, Migration dbMigration, File writePath) throws IOException {

    for (Pair pair : platforms) {
      DdlWrite platformBuffer = new DdlWrite(new MConfiguration(), currentModel.read());

      PlatformDdlWriter platformWriter = new PlatformDdlWriter(pair.platform, serverConfig, pair.prefix);
      platformWriter.processMigration(dbMigration, platformBuffer, writePath, nextMajorVersion);
    }
  }

  protected void writeMigrationXml(Migration dbMigration, File resourcePath, int migrationVersion) {
    File file = new File(resourcePath, "v"+migrationVersion+".0.xml");

    MigrationXmlWriter xmlWriter = new MigrationXmlWriter();
    xmlWriter.write(dbMigration, file);
  }

  protected void setDefaults() {
    if (server == null) {
      setServer(Ebean.getDefaultServer());
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

  /**
   * Holds a platform and prefix. Used to generate multiple platform specific DDL
   * for a single migration.
   */
  public static class Pair {

    /**
     * The platform to generate the DDL for.
     */
    public final DatabasePlatform platform;

    /**
     * A prefix included into the file/resource names indicating the platform.
     */
    public final String prefix;

    public Pair(DatabasePlatform platform, String prefix) {
      this.platform = platform;
      this.prefix = prefix;
    }
  }

}

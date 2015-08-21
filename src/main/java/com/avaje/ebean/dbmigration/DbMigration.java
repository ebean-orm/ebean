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
import com.avaje.ebean.config.dbplatform.OraclePlatform;
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
 * Generates DB Migration xml and sql scripts.
 * <p>
 * Reads the prior migrations and compares with the current model of the EbeanServer
 * and generates a migration 'diff' in the form of xml document with the logical schema
 * changes and a series of sql scripts to apply, rollback the applied changes if necessary
 * and drop objects (drop tables, drop columns).
 * </p>
 * <p>
 *   This does not run the migration or ddl scripts but just generates them.
 * </p>
 * <pre>{@code
 *
 *       DbMigration migration = new DbMigration();
 *       migration.setPathToResources("src/main/resources");
 *       migration.setPlatform(DbPlatformName.ORACLE);
 *
 *       migration.generateMigration();
 *
 * }</pre>
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
  }

  /**
   * Set the path from the current working directory to the application resources.
   *
   * This defaults to maven style 'src/main/resources'.
   */
  public void setPathToResources(String pathToResources) {
    this.pathToResources = pathToResources;
  }

  /**
   * Set the server to use to determine the current model.
   * Typically this is not called explicitly.
   */
  public void setServer(EbeanServer ebeanServer) {
    this.server = (SpiEbeanServer) ebeanServer;
    setServerConfig(server.getServerConfig());
  }

  /**
   * Set the serverConfig to use. Typically this is not called explicitly.
   */
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

  /**
   * Set the specific platform to generate DDL for.
   * <p>
   * If not set this defaults to the platform of the default server.
   * </p>
   */
  public void setPlatform(DbPlatformName platform) {
    setPlatform(getPlatform(platform));
  }

  /**
   * Set the specific platform to generate DDL for.
   * <p>
   * If not set this defaults to the platform of the default server.
   * </p>
   */
  public void setPlatform(DatabasePlatform databasePlatform) {
    this.databasePlatform = databasePlatform;
    DbOffline.setPlatform(databasePlatform.getName());
  }

  /**
   * Add an additional platform to write the migration DDL.
   * <p>
   * Use this when you want to generate sql scripts for multiple database platforms
   * from the migration (e.g. generate migration sql for MySql, Postgres and Oracle).
   * </p>
   */
  public void addPlatform(DbPlatformName platform, String prefix) {
    if (!prefix.endsWith("-")) {
      prefix += "-";
    }
    platforms.add(new Pair(getPlatform(platform), prefix));
  }

  /**
   * Generate the next migration xml file and associated apply and rollback sql scripts.
   * <p>
   *   This does not run the migration or ddl scripts but just generates them.
   * </p>
   * <h3>Example: Run for a single specific platform</h3>
   * <pre>{@code
   *
   *       DbMigration migration = new DbMigration();
   *       migration.setPathToResources("src/main/resources");
   *       migration.setPlatform(DbPlatformName.ORACLE);
   *
   *       migration.generateMigration();
   *
   * }</pre>
   *
   * <h3>Example: Run migration generating DDL for multiple platforms</h3>
   * <pre>{@code
   *
   *       DbMigration migration = new DbMigration();
   *       migration.setPathToResources("src/main/resources");
   *
   *       migration.addPlatform(DbPlatformName.POSTGRES, "pg");
   *       migration.addPlatform(DbPlatformName.MYSQL, "mysql");
   *       migration.addPlatform(DbPlatformName.ORACLE, "mysql");
   *
   *       migration.generateMigration();
   *
   * }</pre>
   */
  public void generateMigration() throws IOException {

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

  /**
   * Write the migration xml.
   */
  protected void writeMigrationXml(Migration dbMigration, File resourcePath, int migrationVersion) {

    File file = new File(resourcePath, "v"+migrationVersion+".0.xml");
    MigrationXmlWriter xmlWriter = new MigrationXmlWriter();
    xmlWriter.write(dbMigration, file);
  }

  /**
   * Set default server and platform if necessary.
   */
  protected void setDefaults() {
    if (server == null) {
      setServer(Ebean.getDefaultServer());
    }
    if (databasePlatform == null && platforms.isEmpty()) {
      // not explicitly set not set a list of platforms so
      // default to the platform of the default server
      databasePlatform = server.getDatabasePlatform();
      logger.debug("set platform to {}", databasePlatform.getName());
    }
  }

  /**
   * Return the file path to write the xml and sql to.
   */
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

  /**
   * Return the DatabasePlatform given the platform key.
   */
  protected DatabasePlatform getPlatform(DbPlatformName platform) {
    switch (platform) {
      case H2:
        return new H2Platform();
      case POSTGRES:
        return new PostgresPlatform();
      case MYSQL:
        return new MySqlPlatform();
      case ORACLE:
        return new OraclePlatform();
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

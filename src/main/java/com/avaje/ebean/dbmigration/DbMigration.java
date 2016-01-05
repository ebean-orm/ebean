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
import com.avaje.ebean.dbmigration.model.ModelDiff;
import com.avaje.ebean.dbmigration.model.PlatformDdlWriter;
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

  private static final String initialVersion = "1.0";

  /**
   * Set to true if DbMigration run with online EbeanServer instance.
   */
  protected final boolean online;

  protected SpiEbeanServer server;

  protected DbMigrationConfig migrationConfig;

  protected String pathToResources = "src/main/resources";

  protected DatabasePlatform databasePlatform;

  protected List<Pair> platforms = new ArrayList<Pair>();

  protected ServerConfig serverConfig;

  protected DbConstraintNaming constraintNaming;

  /**
   * Create for offline migration generation.
   */
  public DbMigration() {
    this.online = false;
  }

  /**
   * Create using online EbeanServer.
   */
  public DbMigration(EbeanServer server) {
    this.online = true;
    setServer(server);
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
    if (!online) {
      DbOffline.setPlatform(databasePlatform.getName());
    }
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
    if (!online) {
      DbOffline.setRunningMigration();
    }

    setDefaults();

    try {

      File migrationDir = getMigrationDirectory();
      File modelDir = getModelDirectory(migrationDir);

      MigrationModel migrationModel = new MigrationModel(modelDir, migrationConfig.getModelSuffix());
      ModelContainer migrated = migrationModel.read();

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

      String fullVersion = getFullVersion(migrationModel);

      logger.info("generating migration:{}", fullVersion);
      if (!writeMigrationXml(dbMigration, modelDir, fullVersion)) {
        logger.warn("migration already exists, not generating DDL");

      } else {
        if (databasePlatform != null) {
          // writer needs the current model to provide table/column details for
          // history ddl generation (triggers, history tables etc)
          DdlWrite write = new DdlWrite(new MConfiguration(), currentModel.read());
          PlatformDdlWriter writer = createDdlWriter(databasePlatform, "");
          writer.processMigration(dbMigration, write, migrationDir , fullVersion);
        }
        writeExtraPlatformDdl(fullVersion, currentModel, dbMigration, migrationDir);
      }

    } finally {
      if (!online) {
        DbOffline.reset();
      }
    }
  }

  /**
   * Return the full version for the migration being generated.
   */
  private String getFullVersion(MigrationModel migrationModel) {

    String version = migrationConfig.getVersion();
    if (version == null) {
      version = migrationModel.getNextVersion(initialVersion);
    }

    String fullVersion = version;

    String name = migrationConfig.getName();
    if (name != null) {
      fullVersion += "__" + toUnderScore(name);
    }
    return fullVersion;
  }

  /**
   * Replace spaces with underscores.
   */
  private String toUnderScore(String name) {
    return name.replace(' ','_');
  }

  /**
   * Write any extra platform ddl.
   */
  protected void writeExtraPlatformDdl(String fullVersion, CurrentModel currentModel, Migration dbMigration, File writePath) throws IOException {

    for (Pair pair : platforms) {
      DdlWrite platformBuffer = new DdlWrite(new MConfiguration(), currentModel.read());
      PlatformDdlWriter platformWriter = createDdlWriter(pair);
      platformWriter.processMigration(dbMigration, platformBuffer, writePath, fullVersion);
    }
  }

  private PlatformDdlWriter createDdlWriter(Pair pair) {
    return createDdlWriter(pair.platform, pair.prefix);
  }

  private PlatformDdlWriter createDdlWriter(DatabasePlatform platform, String prefix) {
    return new PlatformDdlWriter(platform, serverConfig, prefix, migrationConfig);
  }

  /**
   * Write the migration xml.
   */
  protected boolean writeMigrationXml(Migration dbMigration, File resourcePath, String fullVersion) {

    String modelFile = fullVersion + migrationConfig.getModelSuffix();
    File file = new File(resourcePath, modelFile);
    if (file.exists()) {
      return false;
    }
    MigrationXmlWriter xmlWriter = new MigrationXmlWriter();
    xmlWriter.write(dbMigration, file);
    return true;
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
  protected File getMigrationDirectory() {

    // path to src/main/resources in typical maven project
    File resourceRootDir = new File(pathToResources);
    String resourcePath = migrationConfig.getMigrationPath();

    // expect to be a path to something like - src/main/resources/dbmigration/model
    File path = new File(resourceRootDir, resourcePath);
    if (!path.exists()) {
      if (!path.mkdirs()) {
        logger.debug("Unable to ensure migration directory exists at {}", path.getAbsolutePath());
      }
    }
    return path;
  }

  /**
   * Return the model directory (relative to the migration directory).
   */
  protected File getModelDirectory(File migrationDirectory) {
    String modelPath = migrationConfig.getModelPath();
    if (modelPath ==  null || modelPath.isEmpty()) {
      return migrationDirectory;
    }
    File modelDir = new File(migrationDirectory, migrationConfig.getModelPath());
    if (!modelDir.exists() && !modelDir.mkdirs()) {
      logger.debug("Unable to ensure migration model directory exists at {}", modelDir.getAbsolutePath());
    }
    return modelDir;
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

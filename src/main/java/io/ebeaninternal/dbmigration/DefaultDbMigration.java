package io.ebeaninternal.dbmigration;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.annotation.Platform;
import io.ebean.config.DbConstraintNaming;
import io.ebean.config.DbMigrationConfig;
import io.ebean.config.PlatformConfig;
import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.db2.DB2Platform;
import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebean.config.dbplatform.hsqldb.HsqldbPlatform;
import io.ebean.config.dbplatform.mysql.MySqlPlatform;
import io.ebean.config.dbplatform.oracle.OraclePlatform;
import io.ebean.config.dbplatform.postgres.PostgresPlatform;
import io.ebean.config.dbplatform.sqlanywhere.SqlAnywherePlatform;
import io.ebean.config.dbplatform.sqlite.SQLitePlatform;
import io.ebean.config.dbplatform.sqlserver.SqlServer16Platform;
import io.ebean.config.dbplatform.sqlserver.SqlServer17Platform;
import io.ebean.dbmigration.DbMigration;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.migration.Migration;
import io.ebeaninternal.dbmigration.migrationreader.MigrationXmlWriter;
import io.ebeaninternal.dbmigration.model.CurrentModel;
import io.ebeaninternal.dbmigration.model.MConfiguration;
import io.ebeaninternal.dbmigration.model.MigrationModel;
import io.ebeaninternal.dbmigration.model.MigrationVersion;
import io.ebeaninternal.dbmigration.model.ModelContainer;
import io.ebeaninternal.dbmigration.model.ModelDiff;
import io.ebeaninternal.dbmigration.model.PlatformDdlWriter;
import io.ebeaninternal.extraddl.model.DdlScript;
import io.ebeaninternal.extraddl.model.ExtraDdl;
import io.ebeaninternal.extraddl.model.ExtraDdlXmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
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
 * This does not run the migration or ddl scripts but just generates them.
 * </p>
 * <pre>{@code
 *
 *       DbMigration migration = DbMigration.create();
 *       migration.setPathToResources("src/main/resources");
 *       migration.setPlatform(DbPlatformName.ORACLE);
 *
 *       migration.generateMigration();
 *
 * }</pre>
 */
public class DefaultDbMigration implements DbMigration {

  protected static final Logger logger = LoggerFactory.getLogger(DefaultDbMigration.class);

  private static final String initialVersion = "1.0";

  private static final String GENERATED_COMMENT = "THIS IS A GENERATED FILE - DO NOT MODIFY";

  /**
   * Set to true if DefaultDbMigration run with online EbeanServer instance.
   */
  protected final boolean online;

  protected SpiEbeanServer server;

  protected DbMigrationConfig migrationConfig;

  protected String pathToResources = "src/main/resources";

  protected DatabasePlatform databasePlatform;

  private boolean vanillaPlatform;

  protected List<Pair> platforms = new ArrayList<>();

  protected ServerConfig serverConfig;

  protected DbConstraintNaming constraintNaming;

  protected Boolean strictMode;
  protected Boolean includeGeneratedFileComment;
  protected String header;
  protected String applyPrefix;
  protected String version;
  protected String name;
  protected String generatePendingDrop;

  protected boolean includeBuiltInPartitioning = true;

  /**
   * Create for offline migration generation.
   */
  public DefaultDbMigration() {
    this.online = false;
  }

  /**
   * Create using online EbeanServer.
   */
  public DefaultDbMigration(EbeanServer server) {
    this.online = true;
    setServer(server);
  }

  /**
   * Set the path from the current working directory to the application resources.
   * <p>
   * This defaults to maven style 'src/main/resources'.
   */
  @Override
  public void setPathToResources(String pathToResources) {
    this.pathToResources = pathToResources;
  }

  /**
   * Set the server to use to determine the current model.
   * Typically this is not called explicitly.
   */
  @Override
  public void setServer(EbeanServer ebeanServer) {
    this.server = (SpiEbeanServer) ebeanServer;
    setServerConfig(server.getServerConfig());
  }

  /**
   * Set the serverConfig to use. Typically this is not called explicitly.
   */
  @Override
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

  @Override
  public void setStrictMode(boolean strictMode) {
    this.strictMode = strictMode;
  }

  @Override
  public void setApplyPrefix(String applyPrefix) {
    this.applyPrefix = applyPrefix;
  }

  @Override
  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void setGeneratePendingDrop(String generatePendingDrop) {
    this.generatePendingDrop = generatePendingDrop;
  }

  @Override
  public void setIncludeGeneratedFileComment(boolean includeGeneratedFileComment) {
    this.includeGeneratedFileComment = includeGeneratedFileComment;
  }

  @Override
  public void setIncludeBuiltInPartitioning(boolean includeBuiltInPartitioning) {
    this.includeBuiltInPartitioning = includeBuiltInPartitioning;
  }

  @Override
  public void setHeader(String header) {
    this.header = header;
  }

  /**
   * Set the specific platform to generate DDL for.
   * <p>
   * If not set this defaults to the platform of the default server.
   * </p>
   */
  @Override
  public void setPlatform(Platform platform) {
    vanillaPlatform = true;
    setPlatform(getPlatform(platform));
  }

  /**
   * Set the specific platform to generate DDL for.
   * <p>
   * If not set this defaults to the platform of the default server.
   * </p>
   */
  @Override
  public void setPlatform(DatabasePlatform databasePlatform) {
    this.databasePlatform = databasePlatform;
    if (!online) {
      DbOffline.setPlatform(databasePlatform.getPlatform());
    }
  }

  /**
   * Add an additional platform to write the migration DDL.
   * <p>
   * Use this when you want to generate sql scripts for multiple database platforms
   * from the migration (e.g. generate migration sql for MySql, Postgres and Oracle).
   * </p>
   */
  @Override
  public void addPlatform(Platform platform, String prefix) {
    platforms.add(new Pair(getPlatform(platform), prefix));
  }

  @Override
  public void addDatabasePlatform(DatabasePlatform databasePlatform, String prefix) {
    platforms.add(new Pair(databasePlatform, prefix));
  }

  /**
   * Generate the next migration xml file and associated apply and rollback sql scripts.
   * <p>
   * This does not run the migration or ddl scripts but just generates them.
   * </p>
   * <h3>Example: Run for a single specific platform</h3>
   * <pre>{@code
   *
   *       DbMigration migration = DbMigration.create();
   *       migration.setPathToResources("src/main/resources");
   *       migration.setPlatform(DbPlatformName.ORACLE);
   *
   *       migration.generateMigration();
   *
   * }</pre>
   * <p>
   * <h3>Example: Run migration generating DDL for multiple platforms</h3>
   * <pre>{@code
   *
   *       DbMigration migration = DbMigration.create();
   *       migration.setPathToResources("src/main/resources");
   *
   *       migration.addPlatform(DbPlatformName.POSTGRES, "pg");
   *       migration.addPlatform(DbPlatformName.MYSQL, "mysql");
   *       migration.addPlatform(DbPlatformName.ORACLE, "mysql");
   *
   *       migration.generateMigration();
   *
   * }</pre>
   * @return the generated migration or null
   */
  @Override
  public String generateMigration() throws IOException {

    // use this flag to stop other plugins like full DDL generation
    if (!online) {
      DbOffline.setGenerateMigration();
      if (databasePlatform == null && !platforms.isEmpty()) {
        // for multiple platform generation the first platform
        // is used to generate the "logical" model diff
        setPlatform(platforms.get(0).platform);
      }
    }
    setDefaults();
    if (!platforms.isEmpty()) {
      configurePlatforms();
    }
    try {
      Request request = createRequest();
      if (platforms.isEmpty()) {
        generateExtraDdl(request.migrationDir, databasePlatform, request.isTablePartitioning());
      }

      String pendingVersion = generatePendingDrop();
      if (pendingVersion != null) {
        return generatePendingDrop(request, pendingVersion);
      } else {
        return generateDiff(request);
      }

    } finally {
      if (!online) {
        DbOffline.reset();
      }
    }
  }

  /**
   * Load the configuration for each of the target platforms.
   */
  private void configurePlatforms() {
    for (Pair pair : platforms) {
      PlatformConfig config = serverConfig.newPlatformConfig("dbmigration.platform", pair.prefix);
      pair.platform.configure(config);
    }
  }

  /**
   * Generate "repeatable" migration scripts.
   * <p>
   * These take scrips from extra-dll.xml (typically views) and outputs "repeatable"
   * migration scripts (starting with "R__") to be run by FlywayDb or Ebean's own
   * migration runner.
   * </p>
   */
  private void generateExtraDdl(File migrationDir, DatabasePlatform dbPlatform, boolean tablePartitioning) throws IOException {

    if (dbPlatform != null) {
      if (tablePartitioning && includeBuiltInPartitioning) {
        generateExtraDdlFor(migrationDir, dbPlatform, ExtraDdlXmlReader.readBuiltinTablePartitioning());
      }
      generateExtraDdlFor(migrationDir, dbPlatform, ExtraDdlXmlReader.readBuiltin());
      generateExtraDdlFor(migrationDir, dbPlatform, ExtraDdlXmlReader.read());
    }
  }

  private void generateExtraDdlFor(File migrationDir, DatabasePlatform dbPlatform, ExtraDdl extraDdl) throws IOException {
    if (extraDdl != null) {
      List<DdlScript> ddlScript = extraDdl.getDdlScript();
      for (DdlScript script : ddlScript) {
        if (!script.isDrop() && ExtraDdlXmlReader.matchPlatform(dbPlatform.getName(), script.getPlatforms())) {
          writeExtraDdl(migrationDir, script);
        }
      }
    }
  }

  /**
   * Write (or override) the "repeatable" migration script.
   */
  private void writeExtraDdl(File migrationDir, DdlScript script) throws IOException {

    String fullName = repeatableMigrationName(script.isInit(), script.getName());

    logger.info("writing repeatable script {}", fullName);

    File file = new File(migrationDir, fullName);
    try (FileWriter writer = new FileWriter(file)) {
      writer.write(script.getValue());
      writer.flush();
    }
  }

  private String repeatableMigrationName(boolean init, String scriptName) {
    StringBuilder sb = new StringBuilder();
    if (init) {
      sb.append("I__");
    } else {
      sb.append("R__");
    }
    sb.append(scriptName.replace(' ', '_'));
    sb.append(migrationConfig.getApplySuffix());
    return sb.toString();
  }

  /**
   * Generate the diff migration.
   */
  private String generateDiff(Request request) throws IOException {

    List<String> pendingDrops = request.getPendingDrops();
    if (!pendingDrops.isEmpty()) {
      logger.info("Pending un-applied drops in versions {}", pendingDrops);
    }

    Migration migration = request.createDiffMigration();
    if (migration == null) {
      logger.info("no changes detected - no migration written");
      return null;
    } else {
      // there were actually changes to write
      return generateMigration(request, migration, null);
    }
  }

  /**
   * Generate the migration based on the pendingDrops from a prior version.
   */
  private String generatePendingDrop(Request request, String pendingVersion) throws IOException {

    Migration migration = request.migrationForPendingDrop(pendingVersion);

    String version = generateMigration(request, migration, pendingVersion);

    List<String> pendingDrops = request.getPendingDrops();
    if (!pendingDrops.isEmpty()) {
      logger.info("... remaining pending un-applied drops in versions {}", pendingDrops);
    }
    return version;
  }

  private Request createRequest() {
    return new Request();
  }

  private class Request {

    final File migrationDir;
    final File modelDir;
    final MigrationModel migrationModel;
    final CurrentModel currentModel;
    final ModelContainer migrated;
    final ModelContainer current;

    private Request() {
      this.migrationDir = getMigrationDirectory();
      this.modelDir = getModelDirectory(migrationDir);
      this.migrationModel = new MigrationModel(modelDir, migrationConfig.getModelSuffix());
      this.migrated = migrationModel.read();
      this.currentModel = new CurrentModel(server, constraintNaming);
      this.current = currentModel.read();
    }

    boolean isTablePartitioning() {
      return current.isTablePartitioning();
    }

    /**
     * Return the migration for the pending drops for a given version.
     */
    public Migration migrationForPendingDrop(String pendingVersion) {

      Migration migration = migrated.migrationForPendingDrop(pendingVersion);

      // register any remaining pending drops
      migrated.registerPendingHistoryDropColumns(current);
      return migration;
    }

    /**
     * Return the list of versions that have pending un-applied drops.
     */
    public List<String> getPendingDrops() {
      return migrated.getPendingDrops();
    }

    /**
     * Create and return the diff of the current model to the migration model.
     */
    public Migration createDiffMigration() {
      ModelDiff diff = new ModelDiff(migrated);
      diff.compareTo(current);
      return diff.isEmpty() ? null : diff.getMigration();
    }
  }

  private String generateMigration(Request request, Migration dbMigration, String dropsFor) throws IOException {

    String fullVersion = getFullVersion(request.migrationModel, dropsFor);

    logger.info("generating migration:{}", fullVersion);
    if (!writeMigrationXml(dbMigration, request.modelDir, fullVersion)) {
      logger.warn("migration already exists, not generating DDL");
      return null;
    } else {
      if (!platforms.isEmpty()) {
        writeExtraPlatformDdl(fullVersion, request.currentModel, dbMigration, request.migrationDir);

      } else if (databasePlatform != null) {
        // writer needs the current model to provide table/column details for
        // history ddl generation (triggers, history tables etc)
        DdlWrite write = new DdlWrite(new MConfiguration(), request.current);
        PlatformDdlWriter writer = createDdlWriter(databasePlatform);
        writer.processMigration(dbMigration, write, request.migrationDir, fullVersion);
      }
      return fullVersion;
    }
  }

  /**
   * Return true if the next pending drop changeSet should be generated as the next migration.
   */
  private String generatePendingDrop() {

    String nextDrop = System.getProperty("ddl.migration.pendingDropsFor");
    if (nextDrop != null) {
      return nextDrop;
    }
    return migrationConfig.getGeneratePendingDrop();
  }

  /**
   * Return the full version for the migration being generated.
   * <p>
   * The full version can contain a comment suffix after a "__" double underscore.
   */
  private String getFullVersion(MigrationModel migrationModel, String dropsFor) {

    String version = migrationConfig.getVersion();
    if (version == null) {
      version = migrationModel.getNextVersion(initialVersion);
    }

    String fullVersion = migrationConfig.getApplyPrefix() + version;
    if (migrationConfig.getName() != null) {
      fullVersion += "__" + toUnderScore(migrationConfig.getName());

    } else if (dropsFor != null) {
      fullVersion += "__" + toUnderScore("dropsFor_" + MigrationVersion.trim(dropsFor));

    } else if (version.equals(initialVersion)) {
      fullVersion += "__initial";
    }
    return fullVersion;
  }

  /**
   * Replace spaces with underscores.
   */
  private String toUnderScore(String name) {
    return name.replace(' ', '_');
  }

  /**
   * Write any extra platform ddl.
   */
  protected void writeExtraPlatformDdl(String fullVersion, CurrentModel currentModel, Migration dbMigration, File writePath) throws IOException {

    for (Pair pair : platforms) {
      DdlWrite platformBuffer = new DdlWrite(new MConfiguration(), currentModel.read());
      PlatformDdlWriter platformWriter = createDdlWriter(pair.platform);
      File subPath = platformWriter.subPath(writePath, pair.prefix);
      platformWriter.processMigration(dbMigration, platformBuffer, subPath, fullVersion);

      generateExtraDdl(subPath, pair.platform, currentModel.isTablePartitioning());
    }
  }

  private PlatformDdlWriter createDdlWriter(DatabasePlatform platform) {
    return new PlatformDdlWriter(platform, serverConfig, migrationConfig);
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
    String comment = migrationConfig.isIncludeGeneratedFileComment() ? GENERATED_COMMENT : null;
    MigrationXmlWriter xmlWriter = new MigrationXmlWriter(comment);
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
    if (vanillaPlatform || databasePlatform == null) {
      // not explicitly set so use the platform of the server
      databasePlatform = server.getDatabasePlatform();
      logger.trace("set platform to {}", databasePlatform.getName());
    }
    if (migrationConfig != null) {
      if (strictMode != null) {
        migrationConfig.setStrictMode(strictMode);
      }
      if (applyPrefix != null) {
        migrationConfig.setApplyPrefix(applyPrefix);
      }
      if (header != null) {
        migrationConfig.setDdlHeader(header);
      }
      if (includeGeneratedFileComment != null) {
        migrationConfig.setIncludeGeneratedFileComment(includeGeneratedFileComment);
      }
      if (version != null) {
        migrationConfig.setVersion(version);
      }
      if (name != null) {
        migrationConfig.setName(name);
      }
      if (generatePendingDrop != null) {
        migrationConfig.setGeneratePendingDrop(generatePendingDrop);
      }
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
    if (modelPath == null || modelPath.isEmpty()) {
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
  protected DatabasePlatform getPlatform(Platform platform) {
    switch (platform) {
      case H2:
        return new H2Platform();
      case HSQLDB:
        return new HsqldbPlatform();
      case POSTGRES:
        return new PostgresPlatform();
      case MYSQL:
        return new MySqlPlatform();
      case ORACLE:
        return new OraclePlatform();
      case SQLANYWHERE:
        return new SqlAnywherePlatform();
      case SQLSERVER16:
        return new SqlServer16Platform();
      case SQLSERVER17:
        return new SqlServer17Platform();
      case SQLSERVER:
        throw new IllegalArgumentException("Please choose the more specific SQLSERVER16 or SQLSERVER17 platform. Refer to issue #1340 for details");
      case DB2:
        return new DB2Platform();
      case SQLITE:
        return new SQLitePlatform();
      case GENERIC:
        return new DatabasePlatform();

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

package io.ebeaninternal.dbmigration;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.annotation.Platform;
import io.ebean.config.DbConstraintNaming;
import io.ebean.config.DbMigrationConfig;
import io.ebean.config.PlatformConfig;
import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.clickhouse.ClickHousePlatform;
import io.ebean.config.dbplatform.cockroach.CockroachPlatform;
import io.ebean.config.dbplatform.db2.DB2Platform;
import io.ebean.config.dbplatform.h2.H2Platform;
import io.ebean.config.dbplatform.hana.HanaPlatform;
import io.ebean.config.dbplatform.hsqldb.HsqldbPlatform;
import io.ebean.config.dbplatform.mysql.MySqlPlatform;
import io.ebean.config.dbplatform.oracle.OraclePlatform;
import io.ebean.config.dbplatform.postgres.PostgresPlatform;
import io.ebean.config.dbplatform.sqlanywhere.SqlAnywherePlatform;
import io.ebean.config.dbplatform.sqlite.SQLitePlatform;
import io.ebean.config.dbplatform.sqlserver.SqlServer16Platform;
import io.ebean.config.dbplatform.sqlserver.SqlServer17Platform;
import io.ebean.dbmigration.DbMigration;
import io.ebean.migration.MigrationVersion;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.migration.Migration;
import io.ebeaninternal.dbmigration.migrationreader.MigrationXmlWriter;
import io.ebeaninternal.dbmigration.model.CurrentModel;
import io.ebeaninternal.dbmigration.model.MConfiguration;
import io.ebeaninternal.dbmigration.model.MigrationModel;
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
 *       migration.setPlatform(Platform.POSTGRES);
 *
 *       migration.generateMigration();
 *
 * }</pre>
 */
public class DefaultDbMigration implements DbMigration {

  protected static final Logger logger = LoggerFactory.getLogger("io.ebean.GenerateMigration");

  private static final String initialVersion = "1.0";

  private static final String GENERATED_COMMENT = "THIS IS A GENERATED FILE - DO NOT MODIFY";

  private boolean logToSystemOut = true;

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
   *
   * @return the generated migration or null
   */
  @Override
  public String generateMigration() throws IOException {
    return generateMigrationFor(false);
  }

  @Override
  public String generateInitMigration() throws IOException {
    return generateMigrationFor(true);
  }

  private String generateMigrationFor(boolean dbinitMigration) throws IOException {

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
      Request request = createRequest(dbinitMigration);
      if (!dbinitMigration) {
        // repeatable migrations
        if (platforms.isEmpty()) {
          generateExtraDdl(request.migrationDir, databasePlatform, request.isTablePartitioning());
        } else {
          for (Pair pair : platforms) {
            PlatformDdlWriter platformWriter = createDdlWriter(pair.platform);
            File subPath = platformWriter.subPath(request.migrationDir, pair.prefix);
            generateExtraDdl(subPath, pair.platform, request.isTablePartitioning());
          }
        }
      }

      String pendingVersion = generatePendingDrop();
      if (pendingVersion != null) {
        return generatePendingDrop(request, pendingVersion);
      } else {
        return generateDiff(request);
      }

    } catch (UnknownResourcePathException e) {
      logError("ERROR - " + e.getMessage());
      logError("Check the working directory or change dbMigration.setPathToResources() value?");
      return null;

    } finally {
      if (!online) {
        DbOffline.reset();
      }
    }
  }

  /**
   * Return the versions containing pending drops.
   */
  @Override
  public List<String> getPendingDrops() {
    if (!online) {
      DbOffline.setGenerateMigration();
    }
    setDefaults();
    try {
      return createRequest(false).getPendingDrops();
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
   * These take scrips from extra-ddl.xml (typically views) and outputs "repeatable"
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
    logger.debug("writing repeatable script {}", fullName);

    File file = new File(migrationDir, fullName);
    try (FileWriter writer = new FileWriter(file)) {
      writer.write(script.getValue());
      writer.flush();
    }
  }

  @Override
  public void setLogToSystemOut(boolean logToSystemOut) {
    this.logToSystemOut = logToSystemOut;
  }

  private void logError(String message) {
    if (logToSystemOut) {
      System.out.println("DbMigration> " + message);
    } else {
      logger.error(message);
    }
  }

  private void logInfo(String message, Object value) {
    if (value != null) {
      message = String.format(message, value);
    }
    if (logToSystemOut) {
      System.out.println("DbMigration> " + message);
    } else {
      logger.info(message);
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
      logInfo("Pending un-applied drops in versions %s", pendingDrops);
    }

    Migration migration = request.createDiffMigration();
    if (migration == null) {
      logInfo("no changes detected - no migration written", null);
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
      logInfo("... remaining pending un-applied drops in versions %s", pendingDrops);
    }
    return version;
  }

  private Request createRequest(boolean dbinitMigration) {
    return new Request(dbinitMigration);
  }

  private class Request {

    final boolean dbinitMigration;
    final File migrationDir;
    final File modelDir;
    final CurrentModel currentModel;
    final ModelContainer migrated;
    final ModelContainer current;

    private Request(boolean dbinitMigration) {
      this.dbinitMigration = dbinitMigration;
      this.currentModel = new CurrentModel(server, constraintNaming);
      this.current = currentModel.read();
      this.migrationDir = getMigrationDirectory(dbinitMigration);
      if (dbinitMigration) {
        this.modelDir = null;
        this.migrated = new ModelContainer();
      } else {
        this.modelDir = getModelDirectory(migrationDir);
        MigrationModel migrationModel = new MigrationModel(modelDir, migrationConfig.getModelSuffix());
        this.migrated = migrationModel.read(dbinitMigration);
      }
    }

    boolean isTablePartitioning() {
      return current.isTablePartitioning();
    }

    /**
     * Return the next migration version (based on existing migration versions).
     */
    String nextVersion() {
      // always read the next version using the main migration directory (not dbinit)
      File migDirectory = getMigrationDirectory(false);
      File modelDir = getModelDirectory(migDirectory);
      return LastMigration.nextVersion(migDirectory, modelDir, dbinitMigration);
    }

    /**
     * Return the migration for the pending drops for a given version.
     */
    Migration migrationForPendingDrop(String pendingVersion) {

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
    Migration createDiffMigration() {
      ModelDiff diff = new ModelDiff(migrated);
      diff.compareTo(current);
      return diff.isEmpty() ? null : diff.getMigration();
    }
  }

  private String generateMigration(Request request, Migration dbMigration, String dropsFor) throws IOException {

    String fullVersion = getFullVersion(request.nextVersion(), dropsFor);

    logInfo("generating migration:%s", fullVersion);
    if (!request.dbinitMigration && !writeMigrationXml(dbMigration, request.modelDir, fullVersion)) {
      logError("migration already exists, not generating DDL");
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
    return generatePendingDrop;
  }

  /**
   * Return the full version for the migration being generated.
   * <p>
   * The full version can contain a comment suffix after a "__" double underscore.
   */
  private String getFullVersion(String nextVersion, String dropsFor) {

    String version = getVersion();
    if (version == null) {
      version = (nextVersion != null) ? nextVersion : initialVersion;
    }

    String fullVersion = migrationConfig.getApplyPrefix() + version;
    String name = getName();
    if (name != null) {
      fullVersion += "__" + toUnderScore(name);

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
  private void writeExtraPlatformDdl(String fullVersion, CurrentModel currentModel, Migration dbMigration, File writePath) throws IOException {

    for (Pair pair : platforms) {
      DdlWrite platformBuffer = new DdlWrite(new MConfiguration(), currentModel.read());
      PlatformDdlWriter platformWriter = createDdlWriter(pair.platform);
      File subPath = platformWriter.subPath(writePath, pair.prefix);
      platformWriter.processMigration(dbMigration, platformBuffer, subPath, fullVersion);
    }
  }

  private PlatformDdlWriter createDdlWriter(DatabasePlatform platform) {
    return new PlatformDdlWriter(platform, serverConfig, migrationConfig);
  }

  /**
   * Write the migration xml.
   */
  private boolean writeMigrationXml(Migration dbMigration, File resourcePath, String fullVersion) {

    String modelFile = fullVersion + migrationConfig.getModelSuffix();
    File file = new File(resourcePath, modelFile);
    if (file.exists()) {
      return false;
    }
    String comment = Boolean.TRUE.equals(includeGeneratedFileComment) ? GENERATED_COMMENT : null;
    MigrationXmlWriter xmlWriter = new MigrationXmlWriter(comment);
    xmlWriter.write(dbMigration, file);
    return true;
  }

  /**
   * Set default server and platform if necessary.
   */
  private void setDefaults() {
    if (server == null) {
      setServer(Ebean.getDefaultServer());
    }
    if (vanillaPlatform || databasePlatform == null) {
      // not explicitly set so use the platform of the server
      databasePlatform = server.getDatabasePlatform();
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
    }
  }

  /**
   * Return the migration version (typically FlywayDb compatible).
   * <p>
   * Example: 1.1.1_2
   * <p>
   * The version is expected to be the combination of the current pom version plus
   * a 'feature' id. The combined version must be unique and ordered to work with
   * FlywayDb so each developer sets a unique version so that the migration script
   * generated is unique (typically just prior to being submitted as a merge request).
   */
  private String getVersion() {
    String envVersion = readEnvironment("ddl.migration.version");
    if (!isEmpty(envVersion)) {
      return envVersion.trim();
    }
    return version;
  }

  /**
   * Return the migration name which is short description text that can be appended to
   * the migration version to become the ddl script file name.
   * <p>
   * So if the name is "a foo table" then the ddl script file could be:
   * "1.1.1_2__a-foo-table.sql"
   * </p>
   * <p>
   * When the DB migration relates to a git feature (merge request) then this description text
   * is a short description of the feature.
   * </p>
   */
  private String getName() {
    String envName = readEnvironment("ddl.migration.name");
    if (!isEmpty(envName)) {
      return envName.trim();
    }
    return name;
  }

  /**
   * Return true if the string is null or empty.
   */
  private boolean isEmpty(String val) {
    return val == null || val.trim().isEmpty();
  }

  /**
   * Return the system or environment property.
   */
  private String readEnvironment(String key) {

    String val = System.getProperty(key);
    if (val == null) {
      val = System.getenv(key);
    }
    return val;
  }

  /**
   * Return the main migration directory.
   */
  File getMigrationDirectory() {
    return getMigrationDirectory(false);
  }

  /**
   * Return the file path to write the xml and sql to.
   */
  File getMigrationDirectory(boolean dbinitMigration) {

    // path to src/main/resources in typical maven project
    File resourceRootDir = new File(pathToResources);
    if (!resourceRootDir.exists()) {
      String msg = String.format("Error - path to resources %s does not exist. Absolute path is %s", pathToResources, resourceRootDir.getAbsolutePath());
      throw new UnknownResourcePathException(msg);
    }
    String resourcePath = migrationConfig.getMigrationPath(dbinitMigration);

    // expect to be a path to something like - src/main/resources/dbmigration/model
    File path = new File(resourceRootDir, resourcePath);
    if (!path.exists()) {
      if (!path.mkdirs()) {
        logInfo("Warning - Unable to ensure migration directory exists at %s", path.getAbsolutePath());
      }
    }
    return path;
  }

  /**
   * Return the model directory (relative to the migration directory).
   */
  private File getModelDirectory(File migrationDirectory) {
    String modelPath = migrationConfig.getModelPath();
    if (modelPath == null || modelPath.isEmpty()) {
      return migrationDirectory;
    }
    File modelDir = new File(migrationDirectory, migrationConfig.getModelPath());
    if (!modelDir.exists() && !modelDir.mkdirs()) {
      logInfo("Warning - Unable to ensure migration model directory exists at %s", modelDir.getAbsolutePath());
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
      case HANA:
        return new HanaPlatform();
      case COCKROACH:
        return new CockroachPlatform();
      case CLICKHOUSE:
        return new ClickHousePlatform();

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
  static class Pair {

    /**
     * The platform to generate the DDL for.
     */
    final DatabasePlatform platform;

    /**
     * A prefix included into the file/resource names indicating the platform.
     */
    final String prefix;

    Pair(DatabasePlatform platform, String prefix) {
      this.platform = platform;
      this.prefix = prefix;
    }
  }

}

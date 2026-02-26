package io.ebeaninternal.dbmigration;

import io.avaje.applog.AppLog;
import io.avaje.classpath.scanner.core.Location;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.DatabaseBuilder;
import io.ebean.annotation.Platform;
import io.ebean.config.*;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.DatabasePlatformProvider;
import io.ebean.dbmigration.DbMigration;
import io.ebean.util.IOUtils;
import io.ebean.util.StringHelper;
import io.ebeaninternal.api.DbOffline;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlOptions;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlWrite;
import io.ebeaninternal.dbmigration.migration.Migration;
import io.ebeaninternal.dbmigration.migrationreader.MigrationXmlWriter;
import io.ebeaninternal.dbmigration.model.*;
import io.ebeaninternal.extraddl.model.DdlScript;
import io.ebeaninternal.extraddl.model.ExtraDdl;
import io.ebeaninternal.extraddl.model.ExtraDdlXmlReader;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

import static io.ebeaninternal.api.PlatformMatch.matchPlatform;
import static java.lang.System.Logger.Level.*;

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

  protected static final System.Logger logger = AppLog.getLogger("io.ebean.GenerateMigration");
  private static final String initialVersion = "1.0";
  private static final String GENERATED_COMMENT = "THIS IS A GENERATED FILE - DO NOT MODIFY";

  private List<DatabasePlatformProvider> platformProviders = new ArrayList<>();
  protected boolean online;
  private boolean logToSystemOut = true;
  protected SpiEbeanServer server;
  protected String pathToResources = "src/main/resources";

  protected String migrationPath = "dbmigration";
  protected String migrationInitPath = "dbinit";
  protected String modelPath = "model";
  protected String modelSuffix = ".model.xml";

  protected DatabasePlatform databasePlatform;
  private boolean vanillaPlatform;
  protected List<Pair> platforms = new ArrayList<>();
  protected DatabaseBuilder.Settings databaseBuilder;
  protected DbConstraintNaming constraintNaming;
  @Deprecated
  protected Boolean strictMode;
  protected boolean includeGeneratedFileComment;
  @Deprecated
  protected String header;
  protected String applyPrefix = "";
  protected String version;
  protected String name;
  protected String generatePendingDrop;
  private boolean addForeignKeySkipCheck;
  private int lockTimeoutSeconds;
  protected boolean includeBuiltInPartitioning = true;
  protected boolean includeIndex;
  protected boolean generate = false;
  protected boolean generateInit = false;
  private boolean keepLastInit = true;

  /**
   * Create for offline migration generation.
   */
  public DefaultDbMigration() {
    this.online = false;
    for (DatabasePlatformProvider platformProvider : ServiceLoader.load(DatabasePlatformProvider.class)) {
      platformProviders.add(platformProvider);
    }
  }

  @Override
  public void setPathToResources(String pathToResources) {
    this.pathToResources = pathToResources;
  }

  @Override
  public void setMigrationPath(String migrationPath) {
    this.migrationPath = migrationPath;
  }

  @Override
  public void setServer(Database database) {
    this.server = (SpiEbeanServer) database;
    setServerConfig(server.config());
  }

  @Override
  public void setServerConfig(DatabaseBuilder builder) {
    var config = builder.settings();
    if (this.databaseBuilder == null) {
      this.databaseBuilder = config;
    }
    if (constraintNaming == null) {
      this.constraintNaming = databaseBuilder.getConstraintNaming();
    }
    if (databasePlatform == null) {
      this.databasePlatform = databaseBuilder.getDatabasePlatform();
    }
    Properties properties = config.getProperties();
    if (properties != null) {
      PropertiesWrapper props = new PropertiesWrapper("ebean", config.getName(), properties, config.getClassLoadConfig());
      migrationPath = props.get("migration.migrationPath", migrationPath);
      migrationInitPath = props.get("migration.migrationInitPath", migrationInitPath);
      pathToResources = props.get("migration.pathToResources", pathToResources);
      addForeignKeySkipCheck = props.getBoolean("migration.addForeignKeySkipCheck", addForeignKeySkipCheck);
      applyPrefix = props.get("migration.applyPrefix", applyPrefix);
      databasePlatform = props.createInstance(DatabasePlatform.class, "migration.databasePlatform", databasePlatform);
      generatePendingDrop = props.get("migration.generatePendingDrop", generatePendingDrop);
      includeBuiltInPartitioning = props.getBoolean("migration.includeBuiltInPartitioning", includeBuiltInPartitioning);
      includeGeneratedFileComment = props.getBoolean("migration.includeGeneratedFileComment", includeGeneratedFileComment);
      includeIndex = props.getBoolean("migration.includeIndex", includeIndex);
      lockTimeoutSeconds = props.getInt("migration.lockTimeoutSeconds", lockTimeoutSeconds);
      logToSystemOut = props.getBoolean("migration.logToSystemOut", logToSystemOut);
      modelPath = props.get("migration.modelPath", modelPath);
      modelSuffix = props.get("migration.modelSuffix", modelSuffix);
      name = props.get("migration.name", name);
      online = props.getBoolean("migration.online", online);
      vanillaPlatform = props.getBoolean("migration.vanillaPlatform", vanillaPlatform);
      version = props.get("migration.version", version);
      generate = props.getBoolean("migration.generate", generate);
      generateInit = props.getBoolean("migration.generateInit", generateInit);
      // header & strictMode must be configured at DatabaseConfig level
      parsePlatforms(props, config);
    }
  }

  protected void parsePlatforms(PropertiesWrapper props, DatabaseBuilder.Settings config) {
    String platforms = props.get("migration.platforms");
    if (platforms == null || platforms.isEmpty()) {
      return;
    }
    String[] tmp = StringHelper.splitNames(platforms);
    for (String plat : tmp) {
      DatabasePlatform dbPlatform;
      String platformName = plat;
      String platformPrefix = null;
      int pos = plat.indexOf('=');
      if (pos != -1) {
        platformName = plat.substring(0, pos);
        platformPrefix = plat.substring(pos + 1);
      }

      if (platformName.indexOf('.') == -1) {
        // parse platform as enum value
        Platform platform = Enum.valueOf(Platform.class, platformName.toUpperCase());
        dbPlatform = platform(platform);
      } else {
        // parse platform as class
        dbPlatform = (DatabasePlatform) config.getClassLoadConfig().newInstance(platformName);
      }
      if (platformPrefix == null) {
        platformPrefix = dbPlatform.platform().name().toLowerCase();
      }

      addDatabasePlatform(dbPlatform, platformPrefix);
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
  public void setAddForeignKeySkipCheck(boolean addForeignKeySkipCheck) {
    this.addForeignKeySkipCheck = addForeignKeySkipCheck;
  }

  @Override
  public void setLockTimeout(int seconds) {
    this.lockTimeoutSeconds = seconds;
  }

  @Override
  public void setGeneratePendingDrop(String generatePendingDrop) {
    this.generatePendingDrop = generatePendingDrop;
  }

  @Override
  public void setIncludeIndex(boolean includeIndex) {
    this.includeIndex = includeIndex;
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
    setPlatform(platform(platform));
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
      DbOffline.setPlatform(databasePlatform.platform());
    }
  }

  @Override
  public void addPlatform(Platform platform) {
    String prefix = platform.base().name().toLowerCase();
    addPlatform(platform, prefix);
  }

  @Override
  public void addPlatform(Platform platform, String prefix) {
    platforms.add(new Pair(platform(platform), prefix));
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
   *       migration.setPlatform(Platform.ORACLE);
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
   *       migration.addPlatform(Platform.POSTGRES);
   *       migration.addPlatform(Platform.MYSQL);
   *       migration.addPlatform(Platform.ORACLE);
   *
   *       migration.generateMigration();
   *
   * }</pre>
   *
   * @return the generated migration or null
   */
  @Override
  public String generateMigration() throws IOException {
    final String version = generateMigrationFor(false);
    if (includeIndex) {
      generateIndex();
    }
    return version;
  }

  /**
   * Generate the {@code idx_platform.migrations} file.
   */
  private void generateIndex() throws IOException {
    final File topDir = migrationDirectory(false);
    if (!platforms.isEmpty()) {
      for (Pair pair : platforms) {
        new IndexMigration(topDir, pair).generate();
      }
    } else {
      new IndexMigration(topDir, databasePlatform).generate();
    }
  }

  @Override
  public String generateInitMigration() throws IOException {
    return generateMigrationFor(true);
  }

  private String generateMigrationFor(boolean initMigration) throws IOException {
    if (!online) {
      // use this flag to stop other plugins like full DDL generation
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
      Request request = createRequest(initMigration);
      if (!initMigration) {
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
      if ("auto".equals(pendingVersion)) {
        StringJoiner sj = new StringJoiner(",");
        String diff = generateDiff(request);
        if (diff != null) {
          sj.add(diff);
          request = createRequest(initMigration);
        }
        for (String pendingDrop : request.getPendingDrops()) {
          sj.add(generatePendingDrop(request, pendingDrop));
        }
        return sj.length() == 0 ? null : sj.toString();
      } else if (pendingVersion != null) {
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
      PlatformConfig config = databaseBuilder.newPlatformConfig("dbmigration.platform", pair.prefix);
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
        generateExtraDdlFor(migrationDir, dbPlatform, ExtraDdlXmlReader.readBuiltinTablePartitioning(), false);
      }
      // skip built-in migration stored procedures based on isUseMigrationStoredProcedures
      generateExtraDdlFor(migrationDir, dbPlatform, ExtraDdlXmlReader.readBuiltin(), true);
      generateExtraDdlFor(migrationDir, dbPlatform, ExtraDdlXmlReader.read(), false);
    }
  }

  private void generateExtraDdlFor(File migrationDir, DatabasePlatform dbPlatform, ExtraDdl extraDdl, boolean checkSkip) throws IOException {
    if (extraDdl != null) {
      List<DdlScript> ddlScript = extraDdl.getDdlScript();
      for (DdlScript script : ddlScript) {
        if (!script.isDrop() && matchPlatform(dbPlatform.platform(), script.getPlatforms())) {
          if (!checkSkip || dbPlatform.useMigrationStoredProcedures()) {
            writeExtraDdl(migrationDir, script);
          }
        }
      }
    }
  }

  /**
   * Write (or override) the "repeatable" migration script.
   */
  private void writeExtraDdl(File migrationDir, DdlScript script) throws IOException {
    String fullName = repeatableMigrationName(script.isInit(), script.getName());
    logger.log(DEBUG, "writing repeatable script {0}", fullName);
    File file = new File(migrationDir, fullName);
    try (Writer writer = IOUtils.newWriter(file)) {
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
      logger.log(ERROR, message);
    }
  }

  private void logInfo(String message, Object value) {
    if (value != null) {
      message = String.format(message, value);
    }
    if (logToSystemOut) {
      System.out.println("DbMigration> " + message);
    } else {
      logger.log(INFO, message);
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
    sb.append(".sql");
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

  private Request createRequest(boolean initMigration) {
    return new Request(initMigration);
  }

  private class Request {

    final boolean initMigration;
    final File migrationDir;
    final File modelDir;
    final CurrentModel currentModel;
    final ModelContainer migrated;
    final ModelContainer current;

    private Request(boolean initMigration) {
      this.initMigration = initMigration;
      this.currentModel = new CurrentModel(server, constraintNaming);
      this.current = currentModel.read();
      this.migrationDir = migrationDirectory(initMigration);
      if (initMigration) {
        this.modelDir = null;
        this.migrated = new ModelContainer();
      } else {
        this.modelDir = modelDirectory(migrationDir);
        MigrationModel migrationModel = new MigrationModel(modelDir, modelSuffix);
        this.migrated = migrationModel.read(false);
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
      File migDirectory = migrationDirectory(false);
      File modelDir = modelDirectory(migDirectory);
      return LastMigration.nextVersion(migDirectory, modelDir, initMigration);
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
    String fullVersion = fullVersion(request.nextVersion(), dropsFor);
    logInfo("generating migration:%s", fullVersion);
    if (!request.initMigration && !writeMigrationXml(dbMigration, request.modelDir, fullVersion)) {
      logError("migration already exists, not generating DDL");
      return null;
    } else {
      if (!platforms.isEmpty()) {
        writeExtraPlatformDdl(fullVersion, request.currentModel, dbMigration, request.migrationDir, request.initMigration && keepLastInit);

      } else if (databasePlatform != null) {
        // writer needs the current model to provide table/column details for
        // history ddl generation (triggers, history tables etc)
        DdlOptions options = new DdlOptions(addForeignKeySkipCheck);
        DdlWrite writer = new DdlWrite(new MConfiguration(), request.current, options);
        PlatformDdlWriter platformWriter = createDdlWriter(databasePlatform);
        platformWriter.processMigration(dbMigration, writer, request.migrationDir, fullVersion);
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
  private String fullVersion(String nextVersion, String dropsFor) {
    String version = version();
    if (version == null) {
      version = (nextVersion != null) ? nextVersion : initialVersion;
    }
    checkDropVersion(version, dropsFor);

    String fullVersion = applyPrefix + version;
    String name = name();
    if (name != null) {
      fullVersion += "__" + toUnderScore(name);

    } else if (dropsFor != null) {
      fullVersion += "__" + toUnderScore("dropsFor_" + trimDropsFor(dropsFor));

    } else if (version.equals(initialVersion)) {
      fullVersion += "__initial";
    }
    return fullVersion;
  }

  void checkDropVersion(String version, String dropsFor) {
    if (dropsFor != null && dropsFor.equals(version)) {
      throw new IllegalArgumentException("The next migration version must not be the same as the pending drops version of " +
        dropsFor + ". Please make the next migration version higher than " + dropsFor + ".");
    }
  }

  String trimDropsFor(String dropsFor) {
    if (dropsFor.startsWith("V") || dropsFor.startsWith("v")) {
      dropsFor = dropsFor.substring(1);
    }
    int commentStart = dropsFor.indexOf("__");
    if (commentStart > -1) {
      // trim off the trailing comment
      dropsFor = dropsFor.substring(0, commentStart);
    }
    return dropsFor;
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
  private void writeExtraPlatformDdl(String fullVersion, CurrentModel currentModel, Migration dbMigration, File writePath, boolean clear) throws IOException {
    DdlOptions options = new DdlOptions(addForeignKeySkipCheck);
    for (Pair pair : platforms) {
      DdlWrite writer = new DdlWrite(new MConfiguration(), currentModel.read(), options);
      PlatformDdlWriter platformWriter = createDdlWriter(pair.platform);
      File subPath = platformWriter.subPath(writePath, pair.prefix);
      if (clear) {
        for (File existing : subPath.listFiles()) {
          existing.delete();
        }
      }
      platformWriter.processMigration(dbMigration, writer, subPath, fullVersion);
    }
  }

  private PlatformDdlWriter createDdlWriter(DatabasePlatform platform) {
    return new PlatformDdlWriter(platform, databaseBuilder, lockTimeoutSeconds);
  }

  /**
   * Write the migration xml.
   */
  private boolean writeMigrationXml(Migration dbMigration, File resourcePath, String fullVersion) {
    String modelFile = fullVersion + modelSuffix;
    File file = new File(resourcePath, modelFile);
    if (file.exists()) {
      return false;
    }
    String comment = includeGeneratedFileComment ? GENERATED_COMMENT : null;
    MigrationXmlWriter xmlWriter = new MigrationXmlWriter(comment);
    xmlWriter.write(dbMigration, file);
    return true;
  }

  /**
   * Set default server and platform if necessary.
   */
  private void setDefaults() {
    if (server == null) {
      setServer(DB.getDefault());
    }
    if (vanillaPlatform || databasePlatform == null) {
      // not explicitly set so use the platform of the server
      databasePlatform = server.databasePlatform();
    }
    if (databaseBuilder != null) {
      // FIXME: StrictMode and header may be defined HERE and in DatabaseConfig.
      //  We shoild change either DefaultDbMigration or databaseConfig, so that it is only
      //  defined on one place
      if (strictMode != null) {
        databaseBuilder.ddlStrictMode(strictMode);
      }
      if (header != null) {
        databaseBuilder.ddlHeader(header);
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
  private String version() {
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
  private String name() {
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

  @Override
  public File migrationDirectory() {
    return migrationDirectory(false);
  }

  /**
   * Return the file path to write the xml and sql to.
   */
  File migrationDirectory(boolean initMigration) {
    Location resourcePath = migrationPath(initMigration);
    // expect to be a path to something like - src/main/resources/dbmigration
    File path;
    if (resourcePath.isClassPath()) {
      // path to src/main/resources in typical maven project
      File resourceRootDir = new File(pathToResources);
      if (!resourceRootDir.exists()) {
        String msg = String.format("Error - path to resources %s does not exist. Absolute path is %s", pathToResources, resourceRootDir.getAbsolutePath());
        throw new UnknownResourcePathException(msg);
      }
      path = new File(resourceRootDir, resourcePath.path());
    } else {
      path = new File(resourcePath.path());
    }
    if (!path.exists()) {
      if (!path.mkdirs()) {
        logInfo("Warning - Unable to ensure migration directory exists at %s", path.getAbsolutePath());
      }
    }
    return path;
  }

  private Location migrationPath(boolean initMigration) {
    // remove classpath: or filesystem: prefix
    return new Location(initMigration ? migrationInitPath : migrationPath);
  }

  /**
   * Return the model directory (relative to the migration directory).
   */
  private File modelDirectory(File migrationDirectory) {
    if (modelPath == null || modelPath.isEmpty()) {
      return migrationDirectory;
    }
    File modelDir = new File(migrationDirectory, modelPath);
    if (!modelDir.exists() && !modelDir.mkdirs()) {
      logInfo("Warning - Unable to ensure migration model directory exists at %s", modelDir.getAbsolutePath());
    }
    return modelDir;
  }

  /**
   * Return the DatabasePlatform given the platform key.
   */
  protected DatabasePlatform platform(Platform platform) {
    switch (platform) {
      case SQLSERVER:
        throw new IllegalArgumentException("Please choose the more specific SQLSERVER16 or SQLSERVER17 platform. Refer to issue #1340 for details");
      case DB2:
        logger.log(WARNING, "Using DB2LegacyPlatform. It is recommended to migrate to db2luw/db2zos/db2fori. Refer to issue #2514 for details");
      case GENERIC:
        return new DatabasePlatform();
      default:
        for (DatabasePlatformProvider platformProvider : platformProviders) {
          if (platformProvider.matchPlatform(platform)) {
            return platformProvider.create(platform);
          }
        }
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

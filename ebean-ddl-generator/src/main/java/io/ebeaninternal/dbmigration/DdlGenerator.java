package io.ebeaninternal.dbmigration;

import io.ebean.annotation.Platform;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.ddlrunner.DdlRunner;
import io.ebean.ddlrunner.ScriptTransform;
import io.ebean.util.JdbcClose;
import io.ebeaninternal.api.SpiDdlGenerator;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.dbmigration.model.CurrentModel;
import io.ebeaninternal.dbmigration.model.MTable;
import io.ebeaninternal.extraddl.model.ExtraDdlXmlReader;
import io.ebeaninternal.server.deploy.PartitionMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Controls the generation and execution of "Create All" and "Drop All" DDL scripts.
 * <p>
 * Typically the "Create All" DDL is executed for running tests etc and has nothing to do
 * with DB Migration (diff based) DDL.
 */
public class DdlGenerator implements SpiDdlGenerator {

  private static final Logger log = LoggerFactory.getLogger(DdlGenerator.class);
  private static final String[] BUILD_DIRS = {"target", "build"};

  private final SpiEbeanServer server;

  private final boolean generateDdl;
  private final boolean runDdl;
  private final boolean extraDdl;
  private final boolean createOnly;
  private final boolean jaxbPresent;
  private final boolean ddlAutoCommit;
  private final String dbSchema;
  private final ScriptTransform scriptTransform;
  private final Platform platform;
  private final String platformName;

  private CurrentModel currentModel;
  private String dropAllContent;
  private String createAllContent;
  private final File baseDir;

  public DdlGenerator(SpiEbeanServer server) {
    this.server = server;
    final DatabaseConfig config = server.getServerConfig();
    this.jaxbPresent = Detect.isJAXBPresent(config);
    this.generateDdl = config.isDdlGenerate();
    this.extraDdl = config.isDdlExtra();
    this.createOnly = config.isDdlCreateOnly();
    this.dbSchema = config.getDbSchema();
    final DatabasePlatform databasePlatform = server.getDatabasePlatform();
    this.platform = databasePlatform.getPlatform();
    this.platformName = platform.base().name();
    if (!config.getTenantMode().isDdlEnabled() && config.isDdlRun()) {
      log.warn("DDL can't be run on startup with TenantMode " + config.getTenantMode());
      this.runDdl = false;
      this.ddlAutoCommit = false;
    } else {
      this.runDdl = config.isDdlRun();
      this.ddlAutoCommit = databasePlatform.isDdlAutoCommit();
    }
    this.scriptTransform = createScriptTransform(config);
    this.baseDir = initBaseDir();
  }

  private File initBaseDir() {
    for (String buildDir : BUILD_DIRS) {
      File dir = new File(buildDir);
      if (dir.exists() && dir.isDirectory()) {
        return dir;
      }
    }
    return new File(".");
  }

  @Override
  public void execute(boolean online) {
    generateDdl();
    if (online) {
      runDdl();
    }
  }

  /**
   * Generate the DDL drop and create scripts if the properties have been set.
   */
  protected void generateDdl() {
    if (generateDdl) {
      if (!createOnly) {
        writeDrop(getDropFileName());
      }
      writeCreate(getCreateFileName());
    }
  }

  /**
   * Run the DDL drop and DDL create scripts if properties have been set.
   */
  protected void runDdl() {
    if (runDdl) {
      Connection connection = null;
      try {
        connection = obtainConnection();
        runDdlWith(connection);
      } finally {
        JdbcClose.rollback(connection);
        JdbcClose.close(connection);
      }
    }
  }

  private void runDdlWith(Connection connection) {
    try {
      if (dbSchema != null) {
        createSchemaIfRequired(connection);
      }
      runInitSql(connection);
      runDropSql(connection);
      runCreateSql(connection);
      runSeedSql(connection);
    } catch (IOException e) {
      throw new RuntimeException("Error reading drop/create script from file system", e);
    }
  }

  private Connection obtainConnection() {
    try {
      return server.getDataSource().getConnection();
    } catch (SQLException e) {
      throw new PersistenceException("Failed to obtain connection to run DDL", e);
    }
  }

  private void createSchemaIfRequired(Connection connection) {
    try {
      for (String schema : dbSchema.split(",")) {
        server.getDatabasePlatform().createSchemaIfNotExists(schema, connection);
      }
    } catch (SQLException e) {
      throw new PersistenceException("Failed to create DB Schema", e);
    }
  }

  /**
   * Execute all the DDL statements in the script.
   */
  void runScript(Connection connection, boolean expectErrors, String content, String scriptName) {

    DdlRunner runner = createDdlRunner(expectErrors, scriptName);
    try {
      if (expectErrors || ddlAutoCommit) {
        connection.setAutoCommit(true);
      }
      runner.runAll(scriptTransform.transform(content), connection);
      if (expectErrors || ddlAutoCommit) {
        connection.setAutoCommit(false);
      }
      connection.commit();
      runner.runNonTransactional(connection);

    } catch (SQLException e) {
      throw new PersistenceException("Failed to run script", e);
    } finally {
      JdbcClose.rollback(connection);
    }
  }

  private DdlRunner createDdlRunner(boolean expectErrors, String scriptName) {
    return new DdlRunner(expectErrors, scriptName, platformName);
  }

  protected void runDropSql(Connection connection) throws IOException {
    if (!createOnly) {
      if (extraDdl && jaxbPresent) {
        String extraApply = ExtraDdlXmlReader.buildExtra(platform, true);
        if (extraApply != null) {
          runScript(connection, false, extraApply, "extra-ddl");
        }
      }

      if (dropAllContent == null) {
        dropAllContent = readFile(getDropFileName());
      }
      runScript(connection, true, dropAllContent, getDropFileName());
    }
  }

  protected void runCreateSql(Connection connection) throws IOException {
    if (createAllContent == null) {
      createAllContent = readFile(getCreateFileName());
    }
    runScript(connection, false, createAllContent, getCreateFileName());

    if (extraDdl && jaxbPresent) {
      if (currentModel().isTablePartitioning()) {
        String extraPartitioning = ExtraDdlXmlReader.buildPartitioning(platform);
        if (extraPartitioning != null && !extraPartitioning.isEmpty()) {
          runScript(connection, false, extraPartitioning, "builtin-partitioning-ddl");
        }
      }

      String extraApply = ExtraDdlXmlReader.buildExtra(platform, false);
      if (extraApply != null) {
        runScript(connection, false, extraApply, "extra-ddl");
      }

      if (currentModel().isTablePartitioning()) {
        checkInitialTablePartitions(connection);
      }
    }
  }

  /**
   * Check if table partitions exist and if not create some. The expectation is that
   * extra-ddl.xml should have some partition initialisation but this helps people get going.
   */
  private void checkInitialTablePartitions(Connection connection) {
    DatabasePlatform databasePlatform = server.getDatabasePlatform();
    try {
      StringBuilder sb = new StringBuilder();
      for (MTable table : currentModel.getPartitionedTables()) {
        String tableName = table.getName();
        if (!databasePlatform.tablePartitionsExist(connection, tableName)) {
          log.info("No table partitions for table {}", tableName);
          PartitionMeta meta = table.getPartitionMeta();
          String initPart = databasePlatform.tablePartitionInit(tableName, meta.getMode(), meta.getProperty(), table.singlePrimaryKey());
          sb.append(initPart).append("\n");
        }
      }

      String initialPartitionSql = sb.toString();
      if (!initialPartitionSql.isEmpty()) {
        runScript(connection, false, initialPartitionSql, "initial table partitions");
      }

    } catch (SQLException e) {
      log.error("Error checking initial table partitions", e);
    }
  }

  protected void runInitSql(Connection connection) throws IOException {
    runResourceScript(connection, server.getServerConfig().getDdlInitSql());
  }

  protected void runSeedSql(Connection connection) throws IOException {
    runResourceScript(connection, server.getServerConfig().getDdlSeedSql());
  }

  protected void runResourceScript(Connection connection, String sqlScript) throws IOException {
    if (sqlScript != null) {
      try (InputStream is = getClassLoader().getResourceAsStream(sqlScript)) {
        if (is == null) {
          log.warn("sql script {} was not found as a resource", sqlScript);
        } else {
          String content = readContent(new InputStreamReader(is));
          runScript(connection, false, content, sqlScript);
        }
      }
    }
  }

  /**
   * Return the classLoader to use to read sql scripts as resources.
   */
  protected ClassLoader getClassLoader() {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    if (cl == null) {
      cl = this.getClassLoader();
    }
    return cl;
  }

  protected void writeDrop(String dropFile) {
    try {
      writeFile(dropFile, generateDropAllDdl());
    } catch (IOException e) {
      throw new PersistenceException("Error generating Drop DDL", e);
    }
  }

  protected void writeCreate(String createFile) {
    try {
      writeFile(createFile, generateCreateAllDdl());
    } catch (IOException e) {
      throw new PersistenceException("Error generating Create DDL", e);
    }
  }

  protected String generateDropAllDdl() {
    try {
      dropAllContent = currentModel().getDropAllDdl();
      return dropAllContent;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected String generateCreateAllDdl() {
    try {
      createAllContent = currentModel().getCreateDdl();
      return createAllContent;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected String getDropFileName() {
    return server.getName() + "-drop-all.sql";
  }

  protected String getCreateFileName() {
    return server.getName() + "-create-all.sql";
  }

  protected CurrentModel currentModel() {
    if (currentModel == null) {
      currentModel = new CurrentModel(server);
    }
    return currentModel;
  }

  protected void writeFile(String fileName, String fileContent) throws IOException {
    File f = new File(baseDir, fileName);
    try (FileWriter fw = new FileWriter(f)) {
      fw.write(fileContent);
      fw.flush();
    }
  }

  protected String readFile(String fileName) throws IOException {
    File f = new File(baseDir, fileName);
    if (!f.exists()) {
      return null;
    }
    return readContent(new FileReader(f));
  }

  protected String readContent(Reader reader) throws IOException {
    StringBuilder buf = new StringBuilder();
    try (LineNumberReader lineReader = new LineNumberReader(reader)) {
      String s;
      while ((s = lineReader.readLine()) != null) {
        buf.append(s).append("\n");
      }
      return buf.toString();
    }
  }

  /**
   * Create the ScriptTransform for placeholder key/value replacement.
   */
  private ScriptTransform createScriptTransform(DatabaseConfig config) {
    return ScriptTransform.build(config.getDdlPlaceholders(), config.getDdlPlaceholderMap());
  }

}

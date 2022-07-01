package io.ebeaninternal.dbmigration;

import io.ebean.annotation.Platform;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.ddlrunner.DdlRunner;
import io.ebean.ddlrunner.ScriptTransform;
import io.ebean.plugin.Plugin;
import io.ebean.plugin.SpiServer;
import io.ebean.util.IOUtils;
import io.ebean.util.JdbcClose;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.dbmigration.model.CurrentModel;
import io.ebeaninternal.extraddl.model.ExtraDdlXmlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Controls the generation and execution of "Create All" and "Drop All" DDL scripts.
 * <p>
 * Typically the "Create All" DDL is executed for running tests etc and has nothing to do
 * with DB Migration (diff based) DDL.
 */
public class DdlPlugin implements Plugin {

  private static final Logger log = LoggerFactory.getLogger(DdlPlugin.class);
  private static final String[] BUILD_DIRS = {"target", "build"};

  private SpiEbeanServer server;

  private boolean generateDdl;
  private boolean runDdl;
  private boolean extraDdl;
  private boolean createOnly;
  private boolean jaxbPresent;
  private String dbSchema;
  private ScriptTransform scriptTransform;
  private Platform platform;
  private String platformName;
  private boolean useMigrationStoredProcedures;

  private CurrentModel currentModel;
  private String dropAllContent;
  private String createAllContent;
  private File baseDir;
  
  @Override
  public void configure(SpiServer server) {
    this.server = (SpiEbeanServer) server;
    final DatabaseConfig config = server.config();
    this.generateDdl = config.isDdlGenerate();
    this.runDdl = config.isDdlRun() && !config.isDocStoreOnly();
    if (generateDdl || runDdl) {
      this.jaxbPresent = Detect.isJAXBPresent(config);
      this.extraDdl = config.isDdlExtra();
      this.createOnly = config.isDdlCreateOnly();
      this.dbSchema = config.getDbSchema();
      final DatabasePlatform databasePlatform = server.databasePlatform();
      this.platform = databasePlatform.getPlatform();
      this.platformName = platform.base().name();
      this.useMigrationStoredProcedures = config.getDatabasePlatform().isUseMigrationStoredProcedures();
      this.scriptTransform = createScriptTransform(config);
      this.baseDir = initBaseDir();
    }   
  }

  /**
   * Generate the DDL drop and create scripts if the properties have been set.
   */
  @Override
  public void online(boolean online) {
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
  @Override
  public void start() {
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

  @Override
  public void shutdown() {}


  private File initBaseDir() {
    for (String buildDir : BUILD_DIRS) {
      File dir = new File(buildDir);
      if (dir.exists() && dir.isDirectory()) {
        return dir;
      }
    }
    return new File(".");
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
      return server.dataSource().getConnection();
    } catch (SQLException e) {
      throw new PersistenceException("Failed to obtain connection to run DDL", e);
    }
  }

  private void createSchemaIfRequired(Connection connection) {
    try {
      for (String schema : dbSchema.split(",")) {
        server.databasePlatform().createSchemaIfNotExists(schema, connection);
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
      if (expectErrors) {
        connection.setAutoCommit(true);
      }
      runner.runAll(scriptTransform.transform(content), connection);
      if (expectErrors) {
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
      if (extraDdl && jaxbPresent && useMigrationStoredProcedures) {
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
        if (extraPartitioning != null && !extraPartitioning.isEmpty()  && useMigrationStoredProcedures) {
          runScript(connection, false, extraPartitioning, "builtin-partitioning-ddl");
        }
      }
      String extraApply = ExtraDdlXmlReader.buildExtra(platform, false);
      if (extraApply != null) {
        runScript(connection, false, extraApply, "extra-ddl");
      }
    }
  }

  protected void runInitSql(Connection connection) throws IOException {
    runResourceScript(connection, server.config().getDdlInitSql());
  }

  protected void runSeedSql(Connection connection) throws IOException {
    runResourceScript(connection, server.config().getDdlSeedSql());
  }

  protected void runResourceScript(Connection connection, String sqlScript) throws IOException {
    if (sqlScript != null) {
      try (InputStream is = getClassLoader().getResourceAsStream(sqlScript)) {
        if (is == null) {
          log.warn("sql script {} was not found as a resource", sqlScript);
        } else {
          String content = readContent(IOUtils.newReader(is)); // 'is' is closed
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
    dropAllContent = currentModel().getDropAllDdl();
    return dropAllContent;
  }

  protected String generateCreateAllDdl() {
    createAllContent = currentModel().getCreateDdl();
    return createAllContent;
  }

  protected String getDropFileName() {
    return server.name() + "-drop-all.sql";
  }

  protected String getCreateFileName() {
    return server.name() + "-create-all.sql";
  }

  protected CurrentModel currentModel() {
    if (currentModel == null) {
      currentModel = new CurrentModel(server);
    }
    return currentModel;
  }

  protected void writeFile(String fileName, String fileContent) throws IOException {
    File f = new File(baseDir, fileName);
    try (Writer fw = IOUtils.newWriter(f)) {
      fw.write(fileContent);
      fw.flush();
    }
  }

  protected String readFile(String fileName) throws IOException {
    File f = new File(baseDir, fileName);
    if (!f.exists()) {
      return null;
    }
    try (Reader reader = IOUtils.newReader(f)) {
      return readContent(reader);
    }
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

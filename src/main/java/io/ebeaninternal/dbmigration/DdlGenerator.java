package io.ebeaninternal.dbmigration;

import io.ebean.Transaction;
import io.ebean.config.ServerConfig;
import io.ebeaninternal.dbmigration.model.CurrentModel;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.extraddl.model.ExtraDdlXmlReader;
import io.ebean.migration.ddl.DdlRunner;
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
public class DdlGenerator {

  private static final Logger log = LoggerFactory.getLogger(DdlGenerator.class);

  private final SpiEbeanServer server;

  private final boolean generateDdl;
  private final boolean runDdl;
  private final boolean createOnly;
  private final boolean jaxbPresent;
  private final boolean ddlCommitOnCreateIndex;

  private CurrentModel currentModel;
  private String dropAllContent;
  private String createAllContent;

  public DdlGenerator(SpiEbeanServer server, ServerConfig serverConfig) {
    this.server = server;
    this.jaxbPresent = serverConfig.getClassLoadConfig().isJavaxJAXBPresent();
    this.generateDdl = serverConfig.isDdlGenerate();
    this.createOnly = serverConfig.isDdlCreateOnly();
    if (!serverConfig.getTenantMode().isDdlEnabled() && serverConfig.isDdlRun()) {
      log.warn("DDL can't be run on startup with TenantMode " + serverConfig.getTenantMode());
      this.runDdl = false;
      this.ddlCommitOnCreateIndex = false;
    } else {
      this.runDdl = serverConfig.isDdlRun();
      this.ddlCommitOnCreateIndex = server.getDatabasePlatform().isDdlCommitOnCreateIndex();
    }
  }

  /**
   * Generate the DDL and then run the DDL based on property settings
   * (ebean.ddl.generate and ebean.ddl.run etc).
   */
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
      try {
        runInitSql();
        runDropSql();
        runCreateSql();
        runSeedSql();

      } catch (IOException e) {
        String msg = "Error reading drop/create script from file system";
        throw new RuntimeException(msg, e);
      }
    }
  }

  /**
   * Execute all the DDL statements in the script.
   */
  public int runScript(boolean expectErrors, String content, String scriptName) {

    DdlRunner runner = new DdlRunner(expectErrors, scriptName);

    Transaction transaction = server.createTransaction();
    Connection connection = transaction.getConnection();
    try {
      if (expectErrors) {
        connection.setAutoCommit(true);
      } else if (ddlCommitOnCreateIndex) {
        runner.setCommitOnCreateIndex();
      }
      int count = runner.runAll(content, connection);
      if (expectErrors) {
        connection.setAutoCommit(false);
      }
      transaction.commit();
      return count;

    } catch (SQLException e) {
      throw new PersistenceException("Failed to run script", e);

    } finally {
      transaction.end();
    }
  }

  protected void runDropSql() throws IOException {
    if (!createOnly) {
      String ignoreExtraDdl = System.getProperty("ebean.ignoreExtraDdl");
      if (!"true".equalsIgnoreCase(ignoreExtraDdl) && jaxbPresent) {
        String extraApply = ExtraDdlXmlReader.buildExtra(server.getDatabasePlatform().getName(), true);
        if (extraApply != null) {
          runScript(false, extraApply, "extra-dll");
        }
      }

      if (dropAllContent == null) {
        dropAllContent = readFile(getDropFileName());
      }
      runScript(true, dropAllContent, getDropFileName());
    }
  }

  protected void runCreateSql() throws IOException {
    if (createAllContent == null) {
      createAllContent = readFile(getCreateFileName());
    }
    runScript(false, createAllContent, getCreateFileName());

    String ignoreExtraDdl = System.getProperty("ebean.ignoreExtraDdl");
    if (!"true".equalsIgnoreCase(ignoreExtraDdl) && jaxbPresent) {
      String extraApply = ExtraDdlXmlReader.buildExtra(server.getDatabasePlatform().getName(), false);
      if (extraApply != null) {
        runScript(false, extraApply, "extra-dll");
      }
    }
  }

  protected void runInitSql() throws IOException {
    runResourceScript(server.getServerConfig().getDdlInitSql());
  }

  protected void runSeedSql() throws IOException {
    runResourceScript(server.getServerConfig().getDdlSeedSql());
  }

  protected void runResourceScript(String sqlScript) throws IOException {

    if (sqlScript != null) {
      InputStream is = getClassLoader().getResourceAsStream(sqlScript);
      if (is != null) {
        String content = readContent(new InputStreamReader(is));
        runScript(false, content, sqlScript);
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

    File f = new File(fileName);
    try (FileWriter fw = new FileWriter(f)) {
      fw.write(fileContent);
      fw.flush();
    }
  }

  protected String readFile(String fileName) throws IOException {

    File f = new File(fileName);
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

}

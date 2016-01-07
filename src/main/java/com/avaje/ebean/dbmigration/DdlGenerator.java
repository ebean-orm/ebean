package com.avaje.ebean.dbmigration;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.dbmigration.model.CurrentModel;
import com.avaje.ebeaninternal.api.SpiEbeanPlugin;
import com.avaje.ebeaninternal.api.SpiEbeanServer;

import javax.persistence.PersistenceException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;

/**
 * Controls the generation of DDL and potentially runs the resulting scripts.
 */
public class DdlGenerator implements SpiEbeanPlugin {

  private SpiEbeanServer server;

  private boolean generateDdl;
  private boolean runDdl;
  private boolean createOnly;

  private CurrentModel currentModel;
  private String dropContent;
  private String createContent;

  public void setup(SpiEbeanServer server, ServerConfig serverConfig) {
    this.server = server;
    this.generateDdl = serverConfig.isDdlGenerate();
    this.runDdl = serverConfig.isDdlRun();
    this.createOnly = serverConfig.isDdlCreateOnly();
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
  public void generateDdl() {
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
  public void runDdl() {

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

  protected void runDropSql() throws IOException {
    if (!createOnly) {
      if (dropContent == null) {
        dropContent = readFile(getDropFileName());
      }
      runScript(true, dropContent, getDropFileName());
    }
  }

  protected void runCreateSql() throws IOException {
    if (createContent == null) {
      createContent = readFile(getCreateFileName());
    }
    runScript(false, createContent, getCreateFileName());
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
        DdlRunner runner = new DdlRunner(false, sqlScript);
        String content = readContent(new InputStreamReader(is));
        runner.runAll(content, server);
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
      String c = generateDropDdl();
      writeFile(dropFile, c);
    } catch (IOException e) {
      throw new PersistenceException("Error generating Drop DDL", e);
    }
  }

  protected void writeCreate(String createFile) {

    try {
      String c = generateCreateDdl();
      writeFile(createFile, c);
    } catch (IOException e) {
      throw new PersistenceException("Error generating Create DDL", e);
    }
  }

  public String generateDropDdl() {

    try {
      dropContent = currentModel().getDropDdl();
      return dropContent;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String generateCreateDdl() {

    try {
      createContent = currentModel().getCreateDdl();
      return createContent;
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

    FileWriter fw = new FileWriter(f);
    try {
      fw.write(fileContent);
      fw.flush();
    } finally {
      fw.close();
    }
  }

  protected String readFile(String fileName) throws IOException {

    File f = new File(fileName);
    if (!f.exists()) {
      return null;
    }

    return readContent(new FileReader(f));
  }

  private String readContent(Reader reader) throws IOException {

    StringBuilder buf = new StringBuilder();

    LineNumberReader lineReader = new LineNumberReader(reader);
    try {
      String s;
      while ((s = lineReader.readLine()) != null) {
        buf.append(s).append("\n");
      }
      return buf.toString();

    } finally {
      lineReader.close();
    }
  }

  /**
   * Execute all the DDL statements in the script.
   */
  public int runScript(boolean expectErrors, String content, String scriptName) {

    DdlRunner runner = new DdlRunner(expectErrors, scriptName);
    return runner.runAll(content, server);
  }

}

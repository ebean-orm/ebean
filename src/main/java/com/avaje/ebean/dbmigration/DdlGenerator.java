package com.avaje.ebean.dbmigration;

import com.avaje.ebean.Transaction;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.dbmigration.model.CurrentModel;
import com.avaje.ebeaninternal.api.SpiEbeanPlugin;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controls the generation of DDL and potentially runs the resulting scripts.
 */
public class DdlGenerator implements SpiEbeanPlugin {

  private static final Logger logger = LoggerFactory.getLogger(DdlGenerator.class);

  private SpiEbeanServer server;

  private boolean generateDdl;
  private boolean runDdl;

  private CurrentModel currentModel;
  private String dropContent;
  private String createContent;

  public void setup(SpiEbeanServer server, ServerConfig serverConfig) {
    this.server = server;
    this.generateDdl = serverConfig.isDdlGenerate();
    this.runDdl = serverConfig.isDdlRun();
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
      writeDrop(getDropFileName());
      writeCreate(getCreateFileName());
    }
  }

  /**
   * Run the DDL drop and DDL create scripts if properties have been set.
   */
  public void runDdl() {

    if (runDdl) {
      try {
        if (dropContent == null) {
          dropContent = readFile(getDropFileName());
        }
        if (createContent == null) {
          createContent = readFile(getCreateFileName());
        }
        runScript(true, dropContent);
        runScript(false, createContent);

      } catch (IOException e) {
        String msg = "Error reading drop/create script from file system";
        throw new RuntimeException(msg, e);
      }
    }
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

    StringBuilder buf = new StringBuilder();

    FileReader fr = new FileReader(f);
    LineNumberReader lr = new LineNumberReader(fr);
    try {
      String s;
      while ((s = lr.readLine()) != null) {
        buf.append(s).append("\n");
      }
    } finally {
      lr.close();
    }

    return buf.toString();
  }

  /**
   * Execute all the DDL statements in the script.
   */
  public void runScript(boolean expectErrors, String content) {

    StringReader sr = new StringReader(content);
    List<String> statements = parseStatements(sr);

    Transaction t = server.createTransaction();
    try {
      Connection connection = t.getConnection();

      logger.info("Running DDL");

      runStatements(expectErrors, statements, connection);

      logger.info("Running DDL Complete");

      t.commit();

    } catch (Exception e) {
      throw new PersistenceException("Error: " + e.getMessage(), e);
    } finally {
      t.end();
    }
  }

  /**
   * Execute the list of statements.
   */
  private void runStatements(boolean expectErrors, List<String> statements, Connection c) {
    List<String> noDuplicates = new ArrayList<String>();

    for (String statement : statements) {
      if (!noDuplicates.contains(statement)) {
        noDuplicates.add(statement);
      }
    }

    for (int i = 0; i < noDuplicates.size(); i++) {
      String xOfy = (i + 1) + " of " + noDuplicates.size();
      runStatement(expectErrors, xOfy, noDuplicates.get(i), c);
    }
  }

  /**
   * Execute the statement.
   */
  private void runStatement(boolean expectErrors, String oneOf, String stmt, Connection c) {

    PreparedStatement pstmt = null;
    try {

      // trim and remove trailing ; or /
      stmt = stmt.trim();
      if (stmt.endsWith(";")) {
        stmt = stmt.substring(0, stmt.length() - 1);
      } else if (stmt.endsWith("/")) {
        stmt = stmt.substring(0, stmt.length() - 1);
      }

      logger.info("executing " + oneOf + " " + getSummary(stmt));

      pstmt = c.prepareStatement(stmt);
      pstmt.execute();

    } catch (Exception e) {
      if (expectErrors) {
        logger.info(" ... ignoring error executing " + getSummary(stmt) + "  error: " + e.getMessage());
      } else {
        String msg = "Error executing stmt[" + stmt + "] error[" + e.getMessage() + "]";
        throw new RuntimeException(msg, e);
      }
    } finally {
      if (pstmt != null) {
        try {
          pstmt.close();
        } catch (SQLException e) {
          logger.error("Error closing pstmt", e);
        }
      }
    }
  }

  /**
   * Local utility used to detect the end of statements / separate statements.
   * This is often just the semicolon character but for trigger/procedures this
   * detects the $$ demarcation used in the history DDL generation for MySql and
   * Postgres.
   */
  static class StatementsSeparator {

    ArrayList<String> statements = new ArrayList<String>();

    boolean trimDelimiter;

    boolean inDbProcedure;

    StringBuilder sb = new StringBuilder();

    void lineContainsDollars(String line) {
      if (inDbProcedure) {
        if (trimDelimiter) {
          line = line.replace("$$","");
        }
        endOfStatement(line);
      } else {
        // MySql style delimiter needs to be trimmed/removed
        trimDelimiter = line.equals("delimiter $$");
        if (!trimDelimiter) {
          sb.append(line).append(" ");
        }
      }
      inDbProcedure = !inDbProcedure;
    }

    void endOfStatement(String line) {
      // end of Db procedure
      sb.append(line);
      statements.add(sb.toString().trim());
      sb = new StringBuilder();
    }

    void nextLine(String line) {

      if (line.contains("$$")) {
        lineContainsDollars(line);
        return;
      }

      if (inDbProcedure) {
        sb.append(line).append(" ");
        return;
      }

      int semiPos = line.indexOf(';');
      if (semiPos == -1) {
        sb.append(line).append(" ");

      } else if (semiPos == line.length() - 1) {
        // semicolon at end of line
        endOfStatement(line);

      } else {
        // semicolon in middle of line
        String preSemi = line.substring(0, semiPos);
        endOfStatement(preSemi);
        sb.append(line.substring(semiPos + 1));
      }
    }
  }

  /**
   * Break up the sql in reader into a list of statements using the semi-colon
   * character;
   */
  protected List<String> parseStatements(StringReader reader) {

    try {
      BufferedReader br = new BufferedReader(reader);
      StatementsSeparator statements = new StatementsSeparator();

      String s;
      while ((s = br.readLine()) != null) {
        s = s.trim();
        statements.nextLine(s);
      }

      return statements.statements;

    } catch (IOException e) {
      throw new PersistenceException(e);
    }
  }

  private String getSummary(String s) {
    if (s.length() > 80) {
      return s.substring(0, 80).trim() + "...";
    }
    return s;
  }
}

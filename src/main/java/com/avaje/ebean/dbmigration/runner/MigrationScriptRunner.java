package com.avaje.ebean.dbmigration.runner;

import com.avaje.ebean.dbmigration.DdlRunner;

import java.sql.Connection;

/**
 * Runs the DDL migration scripts.
 */
public class MigrationScriptRunner {

  private final Connection connection;

  /**
   * Construct with a given connection.
   */
  public MigrationScriptRunner(Connection connection) {
    this.connection = connection;
  }

  /**
   * Execute all the DDL statements in the script.
   */
  public int runScript(boolean expectErrors, String content, String scriptName) {

    DdlRunner runner = new DdlRunner(expectErrors, scriptName);
    return runner.runAll(content, connection);
  }
}

package com.avaje.ebean.dbmigration.runner;

import com.avaje.ebean.dbmigration.DdlRunner;

import java.sql.Connection;

/**
 * Created by rob on 5/02/16.
 */
public class MigrationScriptRunner {

  final Connection connection;

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

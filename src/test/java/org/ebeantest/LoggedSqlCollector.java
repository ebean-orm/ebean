package org.ebeantest;

import io.ebeantest.LoggedSql;

import java.util.List;

/**
 * Helper that can collect the SQL that is logged via SLF4J.
 * <p>
 * Used {@link #start()} and {@link #stop()} to collect the logged messages that contain the
 * executed SQL statements.
 * <p>
 * Internally this uses a Logback Appender to collect messages for org.avaje.ebean.SQL.
 */
public class LoggedSqlCollector {

  /**
   * Start collection of the logged SQL statements.
   */
  public static List<String> start() {
    return LoggedSql.start();
  }

  /**
   * Stop collection of the logged SQL statements return the list of captured messages that contain
   * the SQL.
   */
  public static List<String> stop() {
    return LoggedSql.stop();
  }

  public static List<String> current() {
    return LoggedSql.collect();
  }

}

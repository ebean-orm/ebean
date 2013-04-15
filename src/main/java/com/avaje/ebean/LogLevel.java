package com.avaje.ebean;

/**
 * The transaction log level.
 * <p>
 * This is used to define how much Ebean should log such as generated SQL.
 * </p>
 */
public enum LogLevel {

  /**
   * No logging.
   */
  NONE,

  /**
   * Log only a summary level.
   */
  SUMMARY,

  /**
   * Log generated SQL/DML and binding variables.
   */
  SQL
}

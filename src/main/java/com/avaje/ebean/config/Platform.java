package com.avaje.ebean.config;

/**
 * Built in supported platforms.
 */
public enum Platform {

  /**
   * Generic base platform configured via properties or code.
   */
  GENERIC,

  /**
   * H2.
   */
  H2,

  /**
   * Postgres.
   */
  POSTGRES,

  /**
   * MySql.
   */
  MYSQL,

  /**
   * Oracle.
   */
  ORACLE,

  /**
   * Microsoft SQL Server.
   */
  SQLSERVER,

  /**
   * DB2.
   */
  DB2,

  /**
   * SQLite.
   */
  SQLITE
}

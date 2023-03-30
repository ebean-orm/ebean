package io.ebean.platform.db2;

import io.ebean.annotation.Platform;

/**
 * DB2 platform for Linux/Unix/Windows version 9.x.
 */
public class DB2Luw9Platform extends BaseDB2Platform {
  public DB2Luw9Platform() {
    super();
    this.platform = Platform.DB2LUW; // TODO: Add a DB2LUW9
    this.basicSqlLimiter = new DB2RowNumberBasicLimiter();
    this.sqlLimiter = new DB2RowNumberSqlLimiter();
  }
}

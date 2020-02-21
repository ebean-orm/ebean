package io.ebean.config.dbplatform.oracle;

import io.ebean.annotation.Platform;

/**
 * Oracle 11 platform using rownum sql limiting.
 */
public class Oracle11Platform extends OraclePlatform {

  public Oracle11Platform() {
    super();
    this.platform = Platform.ORACLE11;
    this.columnAliasPrefix = "c";
    this.sqlLimiter = new OracleRownumSqlLimiter();
  }
}

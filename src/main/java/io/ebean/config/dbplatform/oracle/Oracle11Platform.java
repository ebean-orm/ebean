package io.ebean.config.dbplatform.oracle;

/**
 * Oracle 11 platform using rownum sql limiting.
 */
public class Oracle11Platform extends OraclePlatform {

  public Oracle11Platform() {
    super();
    this.columnAliasPrefix = "c";
    this.sqlLimiter = new OracleRownumSqlLimiter();
  }
}

package com.avaje.ebeaninternal.server.querydefn;

import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.config.dbplatform.SqlLimitRequest;
import com.avaje.ebeaninternal.api.SpiQuery;

public class OrmQueryLimitRequest implements SqlLimitRequest {

  private final SpiQuery<?> ormQuery;

  private final DatabasePlatform dbPlatform;

  private final String sql;

  private final String sqlOrderBy;

  public OrmQueryLimitRequest(String sql, String sqlOrderBy, SpiQuery<?> ormQuery, DatabasePlatform dbPlatform) {
    this.sql = sql;
    this.sqlOrderBy = sqlOrderBy;
    this.ormQuery = ormQuery;
    this.dbPlatform = dbPlatform;
  }

  public String getDbOrderBy() {
    return sqlOrderBy;
  }

  public String getDbSql() {
    return sql;
  }

  public int getFirstRow() {
    return ormQuery.getFirstRow();
  }

  public int getMaxRows() {
    return ormQuery.getMaxRows();
  }

  public boolean isDistinct() {
    return ormQuery.isDistinctQuery();
  }

  public SpiQuery<?> getOrmQuery() {
    return ormQuery;
  }

  public DatabasePlatform getDbPlatform() {
    return dbPlatform;
  }
}

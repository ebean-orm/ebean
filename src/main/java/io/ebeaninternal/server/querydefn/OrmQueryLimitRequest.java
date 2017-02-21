package io.ebeaninternal.server.querydefn;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.SqlLimitRequest;
import io.ebeaninternal.api.SpiQuery;

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

  @Override
  public String getDbOrderBy() {
    return sqlOrderBy;
  }

  @Override
  public String getDbSql() {
    return sql;
  }

  @Override
  public int getFirstRow() {
    return ormQuery.getFirstRow();
  }

  @Override
  public int getMaxRows() {
    return ormQuery.getMaxRows();
  }

  @Override
  public boolean isDistinct() {
    return ormQuery.isDistinctQuery();
  }

  @Override
  public SpiQuery<?> getOrmQuery() {
    return ormQuery;
  }

  @Override
  public DatabasePlatform getDbPlatform() {
    return dbPlatform;
  }
}

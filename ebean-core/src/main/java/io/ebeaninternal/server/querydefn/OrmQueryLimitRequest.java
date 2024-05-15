package io.ebeaninternal.server.querydefn;

import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.config.dbplatform.SqlLimitRequest;
import io.ebeaninternal.api.SpiQuery;

public final class OrmQueryLimitRequest implements SqlLimitRequest {

  private final SpiQuery<?> ormQuery;
  private final DatabasePlatform dbPlatform;
  private final String sql;
  private final String sqlOrderBy;
  private final boolean distinct;
  private final String distinctOn;
  private final String hint;
  private final String label;

  public OrmQueryLimitRequest(String sql, String sqlOrderBy, SpiQuery<?> ormQuery, DatabasePlatform dbPlatform, boolean distinct) {
    this(sql, sqlOrderBy, ormQuery, dbPlatform, distinct, null, "", "");
  }

  public OrmQueryLimitRequest(String sql, String sqlOrderBy, SpiQuery<?> ormQuery, DatabasePlatform dbPlatform,
                              boolean distinct, String distinctOn, String hint, String label) {
    this.sql = sql;
    this.sqlOrderBy = sqlOrderBy;
    this.ormQuery = ormQuery;
    this.dbPlatform = dbPlatform;
    this.distinct = distinct;
    this.distinctOn = distinctOn;
    this.hint = hint;
    this.label  = label;
  }

  private StringBuilder newBuffer() {
    return new StringBuilder(50 + sql.length());
  }

  @Override
  public String ansiOffsetRows() {
    final var buffer = selectDistinct();
    buffer.append(sql);
    int firstRow = getFirstRow();
    if (firstRow > 0) {
      buffer.append(" offset ").append(firstRow).append(" rows");
    }
    int maxRows = getMaxRows();
    if (maxRows > 0) {
      buffer.append(" fetch next ").append(maxRows).append(" rows only");
    }
    return buffer.toString();
  }

  @Override
  public StringBuilder selectDistinct() {
    final var buffer = newBuffer();
    buffer.append("select ").append(hint).append(label);
    if (distinct) {
      buffer.append("distinct ");
    }
    return buffer;
  }

  @Override
  public StringBuilder selectDistinctOnSql() {
    var buffer = newBuffer();
    buffer.append("select ").append(hint).append(label);
    if (distinct) {
      buffer.append("distinct ");
      if (distinctOn != null) {
        buffer.append("on (").append(distinctOn).append(") ");
      }
    }
    buffer.append(sql);
    return buffer;
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
    return distinct;
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

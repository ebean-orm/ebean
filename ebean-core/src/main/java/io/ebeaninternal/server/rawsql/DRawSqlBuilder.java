package io.ebeaninternal.server.rawsql;

import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;

import java.sql.ResultSet;

public class DRawSqlBuilder implements RawSqlBuilder {

  private final ResultSet resultSet;

  private final SpiRawSql.Sql sql;

  private final SpiRawSql.ColumnMapping columnMapping;

  DRawSqlBuilder(SpiRawSql.Sql sql, SpiRawSql.ColumnMapping columnMapping) {
    this.sql = sql;
    this.columnMapping = columnMapping;
    this.resultSet = null;
  }

  @Override
  public RawSqlBuilder columnMapping(String dbColumn, String propertyName) {
    columnMapping.columnMapping(dbColumn, propertyName);
    return this;
  }

  @Override
  public RawSqlBuilder columnMappingIgnore(String dbColumn) {
    return columnMapping(dbColumn, SpiRawSql.IGNORE_COLUMN);
  }

  @Override
  public RawSqlBuilder tableAliasMapping(String tableAlias, String path) {
    columnMapping.tableAliasMapping(tableAlias, path);
    return this;
  }

  @Override
  public RawSql create() {
    return new DRawSql(resultSet, sql, columnMapping.createImmutableCopy());
  }
}

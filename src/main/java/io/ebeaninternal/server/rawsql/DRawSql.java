package io.ebeaninternal.server.rawsql;

import java.sql.ResultSet;

/**
 * Default implementation of SpiRawSql.
 */
public final class DRawSql implements SpiRawSql {

  private final ResultSet resultSet;

  private final Sql sql;

  private final ColumnMapping columnMapping;

  /**
   * Construct with a ResultSet and properties that the columns map to.
   */
  public DRawSql(ResultSet resultSet, String... propertyNames) {
    this.resultSet = resultSet;
    this.sql = null;
    this.columnMapping = new ColumnMapping(propertyNames);
  }

  protected DRawSql(ResultSet resultSet, Sql sql, ColumnMapping columnMapping) {
    this.resultSet = resultSet;
    this.sql = sql;
    this.columnMapping = columnMapping;
  }

  /**
   * Return the Sql either unparsed or in parsed (broken up) form.
   */
  @Override
  public Sql getSql() {
    return sql;
  }

  /**
   * Return the key;
   */
  @Override
  public Key getKey() {
    boolean parsed = sql != null && sql.isParsed();
    String unParsedSql = (sql == null) ? "" : sql.getUnparsedSql();
    return new Key(parsed, unParsedSql, columnMapping);
  }

  /**
   * Return the resultSet if this is a ResultSet based RawSql.
   */
  @Override
  public ResultSet getResultSet() {
    return resultSet;
  }

  /**
   * Return the column mapping for the SQL columns to bean properties.
   */
  @Override
  public ColumnMapping getColumnMapping() {
    return columnMapping;
  }

}

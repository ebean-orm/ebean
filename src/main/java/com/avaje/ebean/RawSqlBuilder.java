package com.avaje.ebean;

import java.sql.ResultSet;

import com.avaje.ebean.RawSql.ColumnMapping;
import com.avaje.ebean.RawSql.Sql;

/**
 * Builds RawSql instances from a SQL string and column mappings.
 * <p>
 * Note that RawSql can also be defined in ebean-orm.xml files and be used as a
 * named query.
 * </p>
 * 
 * @see RawSql
 */
public class RawSqlBuilder {

  /**
   * Special property name assigned to a DB column that should be ignored.
   */
  public static final String IGNORE_COLUMN = "$$_IGNORE_COLUMN_$$";

  private final ResultSet resultSet;
  
  private final Sql sql;

  private final ColumnMapping columnMapping;

  /**
   * Create and return a RawSql object based on the resultSet and list of properties the columns in
   * the resultSet map to.
   * <p>
   * The properties listed in the propertyNames must be in the same order as the columns in the
   * resultSet.
   */
  public static RawSql resultSet(ResultSet resultSet, String... propertyNames) {
    return new RawSql(resultSet, propertyNames);
  }
  
  /**
   * Return an unparsed RawSqlBuilder. Unlike a parsed one this query can not be
   * modified - so no additional WHERE or HAVING expressions can be added to
   * this query.
   */
  public static RawSqlBuilder unparsed(String sql) {

    Sql s = new Sql(sql);
    return new RawSqlBuilder(s, new ColumnMapping());
  }

  /**
   * Return a RawSqlBuilder parsing the sql.
   * <p>
   * The sql statement will be parsed so that Ebean can determine where it can
   * insert additional WHERE or HAVING expressions.
   * </p>
   * <p>
   * Additionally the selected columns are parsed to determine the column
   * ordering. This also means additional checks can be made with the column
   * mapping - specifically we can check that all columns are mapped and that
   * correct column names are entered into the mapping.
   * </p>
   */
  public static RawSqlBuilder parse(String sql) {

    Sql sql2 = DRawSqlParser.parse(sql);
    String select = sql2.getPreFrom();

    ColumnMapping mapping = DRawSqlColumnsParser.parse(select);
    return new RawSqlBuilder(sql2, mapping);
  }
  
  private RawSqlBuilder(Sql sql, ColumnMapping columnMapping) {
    this.sql = sql;
    this.columnMapping = columnMapping;
    this.resultSet = null;
  }

  /**
   * Set the mapping of a DB Column to a bean property.
   * <p>
   * For Unparsed SQL the columnMapping MUST be defined in the same order that
   * the columns appear in the SQL statement.
   * </p>
   * 
   * @param dbColumn
   *          the DB column that we are mapping to a bean property
   * @param propertyName
   *          the bean property that we are mapping the DB column to.
   */
  public RawSqlBuilder columnMapping(String dbColumn, String propertyName) {
    columnMapping.columnMapping(dbColumn, propertyName);
    return this;
  }

  /**
   * Ignore this DB column. It is not mapped to any bean property.
   */
  public RawSqlBuilder columnMappingIgnore(String dbColumn) {
    return columnMapping(dbColumn, IGNORE_COLUMN);
  }

  /**
   * Modify any column mappings with the given table alias to have the path prefix.
   * <p>
   * For example modify all mappings with table alias "c" to have the path prefix "customer".
   * </p>
   */
  public RawSqlBuilder tableAliasMapping(String tableAlias, String path) {
    columnMapping.tableAliasMapping(tableAlias, path);
    return this;
  }

  /**
   * Create the immutable RawSql object. Do this after all the column mapping
   * has been defined.
   */
  public RawSql create() {
    return new RawSql(resultSet, sql, columnMapping.createImmutableCopy());
  }

  /**
   * Return the internal parsed Sql object (for testing).
   */
  protected Sql getSql() {
    return sql;
  }



}

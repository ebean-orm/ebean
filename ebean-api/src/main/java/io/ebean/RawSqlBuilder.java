package io.ebean;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Builds RawSql instances from a SQL string and column mappings.
 * <p>
 * Note that RawSql can also be defined in ebean-orm.xml files and be used as a
 * named query.
 * </p>
 *
 * @see RawSql
 */
public interface RawSqlBuilder {

  /**
   * Create and return a RawSql object based on the resultSet and list of properties the columns in
   * the resultSet map to.
   * <p>
   * The properties listed in the propertyNames must be in the same order as the columns in the
   * resultSet.
   */
  static RawSql resultSet(ResultSet resultSet, String... propertyNames) {
    return XServiceProvider.rawSql().resultSet(resultSet, propertyNames);
  }

  /**
   * Create and return a SqlRow based on the resultSet with dbTrueValue and binaryOptimizedUUID options.
   */
  static SqlRow sqlRow(ResultSet resultSet, final String dbTrueValue, boolean binaryOptimizedUUID) throws SQLException {
    return XServiceProvider.rawSql().sqlRow(resultSet, dbTrueValue, binaryOptimizedUUID);
  }

  /**
   * Return an unparsed RawSqlBuilder. Unlike a parsed one this query can not be
   * modified - so no additional WHERE or HAVING expressions can be added to
   * this query.
   */
  static RawSqlBuilder unparsed(String sql) {
    return XServiceProvider.rawSql().unparsed(sql);
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
  static RawSqlBuilder parse(String sql) {
    return XServiceProvider.rawSql().parsed(sql);
  }

  /**
   * Set the mapping of a DB Column to a bean property.
   * <p>
   * For Unparsed SQL the columnMapping MUST be defined in the same order that
   * the columns appear in the SQL statement.
   * </p>
   *
   * @param dbColumn     the DB column that we are mapping to a bean property
   * @param propertyName the bean property that we are mapping the DB column to.
   */
  RawSqlBuilder columnMapping(String dbColumn, String propertyName);

  /**
   * Ignore this DB column. It is not mapped to any bean property.
   */
  RawSqlBuilder columnMappingIgnore(String dbColumn);

  /**
   * Modify any column mappings with the given table alias to have the path prefix.
   * <p>
   * For example modify all mappings with table alias "c" to have the path prefix "customer".
   * </p>
   * <p>
   * For the "Root type" you don't need to specify a tableAliasMapping.
   * </p>
   */
  RawSqlBuilder tableAliasMapping(String tableAlias, String path);

  /**
   * Create the immutable RawSql object. Do this after all the column mapping
   * has been defined.
   */
  RawSql create();

}

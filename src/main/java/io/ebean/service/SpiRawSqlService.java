package io.ebean.service;

import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import io.ebean.SqlRow;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Service provided by Ebean for parsing and column mapping raw SQL queries.
 */
public interface SpiRawSqlService {

  /**
   * Create based on a JDBC ResultSet.
   */
  RawSql resultSet(ResultSet resultSet, String... propertyNames);

  /**
   * Parse the SQL determining column mapping.
   */
  RawSqlBuilder parsed(String sql);

  /**
   * Unparsed SQL so explicit column mapping expected.
   */
  RawSqlBuilder unparsed(String sql);

  /**
   * Create based on a JDBC ResultSet.
   *
   * @param resultSet           The ResultSet row to read as a SqlRow
   * @param dbTrueValue         The DB true value
   * @param binaryOptimizedUUID Flag set to true if the UUID value is stored as optimised binary(16)
   */
  SqlRow sqlRow(ResultSet resultSet, String dbTrueValue, boolean binaryOptimizedUUID) throws SQLException;
}

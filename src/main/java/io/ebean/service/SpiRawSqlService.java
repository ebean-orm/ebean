package io.ebean.service;

import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;

import java.sql.ResultSet;

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
}

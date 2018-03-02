package io.ebeaninternal.server.dto;

import io.ebeaninternal.metric.QueryPlanCollector;
import io.ebeaninternal.server.type.DataReader;

import java.sql.SQLException;

/**
 * Knows how to read and map rows into a Bean.
 */
public interface DtoQueryPlan {

  /**
   * Read the row data and return the DTO bean.
   */
  Object readRow(DataReader dataReader) throws SQLException;

  /**
   * Add an event to the query execution statistics.
   */
  void collect(long exeMicros, int rows);

  /**
   * Collect the query plan statistics.
   */
  void collectStats(QueryPlanCollector collector);
}

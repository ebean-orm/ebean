package io.ebeaninternal.server.core;


import io.ebean.RowConsumer;
import io.ebean.RowMapper;
import io.ebean.SqlRow;
import io.ebean.meta.MetricVisitor;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface RelationalQueryEngine {

  /**
   * Return a new SqlRow with appropriate mapping for DB true and optimised binary UUID etc.
   */
  SqlRow createSqlRow(int estimateCapacity);

  /**
   * Find a list of beans using relational query.
   */
  <T> List<T> findList(RelationalQueryRequest request, RowReader<T> reader);

  /**
   * Find each while query using relational query.
   */
  <T> void findEach(RelationalQueryRequest request, RowReader<T> reader, Predicate<T> consumer);

  /**
   * Find each via raw consumer.
   */
  void findEachRow(RelationalQueryRequest request, RowConsumer mapper);

  /**
   * Find one via mapper.
   */
  <T> T findOneMapper(RelationalQueryRequest request, RowMapper<T> mapper);

  /**
   * Find single attribute.
   */
  <T> T findSingleAttribute(RelationalQueryRequest request, Class<T> cls);

  /**
   * Find single attribute list.
   */
  <T> List<T> findSingleAttributeList(RelationalQueryRequest request, Class<T> cls);

  /**
   * Collect SQL query execution statistics.
   */
  void collect(String label, long exeMicros);

  /**
   * Visit the metrics.
   */
  void visitMetrics(MetricVisitor visitor);
}

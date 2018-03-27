package io.ebeaninternal.server.core;


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
  List<SqlRow> findList(RelationalQueryRequest request);

  /**
   * Find each query using relational query.
   */
  void findEach(RelationalQueryRequest request, Consumer<SqlRow> consumer);

  /**
   * Find each while query using relational query.
   */
  void findEach(RelationalQueryRequest request, Predicate<SqlRow> consumer);

  /**
   * Collect SQL query execution statistics.
   */
  void collect(String label, long exeMicros, int rows);

  /**
   * Visit the metrics.
   */
  void visitMetrics(MetricVisitor visitor);
}

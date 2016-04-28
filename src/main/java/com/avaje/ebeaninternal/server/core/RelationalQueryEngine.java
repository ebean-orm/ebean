package com.avaje.ebeaninternal.server.core;


import com.avaje.ebean.QueryEachConsumer;
import com.avaje.ebean.QueryEachWhileConsumer;
import com.avaje.ebean.SqlRow;

public interface RelationalQueryEngine {

  /**
   * Find a list of beans using relational query.
   */
  Object findMany(RelationalQueryRequest request);

  /**
   * Find each query using relational query.
   */
  void findEach(RelationalQueryRequest request, QueryEachConsumer<SqlRow> consumer);

  /**
   * Find each while query using relational query.
   */
  void findEach(RelationalQueryRequest request, QueryEachWhileConsumer<SqlRow> consumer);

}
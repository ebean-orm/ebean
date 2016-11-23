package com.avaje.ebeaninternal.server.core;


import com.avaje.ebean.QueryEachWhileConsumer;
import com.avaje.ebean.SqlRow;

import java.util.List;
import java.util.function.Consumer;

public interface RelationalQueryEngine {

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
  void findEach(RelationalQueryRequest request, QueryEachWhileConsumer<SqlRow> consumer);

}

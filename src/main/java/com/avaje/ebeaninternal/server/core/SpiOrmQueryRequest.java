package com.avaje.ebeaninternal.server.core;

import com.avaje.ebean.QueryEachConsumer;
import com.avaje.ebean.QueryEachWhileConsumer;
import com.avaje.ebean.Version;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeanservice.docstore.api.DocQueryRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Defines the ORM query request api.
 */
public interface SpiOrmQueryRequest<T> extends DocQueryRequest<T> {

  /**
   * Return the query.
   */
  SpiQuery<T> getQuery();

  /**
   * Return the associated BeanDescriptor.
   */
  BeanDescriptor<?> getBeanDescriptor();

  /**
   * This will create a local (readOnly) transaction if no current transaction
   * exists.
   * <p>
   * A transaction may have been passed in explicitly or currently be active
   * in the thread local. If not, then a readOnly transaction is created to
   * execute this query.
   * </p>
   */
  void initTransIfRequired();

  /**
   * Will end a locally created transaction.
   * <p>
   * It ends the transaction by using a rollback() as the transaction is known
   * to be readOnly.
   * </p>
   */
  void endTransIfRequired();

  /**
   * Execute the query as a delete.
   */
  int delete();

  /**
   * Execute the query as a update.
   */
  int update();

  /**
   * Execute the query as findById.
   */
  Object findId();

  /**
   * Execute the find row count query.
   */
  int findRowCount();

  /**
   * Execute the find ids query.
   */
  List<Object> findIds();

  /**
   * Execute the find returning a QueryIterator and visitor pattern.
   */
  void findEach(QueryEachConsumer<T> consumer);

  /**
   * Execute the find returning a QueryIterator and visitor pattern.
   */
  void findEachWhile(QueryEachWhileConsumer<T> consumer);

  /**
   * Execute the find returning a QueryIterator.
   */
  QueryIterator<T> findIterate();

  /**
   * Execute the finVersions() query.
   */
  List<Version<T>> findVersions();

  /**
   * Execute the query as findList.
   */
  List<T> findList();

  /**
   * Execute the query as findSet.
   */
  Set<?> findSet();

  /**
   * Execute the query as findMap.
   */
  Map<?, ?> findMap();

  /**
   * Try to get the query result from the query cache.
   */
  BeanCollection<T> getFromQueryCache();

  /**
   * Return the Database platform like clause.
   */
  String getDBLikeClause();

  /**
   * Mark the underlying transaction as not being query only.
   */
  void markNotQueryOnly();

  /**
   * Return true if this query is expected to use the doc store.
   */
  boolean isUseDocStore();
}
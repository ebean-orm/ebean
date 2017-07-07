package io.ebeaninternal.server.core;

import io.ebean.QueryIterator;
import io.ebean.Version;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeanservice.docstore.api.DocQueryRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Defines the ORM query request api.
 */
public interface SpiOrmQueryRequest<T> extends DocQueryRequest<T> {

  /**
   * Return the query.
   */
  @Override
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
  int findCount();

  /**
   * Execute the find ids query.
   */
  <A> List<A> findIds();

  /**
   * Execute the find returning a QueryIterator and visitor pattern.
   */
  void findEach(Consumer<T> consumer);

  /**
   * Execute the find returning a QueryIterator and visitor pattern.
   */
  void findEachWhile(Predicate<T> consumer);

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
   * Execute the findSingleAttributeList query.
   */
  <A> List<A> findSingleAttributeList();

  /**
   * Try to get the query result from the query cache.
   */
  <A> A getFromQueryCache();

  /**
   * Return the Database platform like clause.
   */
  String getDBLikeClause();

  /**
   * Escapes a string to use it as exact match in Like clause.
   */
  String escapeLikeString(String value);

  /**
   * Mark the underlying transaction as not being query only.
   */
  void markNotQueryOnly();

  /**
   * Return true if this query is expected to use the doc store.
   */
  boolean isUseDocStore();
}

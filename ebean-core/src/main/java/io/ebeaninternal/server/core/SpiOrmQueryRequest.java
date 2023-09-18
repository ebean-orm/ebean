package io.ebeaninternal.server.core;

import io.ebean.QueryIterator;
import io.ebean.Version;
import io.ebean.event.BeanQueryRequest;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeanservice.docstore.api.DocQueryRequest;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Defines the ORM query request api.
 */
public interface SpiOrmQueryRequest<T> extends BeanQueryRequest<T>, DocQueryRequest<T> {

  /**
   * Return the query.
   */
  @Override
  SpiQuery<T> query();

  /**
   * Return the associated BeanDescriptor.
   */
  BeanDescriptor<T> descriptor();

  /**
   * Prepare the query for execution.
   */
  void prepareQuery();

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
   * Execute findEach iterating results one bean at a time.
   */
  void findEach(Consumer<T> consumer);

  /**
   * Execute findEach with a batch consumer.
   */
  void findEach(int batch, Consumer<List<T>> batchConsumer);

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
  Set<T> findSet();

  /**
   * Execute the query as findMap.
   */
  <K> Map<K, T> findMap();

  /**
   * Execute the findSingleAttributeCollection query.
   */
  <A extends Collection<?>> A findSingleAttributeCollection(A collection);

  /**
   * Execute returning the ResultSet.
   */
  SpiResultSet findResultSet();

  /**
   * Try to get the query result from the query cache.
   */
  <A> A getFromQueryCache();

  /**
   * Return if query cache is active.
   */
  boolean isQueryCacheActive();

  /**
   * Return if results should be put to query cache.
   */
  boolean isQueryCachePut();

  /**
   * Put the result to the query cache.
   */
  void putToQueryCache(Object result);

  /**
   * Maybe hit the bean cache returning true if everything was obtained from the
   * cache (that there were no misses).
   * <p>
   * Do this for findList() on many natural keys or many Ids.
   */
  boolean getFromBeanCache();

  /**
   * Return the bean cache hits (when all hits / no misses).
   */
  List<T> beanCacheHits();

  /**
   * Return the bean cache hits for findMap (when all hits / no misses).
   */
  <K> Map<K,T> beanCacheHitsAsMap();

  /**
   * Return the bean cache hits for findMap (when all hits / no misses).
   */
  Set<T> beanCacheHitsAsSet();

  /**
   * Reset Bean cache mode AUTO - require explicit setting for bean cache use with findList().
   */
  void resetBeanCacheAutoMode(boolean findOne);

  /**
   * Return the Database platform like clause.
   */
  String dbLikeClause(boolean rawLikeExpression);

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

  /**
   * Return true if delete by statement is allowed for this type given cascade rules etc.
   */
  boolean isDeleteByStatement();

  /**
   * Return true if hitting bean cache and returning all beans from cache.
   */
  boolean isGetAllFromBeanCache();
}

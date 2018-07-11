package io.ebean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.NonUniqueResultException;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The extended API for EbeanServer.
 * <p>
 * This provides the finder methods that take an explicit transaction rather than obtaining
 * the transaction from the usual mechanism (which is ThreadLocal based).
 * </p>
 * <p>
 * In general we only want to use this ExtendedServer API when we want to avoid / bypass
 * the use of the mechanism to get the current transaction and instead explicitly supply
 * the transaction to use.
 * </p>
 * <p>
 * Note that in all cases the transaction supplied can be null and in this case the EbeanServer
 * will use the normal mechanism to obtain the transaction to use.
 * </p>
 */
public interface ExtendedServer {

  /**
   * Return the NOW time from the Clock.
   */
  long clockNow();

  /**
   * Set the Clock to use for <code>@WhenCreated</code> and <code>@WhenModified</code>.
   * <p>
   * Note that we only expect to change the Clock for testing purposes.
   * </p>
   */
  void setClock(Clock clock);

  /**
   * Return the number of 'top level' or 'root' entities this query should return.
   *
   * @see Query#findCount()
   * @see Query#findFutureCount()
   */
  <T> int findCount(Query<T> query, Transaction transaction);

  /**
   * Return the Id values of the query as a List.
   *
   * @see Query#findIds()
   */
  @Nonnull
  <A, T> List<A> findIds(Query<T> query, Transaction transaction);

  /**
   * Return a QueryIterator for the query.
   * <p>
   * Generally using {@link #findEach(Query, Consumer, Transaction)} or
   * {@link #findEachWhile(Query, Predicate, Transaction)} is preferred
   * to findIterate(). The reason is that those methods automatically take care of
   * closing the queryIterator (and the underlying jdbc statement and resultSet).
   * </p>
   * <p>
   * This is similar to findEach in that not all the result beans need to be held
   * in memory at the same time and as such is good for processing large queries.
   * </p>
   *
   * @see Query#findIterate()
   * @see Query#findEach(Consumer)
   * @see Query#findEachWhile(Predicate)
   */
  @Nonnull
  <T> QueryIterator<T> findIterate(Query<T> query, Transaction transaction);

  /**
   * Execute the query visiting the each bean one at a time.
   * <p>
   * Unlike findList() this is suitable for processing a query that will return
   * a very large resultSet. The reason is that not all the result beans need to be
   * held in memory at the same time and instead processed one at a time.
   * </p>
   * <p>
   * Internally this query using a PersistenceContext scoped to each bean (and the
   * beans associated object graph).
   * </p>
   * <p>
   * <pre>{@code
   *
   *     ebeanServer.find(Order.class)
   *       .where().eq("status", Order.Status.NEW)
   *       .order().asc("id")
   *       .findEach((Order order) -> {
   *
   *         // do something with the order bean
   *         System.out.println(" -- processing order ... " + order);
   *       });
   *
   * }</pre>
   *
   * @see Query#findEach(Consumer)
   * @see Query#findEachWhile(Predicate)
   */
  <T> void findEach(Query<T> query, Consumer<T> consumer, Transaction transaction);

  /**
   * Execute the query visiting the each bean one at a time.
   * <p>
   * Compared to findEach() this provides the ability to stop processing the query
   * results early by returning false for the Predicate.
   * </p>
   * <p>
   * Unlike findList() this is suitable for processing a query that will return
   * a very large resultSet. The reason is that not all the result beans need to be
   * held in memory at the same time and instead processed one at a time.
   * </p>
   * <p>
   * Internally this query using a PersistenceContext scoped to each bean (and the
   * beans associated object graph).
   * </p>
   * <p>
   * <pre>{@code
   *
   *     ebeanServer.find(Order.class)
   *       .where().eq("status", Order.Status.NEW)
   *       .order().asc("id")
   *       .findEachWhile((Order order) -> {
   *
   *         // do something with the order bean
   *         System.out.println(" -- processing order ... " + order);
   *
   *         boolean carryOnProcessing = ...
   *         return carryOnProcessing;
   *       });
   *
   * }</pre>
   *
   * @see Query#findEach(Consumer)
   * @see Query#findEachWhile(Predicate)
   */
  <T> void findEachWhile(Query<T> query, Predicate<T> consumer, Transaction transaction);

  /**
   * Return versions of a @History entity bean.
   * <p>
   * Generally this query is expected to be a find by id or unique predicates query.
   * It will execute the query against the history returning the versions of the bean.
   * </p>
   */
  @Nonnull
  <T> List<Version<T>> findVersions(Query<T> query, Transaction transaction);

  /**
   * Execute a query returning a list of beans.
   * <p>
   * Generally you are able to use {@link Query#findList()} rather than
   * explicitly calling this method. You could use this method if you wish to
   * explicitly control the transaction used for the query.
   * </p>
   * <p>
   * <pre>{@code
   *
   * List<Customer> customers =
   *     ebeanServer.find(Customer.class)
   *     .where().ilike("name", "rob%")
   *     .findList();
   *
   * }</pre>
   *
   * @param <T>         the type of entity bean to fetch.
   * @param query       the query to execute.
   * @param transaction the transaction to use (can be null).
   * @return the list of fetched beans.
   * @see Query#findList()
   */
  @Nonnull
  <T> List<T> findList(Query<T> query, Transaction transaction);

  /**
   * Execute find row count query in a background thread.
   * <p>
   * This returns a Future object which can be used to cancel, check the
   * execution status (isDone etc) and get the value (with or without a
   * timeout).
   * </p>
   *
   * @param query       the query to execute the row count on
   * @param transaction the transaction (can be null).
   * @return a Future object for the row count query
   * @see Query#findFutureCount()
   */
  @Nonnull
  <T> FutureRowCount<T> findFutureCount(Query<T> query, Transaction transaction);

  /**
   * Execute find Id's query in a background thread.
   * <p>
   * This returns a Future object which can be used to cancel, check the
   * execution status (isDone etc) and get the value (with or without a
   * timeout).
   * </p>
   *
   * @param query       the query to execute the fetch Id's on
   * @param transaction the transaction (can be null).
   * @return a Future object for the list of Id's
   * @see Query#findFutureIds()
   */
  @Nonnull
  <T> FutureIds<T> findFutureIds(Query<T> query, Transaction transaction);

  /**
   * Execute find list query in a background thread returning a FutureList object.
   * <p>
   * This returns a Future object which can be used to cancel, check the
   * execution status (isDone etc) and get the value (with or without a timeout).
   * <p>
   * This query will execute in it's own PersistenceContext and using its own transaction.
   * What that means is that it will not share any bean instances with other queries.
   *
   * @param query       the query to execute in the background
   * @param transaction the transaction (can be null).
   * @return a Future object for the list result of the query
   * @see Query#findFutureList()
   */
  @Nonnull
  <T> FutureList<T> findFutureList(Query<T> query, Transaction transaction);

  /**
   * Return a PagedList for this query using firstRow and maxRows.
   * <p>
   * The benefit of using this over findList() is that it provides functionality to get the
   * total row count etc.
   * </p>
   * <p>
   * If maxRows is not set on the query prior to calling findPagedList() then a
   * PersistenceException is thrown.
   * </p>
   * <p>
   * <pre>{@code
   *
   *  PagedList<Order> pagedList = Ebean.find(Order.class)
   *       .setFirstRow(50)
   *       .setMaxRows(20)
   *       .findPagedList();
   *
   *       // fetch the total row count in the background
   *       pagedList.loadRowCount();
   *
   *       List<Order> orders = pagedList.getList();
   *       int totalRowCount = pagedList.getTotalRowCount();
   *
   * }</pre>
   *
   * @return The PagedList
   * @see Query#findPagedList()
   */
  @Nonnull
  <T> PagedList<T> findPagedList(Query<T> query, Transaction transaction);

  /**
   * Execute the query returning a set of entity beans.
   * <p>
   * Generally you are able to use {@link Query#findSet()} rather than
   * explicitly calling this method. You could use this method if you wish to
   * explicitly control the transaction used for the query.
   * </p>
   * <p>
   * <pre>{@code
   *
   * Set<Customer> customers =
   *     ebeanServer.find(Customer.class)
   *     .where().ilike("name", "rob%")
   *     .findSet();
   *
   * }</pre>
   *
   * @param <T>         the type of entity bean to fetch.
   * @param query       the query to execute
   * @param transaction the transaction to use (can be null).
   * @return the set of fetched beans.
   * @see Query#findSet()
   */
  @Nonnull
  <T> Set<T> findSet(Query<T> query, Transaction transaction);

  /**
   * Execute the query returning the entity beans in a Map.
   * <p>
   * Generally you are able to use {@link Query#findMap()} rather than
   * explicitly calling this method. You could use this method if you wish to
   * explicitly control the transaction used for the query.
   * </p>
   *
   * @param <T>         the type of entity bean to fetch.
   * @param query       the query to execute.
   * @param transaction the transaction to use (can be null).
   * @return the map of fetched beans.
   * @see Query#findMap()
   */
  @Nonnull
  <K, T> Map<K, T> findMap(Query<T> query, Transaction transaction);

  /**
   * Execute the query returning a list of values for a single property.
   * <p>
   * <h3>Example 1:</h3>
   * <pre>{@code
   *
   *  List<String> names =
   *    Ebean.find(Customer.class)
   *      .select("name")
   *      .orderBy().asc("name")
   *      .findSingleAttributeList();
   *
   * }</pre>
   * <h3>Example 2:</h3>
   * <pre>{@code
   *
   *  List<String> names =
   *    Ebean.find(Customer.class)
   *      .setDistinct(true)
   *      .select("name")
   *      .where().eq("status", Customer.Status.NEW)
   *      .orderBy().asc("name")
   *      .setMaxRows(100)
   *      .findSingleAttributeList();
   *
   * }</pre>
   *
   * @return the list of values for the selected property
   * @see Query#findSingleAttributeList()
   */
  @Nonnull
  <A, T> List<A> findSingleAttributeList(Query<T> query, Transaction transaction);

  /**
   * Execute the query returning at most one entity bean or null (if no matching
   * bean is found).
   * <p>
   * This will throw a NonUniqueResultException if the query finds more than one result.
   * </p>
   * <p>
   * Generally you are able to use {@link Query#findOne()} rather than
   * explicitly calling this method. You could use this method if you wish to
   * explicitly control the transaction used for the query.
   * </p>
   *
   * @param <T>         the type of entity bean to fetch.
   * @param query       the query to execute.
   * @param transaction the transaction to use (can be null).
   * @return the list of fetched beans.
   * @throws NonUniqueResultException if more than one result was found
   * @see Query#findOne()
   */
  @Nullable
  <T> T findOne(Query<T> query, Transaction transaction);

  /**
   * Similar to findOne() but returns an Optional (rather than nullable).
   */
  @Nonnull
  <T> Optional<T> findOneOrEmpty(Query<T> query, Transaction transaction);

  /**
   * Execute as a delete query deleting the 'root level' beans that match the predicates
   * in the query.
   * <p>
   * Note that if the query includes joins then the generated delete statement may not be
   * optimal depending on the database platform.
   * </p>
   *
   * @param query       the query used for the delete
   * @param transaction the transaction to use (can be null)
   * @param <T>         the type of entity bean to fetch.
   * @return the number of beans/rows that were deleted
   */
  <T> int delete(Query<T> query, Transaction transaction);

  /**
   * Execute the update query returning the number of rows updated.
   * <p>
   * The update query must be created using {@link EbeanServer#update(Class)}.
   * </p>
   *
   * @param query       the update query to execute
   * @param transaction the optional transaction to use for the update (can be null)
   * @param <T>         the type of entity bean
   * @return The number of rows updated
   */
  <T> int update(Query<T> query, Transaction transaction);

  /**
   * Execute the sql query returning a list of MapBean.
   * <p>
   * Generally you are able to use {@link SqlQuery#findList()} rather than
   * explicitly calling this method. You could use this method if you wish to
   * explicitly control the transaction used for the query.
   * </p>
   *
   * @param query       the query to execute.
   * @param transaction the transaction to use (can be null).
   * @return the list of fetched MapBean.
   * @see SqlQuery#findList()
   */
  @Nonnull
  List<SqlRow> findList(SqlQuery query, Transaction transaction);

  /**
   * Execute the SqlQuery iterating a row at a time.
   * <p>
   * This streaming type query is useful for large query execution as only 1 row needs to be held in memory.
   * </p>
   */
  void findEach(SqlQuery query, Consumer<SqlRow> consumer, Transaction transaction);

  /**
   * Execute the SqlQuery iterating a row at a time with the ability to stop consuming part way through.
   * <p>
   * Returning false after processing a row stops the iteration through the query results.
   * </p>
   * <p>
   * This streaming type query is useful for large query execution as only 1 row needs to be held in memory.
   * </p>
   */
  void findEachWhile(SqlQuery query, Predicate<SqlRow> consumer, Transaction transaction);

  /**
   * Execute the sql query returning a single MapBean or null.
   * <p>
   * This will throw a PersistenceException if the query found more than one
   * result.
   * </p>
   * <p>
   * Generally you are able to use {@link SqlQuery#findOne()} rather than
   * explicitly calling this method. You could use this method if you wish to
   * explicitly control the transaction used for the query.
   * </p>
   *
   * @param query       the query to execute.
   * @param transaction the transaction to use (can be null).
   * @return the fetched MapBean or null if none was found.
   * @see SqlQuery#findOne()
   */
  @Nullable
  SqlRow findOne(SqlQuery query, Transaction transaction);

}

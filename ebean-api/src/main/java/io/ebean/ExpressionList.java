package io.ebean;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import io.ebean.search.*;

import jakarta.persistence.NonUniqueResultException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * List of Expressions that make up a where or having clause.
 * <p>
 * An ExpressionList is returned from {@link Query#where()}.
 * </p>
 * <p>
 * The ExpressionList has a list of convenience methods that create the standard
 * expressions and add them to this list.
 * </p>
 * <p>
 * The ExpressionList also duplicates methods that are found on the Query such
 * as findList() and order(). The purpose of these methods is provide a fluid
 * API. The upside of this approach is that you can build and execute a query
 * via chained methods. The down side is that this ExpressionList object has
 * more methods than you would initially expect (the ones duplicated from
 * Query).
 * </p>
 *
 * @see Query#where()
 */
@NullMarked
public interface ExpressionList<T> {

  /**
   * Return the query that owns this expression list.
   * <p>
   * This is a convenience method solely to support a fluid API where the
   * methods are chained together. Adding expressions returns this expression
   * list and this method can be used after that to return back the original
   * query so that further things can be added to it.
   * </p>
   */
  Query<T> query();

  /**
   * Controls, if paginated queries should always append an 'order by id' statement at the end to
   * guarantee a deterministic sort result. This may affect performance.
   * If this is not enabled, and an orderBy is set on the query, it's up to the programmer that
   * this query provides a deterministic result.
   */
  Query<T> orderById(boolean orderById);

  /**
   * Set the order by clause replacing the existing order by clause if there is
   * one.
   * <p>
   * This follows SQL syntax using commas between each property with the
   * optional asc and desc keywords representing ascending and descending order
   * respectively.
   */
  ExpressionList<T> orderBy(String orderBy);

  /**
   * Return the OrderBy so that you can append an ascending or descending
   * property to the order by clause.
   * <p>
   * This will never return a null. If no order by clause exists then an 'empty'
   * OrderBy object is returned.
   * <p>
   * This is the same as <code>order()</code>
   */
  OrderBy<T> orderBy();

  /**
   * Apply the path properties to the query replacing the select and fetch clauses.
   */
  Query<T> apply(FetchPath fetchPath);

  /**
   * Perform an 'As of' query using history tables to return the object graph
   * as of a time in the past.
   * <p>
   * To perform this query the DB must have underlying history tables.
   * </p>
   *
   * @param asOf the date time in the past at which you want to view the data
   */
  Query<T> asOf(Timestamp asOf);

  /**
   * Execute the query against the draft set of tables.
   */
  Query<T> asDraft();

  /**
   * Convert the query to a DTO bean query.
   * <p>
   * We effectively use the underlying ORM query to build the SQL and then execute
   * and map it into DTO beans.
   */
  <D> DtoQuery<D> asDto(Class<D> dtoClass);

  /**
   * Return the underlying query as an UpdateQuery.
   * <p>
   * Typically this is used with query beans to covert a query bean
   * query into an UpdateQuery like the examples below.
   * </p>
   *
   * <pre>{@code
   *
   *  int rowsUpdated = new QCustomer()
   *       .name.startsWith("Rob")
   *       .asUpdate()
   *       .set("active", false)
   *       .update();;
   *
   * }</pre>
   *
   * <pre>{@code
   *
   *   int rowsUpdated = new QContact()
   *       .notes.note.startsWith("Make Inactive")
   *       .email.endsWith("@foo.com")
   *       .customer.id.equalTo(42)
   *       .asUpdate()
   *       .set("inactive", true)
   *       .setRaw("email = lower(email)")
   *       .update();
   *
   * }</pre>
   */
  UpdateQuery<T> asUpdate();

  /**
   * Execute the query with the given lock type and WAIT.
   * <p>
   * Note that <code>forUpdate()</code> is the same as
   * <code>withLock(LockType.UPDATE)</code>.
   * <p>
   * Provides us with the ability to explicitly use Postgres
   * SHARE, KEY SHARE, NO KEY UPDATE and UPDATE row locks.
   */
  Query<T> withLock(Query.LockType lockType);

  /**
   * Execute the query with the given lock type and lock wait.
   * <p>
   * Note that <code>forUpdateNoWait()</code> is the same as
   * <code>withLock(LockType.UPDATE, LockWait.NOWAIT)</code>.
   * <p>
   * Provides us with the ability to explicitly use Postgres
   * SHARE, KEY SHARE, NO KEY UPDATE and UPDATE row locks.
   */
  Query<T> withLock(Query.LockType lockType, Query.LockWait lockWait);

  /**
   * Execute using "for update" clause which results in the DB locking the record.
   */
  Query<T> forUpdate();

  /**
   * Execute using "for update" clause with No Wait option.
   * <p>
   * This is typically a Postgres and Oracle only option at this stage.
   * </p>
   */
  Query<T> forUpdateNoWait();

  /**
   * Execute using "for update" clause with Skip Locked option.
   * <p>
   * This is typically a Postgres and Oracle only option at this stage.
   * </p>
   */
  Query<T> forUpdateSkipLocked();

  /**
   * Execute the query including soft deleted rows.
   */
  Query<T> setIncludeSoftDeletes();

  /**
   * Execute the query using the given transaction.
   */
  Query<T> usingTransaction(Transaction transaction);

  /**
   * Execute the query using the given connection.
   */
  Query<T> usingConnection(Connection connection);

  /**
   * Execute as a delete query deleting the 'root level' beans that match the predicates
   * in the query.
   * <p>
   * Note that if the query includes joins then the generated delete statement may not be
   * optimal depending on the database platform.
   * </p>
   *
   * @return the number of rows that were deleted.
   */
  int delete();

  /**
   * @deprecated migrate to {@link #usingTransaction(Transaction)} then delete().
   * <p>
   * Execute as a delete query deleting the 'root level' beans that match the predicates
   * in the query.
   * <p>
   * Note that if the query includes joins then the generated delete statement may not be
   * optimal depending on the database platform.
   * </p>
   *
   * @return the number of rows that were deleted.
   */
  @Deprecated(forRemoval = true, since = "13.1.0")
  int delete(Transaction transaction);

  /**
   * Execute as a update query.
   *
   * @return the number of rows that were updated.
   * @see UpdateQuery
   */
  int update();

  /**
   * @deprecated migrate to {@link #usingTransaction(Transaction)} then update().
   * <p>
   * Execute as a update query with the given transaction.
   *
   * @return the number of rows that were updated.
   * @see UpdateQuery
   */
  @Deprecated(forRemoval = true, since = "13.1.0")
  int update(Transaction transaction);

  /**
   * Execute the query returning true if a row is found.
   * <p>
   * The query is executed using max rows of 1 and will only select the id property.
   * This method is really just a convenient way to optimise a query to perform a
   * 'does a row exist in the db' check.
   * </p>
   *
   * <h2>Example:</h2>
   * <pre>{@code
   *
   *   boolean userExists = query().where().eq("email", "rob@foo.com").exists();
   *
   * }</pre>
   *
   * <h2>Example using a query bean:</h2>
   * <pre>{@code
   *
   *   boolean userExists = new QContact().email.equalTo("rob@foo.com").exists();
   *
   * }</pre>
   *
   * @return True if the query finds a matching row in the database
   */
  boolean exists();

  /**
   * Execute the query iterating over the results.
   *
   * @see Query#findIterate()
   */
  QueryIterator<T> findIterate();

  /**
   * Execute the query process the beans one at a time.
   *
   * @see Query#findEach(Consumer)
   */
  void findEach(Consumer<T> consumer);

  /**
   * Execute findEach with a batch consumer.
   *
   * @see Query#findEach(int, Consumer)
   */
  void findEach(int batch, Consumer<List<T>> consumer);

  /**
   * Execute the query processing the beans one at a time with the ability to
   * stop processing before reading all the beans.
   *
   * @see Query#findEachWhile(Predicate)
   */
  void findEachWhile(Predicate<T> consumer);

  /**
   * Execute the query returning a list.
   *
   * @see Query#findList()
   */
  List<T> findList();

  /**
   * Execute the query returning the list of Id's.
   *
   * @see Query#findIds()
   */
  <A> List<A> findIds();

  /**
   * Return the count of entities this query should return.
   * <p>
   * This is the number of 'top level' or 'root level' entities.
   * </p>
   */
  int findCount();

  /**
   * Execute the query returning a set.
   *
   * @see Query#findSet()
   */
  Set<T> findSet();

  /**
   * Execute the query returning a map.
   *
   * @see Query#findMap()
   */
  <K> Map<K, T> findMap();

  /**
   * Execute the query returning a list of values for a single property.
   *
   * <h3>Example 1:</h3>
   * <pre>{@code
   *
   *  List<String> names =
   *    DB.find(Customer.class)
   *      .select("name")
   *      .orderBy().asc("name")
   *      .findSingleAttributeList();
   *
   * }</pre>
   *
   * <h3>Example 2:</h3>
   * <pre>{@code
   *
   *  List<String> names =
   *    DB.find(Customer.class)
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
   */
  <A> List<A> findSingleAttributeList();

  /**
   * Executes the query returning a set of values for a single property.
   * <p>
   * This can be used to cache sets.
   *
   * @return a HashSet of values for the selegted property
   */
  <A> Set<A> findSingleAttributeSet();

  /**
   * Execute a query returning a single value of a single property/column.
   * <pre>{@code
   *
   *  String name =
   *    DB.find(Customer.class)
   *      .select("name")
   *      .where().eq("id", 42)
   *      .findSingleAttribute();
   *
   * }</pre>
   */
  @Nullable
  default <A> A findSingleAttribute() {
    List<A> list = findSingleAttributeList();
    return !list.isEmpty() ? list.get(0) : null;
  }

  /**
   * Execute the query returning a single bean or null (if no matching
   * bean is found).
   * <p>
   * If more than 1 row is found for this query then a NonUniqueResultException is
   * thrown.
   * </p>
   *
   * @throws NonUniqueResultException if more than one result was found
   * @see Query#findOne()
   */
  @Nullable
  T findOne();

  /**
   * Execute the query returning an optional bean.
   */
  Optional<T> findOneOrEmpty();

  /**
   * Execute find row count query in a background thread.
   * <p>
   * This returns a Future object which can be used to cancel, check the
   * execution status (isDone etc) and get the value (with or without a
   * timeout).
   * </p>
   *
   * @return a Future object for the row count query
   */
  FutureRowCount<T> findFutureCount();

  /**
   * Execute find Id's query in a background thread.
   * <p>
   * This returns a Future object which can be used to cancel, check the
   * execution status (isDone etc) and get the value (with or without a
   * timeout).
   * </p>
   *
   * @return a Future object for the list of Id's
   */
  FutureIds<T> findFutureIds();

  /**
   * Execute find list query in a background thread.
   * <p>
   * This returns a Future object which can be used to cancel, check the
   * execution status (isDone etc) and get the value (with or without a
   * timeout).
   * </p>
   *
   * @return a Future object for the list result of the query
   */
  FutureList<T> findFutureList();

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
   * <pre>{@code
   *
   *  PagedList<Order> pagedList = DB.find(Order.class)
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
  PagedList<T> findPagedList();

  /**
   * Return versions of a @History entity bean.
   * <p>
   * Generally this query is expected to be a find by id or unique predicates query.
   * It will execute the query against the history returning the versions of the bean.
   * </p>
   */
  List<Version<T>> findVersions();

  /**
   * Return versions of a @History entity bean between the 2 timestamps.
   * <p>
   * Generally this query is expected to be a find by id or unique predicates query.
   * It will execute the query against the history returning the versions of the bean.
   * </p>
   */
  List<Version<T>> findVersionsBetween(Timestamp start, Timestamp end);

  /**
   * Add some filter predicate expressions to the many property.
   */
  ExpressionList<T> filterMany(String manyProperty);

  /**
   * @deprecated for removal - migrate to {@link #filterManyRaw(String, String, Object...)}.
   * <p>
   * Add filter expressions to the many property.
   *
   * <pre>{@code
   *
   *   DB.find(Customer.class)
   *   .where()
   *   .eq("name", "Rob")
   *   .filterMany("orders", "status = ?", Status.NEW)
   *   .findList();
   *
   * }</pre>
   *
   * @param manyProperty The many property
   * @param expressions  Filter expressions with and, or and ? or ?1 type bind parameters
   * @param params       Bind parameters used in the expressions
   */
  @Deprecated(forRemoval = true)
  ExpressionList<T> filterMany(String manyProperty, String expressions, Object... params);

  /**
   * Add filter expressions for the many path. The expressions can include SQL functions if
   * desired and the property names are translated to column names.
   * <p>
   * The expressions can contain placeholders for bind values using <code>?</code> or <code>?1</code> style.
   *
   * <pre>{@code
   *
   *     new QCustomer()
   *       .name.startsWith("Postgres")
   *       .contacts.filterManyRaw("status = ? and firstName like ?", Contact.Status.NEW, "Rob%")
   *       .findList();
   *
   * }</pre>
   *
   * @param rawExpressions The raw expressions which can include ? and ?1 style bind parameter placeholders
   * @param params The parameter values to bind
   */
  ExpressionList<T> filterManyRaw(String manyProperty, String rawExpressions, Object... params);

  /**
   * Specify specific properties to fetch on the main/root bean (aka partial
   * object).
   *
   * @see Query#select(String)
   */
  Query<T> select(String properties);

  /**
   * Apply the fetchGroup which defines what part of the object graph to load.
   */
  Query<T> select(FetchGroup<T> fetchGroup);

  /**
   * Set whether this query uses DISTINCT.
   * <p>
   * The select() clause MUST be specified when setDistinct(true) is set. The reason for this is that
   * generally ORM queries include the "id" property and this doesn't make sense for distinct queries.
   * </p>
   * <pre>{@code
   *
   *   List<Customer> customers =
   *       DB.find(Customer.class)
   *          .setDistinct(true)
   *          .select("name")     // only select the customer name
   *          .findList();
   *
   * }</pre>
   */
  Query<T> setDistinct(boolean distinct);

  /**
   * Set the index(es) to search for a document store which uses partitions.
   * <p>
   * For example, when executing a query against ElasticSearch with daily indexes we can
   * explicitly specify the indexes to search against.
   * </p>
   *
   * @param indexName The index or indexes to search against
   * @return This query
   * @see Query#setDocIndexName(String)
   */
  Query<T> setDocIndexName(String indexName);

  /**
   * Set the first row to fetch.
   *
   * @see Query#setFirstRow(int)
   */
  ExpressionList<T> setFirstRow(int firstRow);

  /**
   * Set the maximum number of rows to fetch.
   *
   * @see Query#setMaxRows(int)
   */
  ExpressionList<T> setMaxRows(int maxRows);

  /**
   * Set the name of the property which values become the key of a map.
   *
   * @see Query#setMapKey(String)
   */
  Query<T> setMapKey(String mapKey);

  /**
   * Set to true when this query should use the bean cache.
   * <p>
   * This is now the same as setUseBeanCache(CacheMode.ON) and will be deprecated.
   * </p>
   *
   * @see Query#setUseCache(boolean)
   */
  Query<T> setUseCache(boolean useCache);

  /**
   * Set the mode to use the bean cache when executing this query.
   *
   * @see Query#setBeanCacheMode(CacheMode)
   */
  Query<T> setBeanCacheMode(CacheMode beanCacheMode);

  /**
   * Set the {@link CacheMode} to use the query cache for executing this query.
   *
   * @see Query#setUseQueryCache(boolean)
   */
  Query<T> setUseQueryCache(CacheMode useCache);

  /**
   * Extended version for setDistinct in conjunction with "findSingleAttributeList";
   * <pre>{@code
   *
   *  List<CountedValue<Order.Status>> orderStatusCount =
   *
   *     DB.find(Order.class)
   *      .select("status")
   *      .where()
   *      .gt("orderDate", LocalDate.now().minusMonths(3))
   *
   *      // fetch as single attribute with a COUNT
   *      .setCountDistinct(CountDistinctOrder.COUNT_DESC_ATTR_ASC)
   *      .findSingleAttributeList();
   *
   *     for (CountedValue<Order.Status> entry : orderStatusCount) {
   *       System.out.println(" count:" + entry.getCount()+" orderStatus:" + entry.getValue() );
   *     }
   *
   *   // produces
   *
   *   count:3 orderStatus:NEW
   *   count:1 orderStatus:SHIPPED
   *   count:1 orderStatus:COMPLETE
   *
   * }</pre>
   */
  Query<T> setCountDistinct(CountDistinctOrder orderBy);

  /**
   * Calls {@link #setUseQueryCache(CacheMode)} with <code>ON</code> or <code>OFF</code>.
   *
   * @see Query#setUseQueryCache(CacheMode)
   */
  default Query<T> setUseQueryCache(boolean enabled) {
    return setUseQueryCache(enabled ? CacheMode.ON : CacheMode.OFF);
  }

  /**
   * Set to true if this query should execute against the doc store.
   * <p>
   * When setting this you may also consider disabling lazy loading.
   * </p>
   */
  Query<T> setUseDocStore(boolean useDocsStore);

  /**
   * Set true if you want to disable lazy loading.
   * <p>
   * That is, once the object graph is returned further lazy loading is disabled.
   * </p>
   */
  Query<T> setDisableLazyLoading(boolean disableLazyLoading);

  /**
   * Disable read auditing for this query.
   * <p>
   * This is intended to be used when the query is not a user initiated query and instead
   * part of the internal processing in an application to load a cache or document store etc.
   * In these cases we don't want the query to be part of read auditing.
   * </p>
   */
  Query<T> setDisableReadAuditing();

  /**
   * Set a label on the query (to help identify query execution statistics).
   */
  Query<T> setLabel(String label);

  /**
   * Add expressions to the having clause.
   * <p>
   * The having clause is only used for queries based on raw sql (via SqlSelect
   * annotation etc).
   * </p>
   */
  ExpressionList<T> having();

  /**
   * Add another expression to the where clause.
   */
  ExpressionList<T> where();

  /**
   * Add the expressions to this expression list.
   *
   * @param expressions The expressions that are parsed and added to this expression list
   * @param params      Bind parameters to match ? or ?1 bind positions.
   */
  ExpressionList<T> where(String expressions, Object... params);

  /**
   * Path exists - for the given path in a JSON document.
   * <pre>{@code
   *
   *   where().jsonExists("content", "path.other")
   *
   * }</pre>
   *
   * @param propertyName the property that holds a JSON document
   * @param path         the nested path in the JSON document in dot notation
   */
  ExpressionList<T> jsonExists(String propertyName, String path);

  /**
   * Path does not exist - for the given path in a JSON document.
   * <pre>{@code
   *
   *   where().jsonNotExists("content", "path.other")
   *
   * }</pre>
   *
   * @param propertyName the property that holds a JSON document
   * @param path         the nested path in the JSON document in dot notation
   */
  ExpressionList<T> jsonNotExists(String propertyName, String path);

  /**
   * Equal to expression for the value at the given path in the JSON document.
   * <pre>{@code
   *
   *   where().jsonEqualTo("content", "path.other", 34)
   *
   * }</pre>
   *
   * @param propertyName the property that holds a JSON document
   * @param path         the nested path in the JSON document in dot notation
   * @param value        the value used to test against the document path's value
   */
  ExpressionList<T> jsonEqualTo(String propertyName, String path, Object value);

  /**
   * Not Equal to - for the given path in a JSON document.
   * <pre>{@code
   *
   *   where().jsonNotEqualTo("content", "path.other", 34)
   *
   * }</pre>
   *
   * @param propertyName the property that holds a JSON document
   * @param path         the nested path in the JSON document in dot notation
   * @param value        the value used to test against the document path's value
   */
  ExpressionList<T> jsonNotEqualTo(String propertyName, String path, Object value);

  /**
   * Greater than - for the given path in a JSON document.
   * <pre>{@code
   *
   *   where().jsonGreaterThan("content", "path.other", 34)
   *
   * }</pre>
   */
  ExpressionList<T> jsonGreaterThan(String propertyName, String path, Object value);

  /**
   * Greater than or equal to - for the given path in a JSON document.
   * <pre>{@code
   *
   *   where().jsonGreaterOrEqual("content", "path.other", 34)
   *
   * }</pre>
   */
  ExpressionList<T> jsonGreaterOrEqual(String propertyName, String path, Object value);

  /**
   * Less than - for the given path in a JSON document.
   * <pre>{@code
   *
   *   where().jsonLessThan("content", "path.other", 34)
   *
   * }</pre>
   */
  ExpressionList<T> jsonLessThan(String propertyName, String path, Object value);

  /**
   * Less than or equal to - for the given path in a JSON document.
   * <pre>{@code
   *
   *   where().jsonLessOrEqualTo("content", "path.other", 34)
   *
   * }</pre>
   */
  ExpressionList<T> jsonLessOrEqualTo(String propertyName, String path, Object value);

  /**
   * Between - for the given path in a JSON document.
   * <pre>{@code
   *
   *   where().jsonBetween("content", "orderDate", lowerDateTime, upperDateTime)
   *
   * }</pre>
   */
  ExpressionList<T> jsonBetween(String propertyName, String path, Object lowerValue, Object upperValue);

  /**
   * Add an Expression to the list.
   */
  ExpressionList<T> add(Expression expr);

  /**
   * Add a list of Expressions to this ExpressionList.s
   */
  ExpressionList<T> addAll(ExpressionList<T> exprList);

  /**
   * Equal To the result of a sub-query.
   */
  ExpressionList<T> eq(String propertyName, Query<?> subQuery);

  /**
   * Equal To - property is equal to a given value.
   */
  ExpressionList<T> eq(String propertyName, Object value);

  /**
   * Is EQUAL TO if value is non-null and otherwise no expression is added to the query.
   * <p>
   * This is the EQUAL TO equivalent to {@link #inOrEmpty(String, Collection)} where the expression/predicate
   * is only added when the value is non-null.
   * <p>
   * This is effectively a helper method that allows a query to be built in fluid style where some predicates are
   * effectively optional. We can use <code>eqIfPresent()</code> rather than having a separate if block.
   * <p>
   * Another option is to instead globally use {@link io.ebean.config.DatabaseConfig#setExpressionEqualsWithNullAsNoop(boolean)}
   * but that is not always desirable.
   */
  ExpressionList<T> eqIfPresent(String propertyName, @Nullable Object value);

  /**
   * Equal To or Null - property is equal to a given value or null.
   */
  ExpressionList<T> eqOrNull(String propertyName, Object value);

  /**
   * Not Equal To the result of a sub-query.
   */
  ExpressionList<T> ne(String propertyName, Query<?> subQuery);

  /**
   * Not Equal To - property not equal to the given value.
   */
  ExpressionList<T> ne(String propertyName, Object value);

  /**
   * Case Insensitive Equal To - property equal to the given value (typically
   * using a lower() function to make it case insensitive).
   */
  ExpressionList<T> ieq(String propertyName, String value);

  /**
   * Case Insensitive Not Equal To - property not equal to the given value (typically
   * using a lower() function to make it case insensitive).
   */
  ExpressionList<T> ine(String propertyName, String value);

  /**
   * Value in Range between 2 properties.
   *
   * <pre>{@code
   *
   *    .startDate.inRangeWith(endDate, now)
   *
   *    // which equates to
   *    startDate <= now and (endDate > now or endDate is null)
   *
   * }</pre>
   *
   * <p>
   * This is a convenience expression combining a number of simple expressions.
   * The most common use of this could be called "effective dating" where 2 date or
   * timestamp columns represent the date range in which
   */
  ExpressionList<T> inRangeWith(String lowProperty, String highProperty, Object value);

  /**
   * A Property is in Range between 2 properties.
   *
   * <pre>{@code
   *
   *    .orderDate.inRangeWith(QOrder.Alias.product.startDate, QOrder.Alias.product.endDate)
   *
   *    // which equates to
   *    product.startDate <= orderDate and (orderDate < product.endDate or product.endDate is null)
   *
   * }</pre>
   *
   * <p>
   * This is a convenience expression combining a number of simple expressions.
   */
  ExpressionList<T> inRangeWithProperties(String propertyName, String lowProperty, String highProperty);

  /**
   * In Range - {@code property >= value1 and property < value2}.
   * <p>
   * Unlike Between inRange is "half open" and usually more useful for use with dates or timestamps.
   * </p>
   */
  ExpressionList<T> inRange(String propertyName, Object value1, Object value2);

  /**
   * Between - property between the two given values.
   */
  ExpressionList<T> between(String propertyName, Object value1, Object value2);

  /**
   * Between - value between the two properties.
   */
  ExpressionList<T> betweenProperties(String lowProperty, String highProperty, Object value);

  /**
   * Greater Than the result of a sub-query.
   */
  ExpressionList<T> gt(String propertyName, Query<?> subQuery);

  /**
   * Greater Than - property greater than the given value.
   */
  ExpressionList<T> gt(String propertyName, Object value);

  /**
   * Greater Than or Null - property greater than the given value or null.
   */
  ExpressionList<T> gtOrNull(String propertyName, Object value);

  /**
   * Is GREATER THAN if value is non-null and otherwise no expression is added to the query.
   * <p>
   * This is effectively a helper method that allows a query to be built in fluid style where some predicates are
   * effectively optional. We can use <code>gtIfPresent()</code> rather than having a separate if block.
   */
  ExpressionList<T> gtIfPresent(String propertyName, @Nullable Object value);

  /**
   * Greater Than or Equal to the result of a sub-query.
   */
  ExpressionList<T> ge(String propertyName, Query<?> subQuery);

  /**
   * Greater Than or Equal to - property greater than or equal to the given
   * value.
   */
  ExpressionList<T> ge(String propertyName, Object value);

  /**
   * Greater Than or Equal to OR Null - ({@code >= or null }).
   */
  ExpressionList<T> geOrNull(String propertyName, Object value);


  /**
   * Is GREATER THAN OR EQUAL TO if value is non-null and otherwise no expression is added to the query.
   * <p>
   * This is effectively a helper method that allows a query to be built in fluid style where some predicates are
   * effectively optional. We can use <code>geIfPresent()</code> rather than having a separate if block.
   */
  ExpressionList<T> geIfPresent(String propertyName, @Nullable Object value);

  /**
   * Less Than the result of a sub-query.
   */
  ExpressionList<T> lt(String propertyName, Query<?> subQuery);

  /**
   * Less Than - property less than the given value.
   */
  ExpressionList<T> lt(String propertyName, Object value);

  /**
   * Less Than or Null - property less than the given value or null.
   */
  ExpressionList<T> ltOrNull(String propertyName, Object value);

  /**
   * Is LESS THAN if value is non-null and otherwise no expression is added to the query.
   * <p>
   * This is effectively a helper method that allows a query to be built in fluid style where some predicates are
   * effectively optional. We can use <code>ltIfPresent()</code> rather than having a separate if block.
   */
  ExpressionList<T> ltIfPresent(String propertyName, @Nullable Object value);

  /**
   * Less Than or Equal to the result of a sub-query.
   */
  ExpressionList<T> le(String propertyName, Query<?> subQuery);

  /**
   * Less Than or Equal to - property less than or equal to the given value.
   */
  ExpressionList<T> le(String propertyName, Object value);

  /**
   * Less Than or Equal to OR Null - ({@code <= or null }).
   */
  ExpressionList<T> leOrNull(String propertyName, Object value);

  /**
   * Is LESS THAN OR EQUAL TO if value is non-null and otherwise no expression is added to the query.
   * <p>
   * This is effectively a helper method that allows a query to be built in fluid style where some predicates are
   * effectively optional. We can use <code>leIfPresent()</code> rather than having a separate if block.
   */
  ExpressionList<T> leIfPresent(String propertyName, @Nullable Object value);

  /**
   * Is Null - property is null.
   */
  ExpressionList<T> isNull(String propertyName);

  /**
   * Is Not Null - property is not null.
   */
  ExpressionList<T> isNotNull(String propertyName);

  /**
   * A "Query By Example" type of expression.
   * <p>
   * Pass in an example entity and for each non-null scalar properties an
   * expression is added.
   * </p>
   * <p>
   * By Default this case sensitive, will ignore numeric zero values and will
   * use a Like for string values (you must put in your own wildcards).
   * </p>
   * <p>
   * To get control over the options you can create an ExampleExpression and set
   * those options such as case insensitive etc.
   * </p>
   * <pre>{@code
   *
   * // create an example bean and set the properties
   * // with the query parameters you want
   * Customer example = new Customer();
   * example.setName("Rob%");
   * example.setNotes("%something%");
   *
   * List<Customer> list =
   *   DB.find(Customer.class)
   *     .where().exampleLike(example)
   *     .findList();
   *
   * }</pre>
   * <p>
   * Similarly you can create an ExampleExpression
   * </p>
   * <pre>{@code
   *
   * Customer example = new Customer();
   * example.setName("Rob%");
   * example.setNotes("%something%");
   *
   * // create a ExampleExpression with more control
   * ExampleExpression qbe = new ExampleExpression(example, true, LikeType.EQUAL_TO).includeZeros();
   *
   * List<Customer> list = DB.find(Customer.class).where().add(qbe).findList();
   *
   * }</pre>
   */
  ExpressionList<T> exampleLike(Object example);

  /**
   * Case insensitive version of {@link #exampleLike(Object)}
   */
  ExpressionList<T> iexampleLike(Object example);

  /**
   * Like - property like value where the value contains the SQL wild card
   * characters % (percentage) and _ (underscore).
   */
  ExpressionList<T> like(String propertyName, String value);

  /**
   * Case insensitive Like - property like value where the value contains the
   * SQL wild card characters % (percentage) and _ (underscore). Typically uses
   * a lower() function to make the expression case insensitive.
   */
  ExpressionList<T> ilike(String propertyName, String value);

  /**
   * Starts With - property like value%.
   */
  ExpressionList<T> startsWith(String propertyName, String value);

  /**
   * Case insensitive Starts With - property like value%. Typically uses a
   * lower() function to make the expression case insensitive.
   */
  ExpressionList<T> istartsWith(String propertyName, String value);

  /**
   * Ends With - property like %value.
   */
  ExpressionList<T> endsWith(String propertyName, String value);

  /**
   * Case insensitive Ends With - property like %value. Typically uses a lower()
   * function to make the expression case insensitive.
   */
  ExpressionList<T> iendsWith(String propertyName, String value);

  /**
   * Contains - property like %value%.
   */
  ExpressionList<T> contains(String propertyName, String value);

  /**
   * Case insensitive Contains - property like %value%. Typically uses a lower()
   * function to make the expression case insensitive.
   */
  ExpressionList<T> icontains(String propertyName, String value);

  /**
   * In expression using pairs of value objects.
   */
  ExpressionList<T> inPairs(Pairs pairs);

  /**
   * In expression using multiple columns.
   */
  ExpressionList<T> inTuples(InTuples pairs);

  /**
   * EXISTS a raw SQL SubQuery.
   *
   * @param sqlSubQuery The SQL SubQuery
   * @param bindValues  Optional bind values if the SubQuery uses {@code ? } bind values.
   */
  ExpressionList<T> exists(String sqlSubQuery, Object... bindValues);

  /**
   * Not EXISTS a raw SQL SubQuery.
   *
   * @param sqlSubQuery The SQL SubQuery
   * @param bindValues  Optional bind values if the SubQuery uses {@code ? } bind values.
   */
  ExpressionList<T> notExists(String sqlSubQuery, Object... bindValues);

  /**
   * IN a raw SQL SubQuery.
   *
   * @param propertyName The bean property
   * @param sqlSubQuery  The SQL SubQuery
   * @param bindValues   Optional bind values if the SubQuery uses {@code ? } bind values.
   */
  ExpressionList<T> inSubQuery(String propertyName, String sqlSubQuery, Object... bindValues);

  /**
   * Not IN a raw SQL SubQuery.
   *
   * @param propertyName The bean property
   * @param sqlSubQuery  The SQL SubQuery
   * @param bindValues   Optional bind values if the SubQuery uses {@code ? } bind values.
   */
  ExpressionList<T> notInSubQuery(String propertyName, String sqlSubQuery, Object... bindValues);

  /**
   * Equal To a raw SQL SubQuery.
   *
   * @param propertyName The bean property
   * @param sqlSubQuery  The SQL SubQuery
   * @param bindValues   Optional bind values if the SubQuery uses {@code ? } bind values.
   */
  ExpressionList<T> eqSubQuery(String propertyName, String sqlSubQuery, Object... bindValues);

  /**
   * Not Equal To a raw SQL SubQuery.
   *
   * @param propertyName The bean property
   * @param sqlSubQuery  The SQL SubQuery
   * @param bindValues   Optional bind values if the SubQuery uses {@code ? } bind values.
   */
  ExpressionList<T> neSubQuery(String propertyName, String sqlSubQuery, Object... bindValues);

  /**
   * Greater Than a raw SQL SubQuery.
   *
   * @param propertyName The bean property
   * @param sqlSubQuery  The SQL SubQuery
   * @param bindValues   Optional bind values if the SubQuery uses {@code ? } bind values.
   */
  ExpressionList<T> gtSubQuery(String propertyName, String sqlSubQuery, Object... bindValues);

  /**
   * Greater Than or Equal To a raw SQL SubQuery.
   *
   * @param propertyName The bean property
   * @param sqlSubQuery  The SQL SubQuery
   * @param bindValues   Optional bind values if the SubQuery uses {@code ? } bind values.
   */
  ExpressionList<T> geSubQuery(String propertyName, String sqlSubQuery, Object... bindValues);

  /**
   * Less Than a raw SQL SubQuery.
   *
   * @param propertyName The bean property
   * @param sqlSubQuery  The SQL SubQuery
   * @param bindValues   Optional bind values if the SubQuery uses {@code ? } bind values.
   */
  ExpressionList<T> ltSubQuery(String propertyName, String sqlSubQuery, Object... bindValues);

  /**
   * Less Than or Equal To a raw SQL SubQuery.
   *
   * @param propertyName The bean property
   * @param sqlSubQuery  The SQL SubQuery
   * @param bindValues   Optional bind values if the SubQuery uses {@code ? } bind values.
   */
  ExpressionList<T> leSubQuery(String propertyName, String sqlSubQuery, Object... bindValues);

  /**
   * In - using a subQuery.
   */
  ExpressionList<T> in(String propertyName, Query<?> subQuery);

  /**
   * In - property has a value in the array of values.
   */
  ExpressionList<T> in(String propertyName, Object... values);

  /**
   * In - property has a value in the collection of values.
   */
  ExpressionList<T> in(String propertyName, Collection<?> values);

  /**
   * In where null or empty values means that no predicate is added to the query.
   * <p>
   * That is, only add the IN predicate if the values are not null or empty.
   * <p>
   * Without this we typically need to code an <code>if</code> block to only add
   * the IN predicate if the collection is not empty like:
   * </p>
   *
   * <h3>Without inOrEmpty()</h3>
   * <pre>{@code
   *
   *   query.where() // add some predicates
   *     .eq("status", Status.NEW);
   *
   *   if (ids != null && !ids.isEmpty()) {
   *     query.where().in("customer.id", ids);
   *   }
   *
   *   query.findList();
   *
   * }</pre>
   *
   * <h3>Using inOrEmpty()</h3>
   * <pre>{@code
   *
   *   query.where()
   *     .eq("status", Status.NEW)
   *     .inOrEmpty("customer.id", ids)
   *     .findList();
   *
   * }</pre>
   */
  ExpressionList<T> inOrEmpty(String propertyName, Collection<?> values);

  /**
   * In - using a subQuery.
   * <p>
   * This is exactly the same as in() and provided due to "in" being a Kotlin keyword
   * (and hence to avoid the slightly ugly escaping when using in() in Kotlin)
   */
  default ExpressionList<T> isIn(String propertyName, Query<?> subQuery) {
    return in(propertyName, subQuery);
  }

  /**
   * In - property has a value in the array of values.
   * <p>
   * This is exactly the same as in() and provided due to "in" being a Kotlin keyword
   * (and hence to avoid the slightly ugly escaping when using in() in Kotlin)
   */
  default ExpressionList<T> isIn(String propertyName, Object... values) {
    return in(propertyName, values);
  }

  /**
   * In - property has a value in the collection of values.
   * <p>
   * This is exactly the same as in() and provided due to "in" being a Kotlin keyword
   * (and hence to avoid the slightly ugly escaping when using in() in Kotlin)
   */
  default ExpressionList<T> isIn(String propertyName, Collection<?> values) {
    return in(propertyName, values);
  }

  /**
   * Not In - property has a value in the array of values.
   */
  ExpressionList<T> notIn(String propertyName, Object... values);

  /**
   * Not In - property has a value in the collection of values.
   */
  ExpressionList<T> notIn(String propertyName, Collection<?> values);

  /**
   * Not In - using a subQuery.
   */
  ExpressionList<T> notIn(String propertyName, Query<?> subQuery);

  /**
   * Is empty expression for collection properties.
   */
  ExpressionList<T> isEmpty(String propertyName);

  /**
   * Is not empty expression for collection properties.
   */
  ExpressionList<T> isNotEmpty(String propertyName);

  /**
   * Exists expression
   */
  ExpressionList<T> exists(Query<?> subQuery);

  /**
   * Not exists expression
   */
  ExpressionList<T> notExists(Query<?> subQuery);

  /**
   * Id IN a list of id values.
   */
  ExpressionList<T> idIn(Object... idValues);

  /**
   * Id IN a collection of id values.
   */
  ExpressionList<T> idIn(Collection<?> idValues);

  /**
   * Id Equal to - ID property is equal to the value.
   */
  ExpressionList<T> idEq(Object value);

  /**
   * All Equal - Map containing property names and their values.
   * <p>
   * Expression where all the property names in the map are equal to the
   * corresponding value.
   * </p>
   *
   * @param propertyMap a map keyed by property names.
   */
  ExpressionList<T> allEq(Map<String, Object> propertyMap);

  /**
   * Array property contains entries with the given values.
   */
  ExpressionList<T> arrayContains(String propertyName, Object... values);

  /**
   * Array does not contain the given values.
   * <p>
   * Array support is effectively limited to Postgres at this time.
   * </p>
   */
  ExpressionList<T> arrayNotContains(String propertyName, Object... values);

  /**
   * Array is empty - for the given array property.
   * <p>
   * Array support is effectively limited to Postgres at this time.
   * </p>
   */
  ExpressionList<T> arrayIsEmpty(String propertyName);

  /**
   * Array is not empty - for the given array property.
   * <p>
   * Array support is effectively limited to Postgres at this time.
   * </p>
   */
  ExpressionList<T> arrayIsNotEmpty(String propertyName);

  /**
   * Add expression for ANY of the given bit flags to be set.
   * <pre>{@code
   *
   * where().bitwiseAny("flags", BwFlags.HAS_BULK + BwFlags.HAS_COLOUR)
   *
   * }</pre>
   *
   * @param propertyName The property that holds the flags value
   * @param flags        The flags we are looking for
   */
  ExpressionList<T> bitwiseAny(String propertyName, long flags);

  /**
   * Add expression for ALL of the given bit flags to be set.
   * <pre>{@code
   *
   * where().bitwiseAll("flags", BwFlags.HAS_BULK + BwFlags.HAS_COLOUR)
   *
   * }</pre>
   *
   * @param propertyName The property that holds the flags value
   * @param flags        The flags we are looking for
   */
  ExpressionList<T> bitwiseAll(String propertyName, long flags);

  /**
   * Add expression for the given bit flags to be NOT set.
   * <pre>{@code
   *
   * where().bitwiseNot("flags", BwFlags.HAS_COLOUR)
   *
   * }</pre>
   *
   * @param propertyName The property that holds the flags value
   * @param flags        The flags we are looking for
   */
  ExpressionList<T> bitwiseNot(String propertyName, long flags);

  /**
   * Add bitwise AND expression of the given bit flags to compare with the match/mask.
   * <p>
   * <pre>{@code
   *
   * // Flags Bulk + Size = Size
   * // ... meaning Bulk is not set and Size is set
   *
   * long selectedFlags = BwFlags.HAS_BULK + BwFlags.HAS_SIZE;
   * long mask = BwFlags.HAS_SIZE; // Only Size flag set
   *
   * where().bitwiseAnd("flags", selectedFlags, mask)
   *
   * }</pre>
   *
   * @param propertyName The property that holds the flags value
   * @param flags        The flags we are looking for
   */
  ExpressionList<T> bitwiseAnd(String propertyName, long flags, long match);

  /**
   * Add raw expression with a single parameter.
   * <p>
   * The raw expression should contain a single ? or ?1
   * at the location of the parameter.  We use ?1 when binding a
   * collection for an IN expression.
   * <p>
   * When properties in the clause are fully qualified as table-column names
   * then they are not translated. logical property name names (not fully
   * qualified) will still be translated to their physical name.
   * <p>
   * <h4>Examples:</h4>
   * <pre>{@code
   *
   *   // use a database function
   *   raw("add_days(orderDate, 10) < ?", someDate)
   *
   *   raw("name like ?", "Rob%")
   *
   *   raw("name in (?1)", asList("Rob", "Fiona", "Jack"))
   *
   *   raw("name = any(?)", asList("Rob", "Fiona", "Jack"))
   *
   * }</pre>
   *
   * <h4>Subquery examples:</h4>
   * <pre>{@code
   *
   *   // Bind collection using ?1
   *   .raw("id in (select c.id from o_customer c where c.name in (?1))", asList("Rob", "Fiona", "Jack"))
   *
   *   // Using Postgres ANY expression
   *   .raw("t0.customer_id in (select customer_id from customer_group where group_id = any(?::uuid[]))", groupIds)
   *
   * }</pre>
   */
  ExpressionList<T> raw(String raw, Object value);

  /**
   * Add raw expression with an array of parameters.
   * <p>
   * The raw expression should contain the same number of ? or ?1, ?2 ... bind parameters
   * as there are values. We use ?1, ?2 etc when binding a collection for an IN expression.
   * <p>
   * When properties in the clause are fully qualified as table-column names
   * then they are not translated. logical property name names (not fully
   * qualified) will still be translated to their physical name.
   * </p>
   *
   * <h4>Examples:</h4>
   * <pre>{@code
   *
   *   raw("unitPrice > ? and product.id > ?", 2, 3)
   *
   *   raw("(status = ? or (orderDate < ? and shipDate is null) or customer.name like ?)",
   *         Order.Status.APPROVED,
   *         new Timestamp(System.currentTimeMillis()),
   *         "Rob")
   *
   * }</pre></pre>
   */
  ExpressionList<T> raw(String raw, Object... values);

  /**
   * Add raw expression with no parameters.
   * <p>
   * When properties in the clause are fully qualified as table-column names
   * then they are not translated. logical property name names (not fully
   * qualified) will still be translated to their physical name.
   * </p>
   * <pre>{@code
   *
   *   raw("orderQty < shipQty")
   *
   * }</pre>
   *
   * <h4>Subquery example:</h4>
   * <pre>{@code
   *
   *   .raw("t0.customer_id in (select customer_id from customer_group where group_id = any(?::uuid[]))", groupIds)
   *
   * }</pre>
   */
  ExpressionList<T> raw(String raw);

  /**
   * Only add the raw expression if the values is not null or empty.
   * <p>
   * This is a pure convenience expression to make it nicer to deal with the pattern where we use
   * raw() expression with a subquery and only want to add the subquery predicate when the collection
   * of values is not empty.
   * </p>
   * <h3>Without inOrEmpty()</h3>
   * <pre>{@code
   *
   *   query.where() // add some predicates
   *     .eq("status", Status.NEW);
   *
   *   // common pattern - we can use rawOrEmpty() instead
   *   if (orderIds != null && !orderIds.isEmpty()) {
   *     query.where().raw("t0.customer_id in (select o.customer_id from orders o where o.id in (?1))", orderIds);
   *   }
   *
   *   query.findList();
   *
   * }</pre>
   *
   * <h3>Using rawOrEmpty()</h3>
   * Note that in the example below we use the <code>?1</code> bind parameter to get  "parameter expansion"
   * for each element in the collection.
   *
   * <pre>{@code
   *
   *   query.where()
   *     .eq("status", Status.NEW)
   *     // only add the expression if orderIds is not empty
   *     .rawOrEmpty("t0.customer_id in (select o.customer_id from orders o where o.id in (?1))", orderIds);
   *     .findList();
   *
   * }</pre>
   *
   * <h3>Postgres ANY</h3>
   * With Postgres we would often use the SQL <code>ANY</code> expression and array parameter binding
   * rather than <code>IN</code>.
   *
   * <pre>{@code
   *
   *   query.where()
   *     .eq("status", Status.NEW)
   *     .rawOrEmpty("t0.customer_id in (select o.customer_id from orders o where o.id = any(?))", orderIds);
   *     .findList();
   *
   * }</pre>
   * <p>
   * Note that we need to cast the Postgres array for UUID types like:
   * </p>
   * <pre>{@code
   *
   *   " ... = any(?::uuid[])"
   *
   * }</pre>
   *
   * @param raw    The raw expression that is typically a subquery
   * @param values The values which is typically a list or set of id values.
   */
  ExpressionList<T> rawOrEmpty(String raw, Collection<?> values);

  /**
   * Add a match expression.
   *
   * @param propertyName The property name for the match
   * @param search       The search value
   */
  ExpressionList<T> match(String propertyName, String search);

  /**
   * Add a match expression with options.
   *
   * @param propertyName The property name for the match
   * @param search       The search value
   */
  ExpressionList<T> match(String propertyName, String search, Match options);

  /**
   * Add a multi-match expression.
   */
  ExpressionList<T> multiMatch(String search, String... properties);

  /**
   * Add a multi-match expression using options.
   */
  ExpressionList<T> multiMatch(String search, MultiMatch options);

  /**
   * Add a simple query string expression.
   */
  ExpressionList<T> textSimple(String search, TextSimple options);

  /**
   * Add a query string expression.
   */
  ExpressionList<T> textQueryString(String search, TextQueryString options);

  /**
   * Add common terms expression.
   */
  ExpressionList<T> textCommonTerms(String search, TextCommonTerms options);

  /**
   * And - join two expressions with a logical and.
   */
  ExpressionList<T> and(Expression expOne, Expression expTwo);

  /**
   * Or - join two expressions with a logical or.
   */
  ExpressionList<T> or(Expression expOne, Expression expTwo);

  /**
   * Negate the expression (prefix it with NOT).
   */
  ExpressionList<T> not(Expression exp);

  /**
   * Start a list of expressions that will be joined by AND's
   * returning the expression list the expressions are added to.
   * <p>
   * This is exactly the same as conjunction();
   * </p>
   * <p>
   * Use endAnd() or endJunction() to end the AND junction.
   * </p>
   * <p>
   * Note that a where() clause defaults to an AND junction so
   * typically you only explicitly need to use the and() junction
   * when it is nested inside an or() or not() junction.
   * </p>
   * <pre>{@code
   *
   *  // Example: Nested and()
   *
   *    .where()
   *    .or()
   *      .and() // nested and
   *        .startsWith("name", "r")
   *        .eq("anniversary", onAfter)
   *        .endAnd()
   *      .and()
   *        .eq("status", Customer.Status.ACTIVE)
   *        .gt("id", 0)
   *        .endAnd()
   *      .orderBy().asc("name")
   *      .findList();
   * }</pre>
   */
  Junction<T> and();

  /**
   * Return a list of expressions that will be joined by OR's.
   * This is exactly the same as disjunction();
   * <p>
   * Use endOr() or endJunction() to end the OR junction.
   * </p>
   *
   * <pre>{@code
   *
   *  // Example: (status active OR anniversary is null)
   *
   *    .where()
   *    .or()
   *      .eq("status", Customer.Status.ACTIVE)
   *      .isNull("anniversary")
   *    .orderBy().asc("name")
   *    .findList();
   *
   * }</pre>
   *
   * <pre>{@code
   *
   *  // Example: Use or() to join
   *  // two nested and() expressions
   *
   *    .where()
   *    .or()
   *      .and()
   *        .startsWith("name", "r")
   *        .eq("anniversary", onAfter)
   *        .endAnd()
   *      .and()
   *        .eq("status", Customer.Status.ACTIVE)
   *        .gt("id", 0)
   *        .endAnd()
   *      .orderBy().asc("name")
   *      .findList();
   *
   * }</pre>
   */
  Junction<T> or();

  /**
   * Return a list of expressions that will be wrapped by NOT.
   * <p>
   * Use endNot() or endJunction() to end expressions being added to the
   * NOT expression list.
   * </p>
   *
   * <pre>{@code
   *
   *    .where()
   *      .not()
   *        .gt("id", 1)
   *        .eq("anniversary", onAfter)
   *        .endNot()
   *
   * }</pre>
   *
   * <pre>{@code
   *
   * // Example: nested not()
   *
   *   .where()
   *     .eq("status", Customer.Status.ACTIVE)
   *     .not()
   *       .gt("id", 1)
   *       .eq("anniversary", onAfter)
   *       .endNot()
   *     .orderBy()
   *       .asc("name")
   *     .findList();
   *
   * }</pre>
   */
  Junction<T> not();

  /**
   * Start (and return) a list of expressions that will be joined by AND's.
   * <p>
   * This is the same as and().
   * </p>
   */
  Junction<T> conjunction();

  /**
   * Start (and return) a list of expressions that will be joined by OR's.
   * <p>
   * This is the same as or().
   * </p>
   */
  Junction<T> disjunction();

  /**
   * Start a list of expressions that will be joined by MUST.
   * <p>
   * This automatically makes the query a useDocStore(true) query that
   * will execute against the document store (ElasticSearch etc).
   * </p>
   * <p>
   * This is logically similar to and().
   * </p>
   */
  Junction<T> must();

  /**
   * Start a list of expressions that will be joined by SHOULD.
   * <p>
   * This automatically makes the query a useDocStore(true) query that
   * will execute against the document store (ElasticSearch etc).
   * </p>
   * <p>
   * This is logically similar to or().
   * </p>
   */
  Junction<T> should();

  /**
   * Start a list of expressions that will be joined by MUST NOT.
   * <p>
   * This automatically makes the query a useDocStore(true) query that
   * will execute against the document store (ElasticSearch etc).
   * </p>
   * <p>
   * This is logically similar to not().
   * </p>
   */
  Junction<T> mustNot();

  /**
   * End a junction returning the parent expression list.
   * <p>
   * Ends a and(), or(), not(), must(), mustNot() or should() junction
   * such that you get the parent expression.
   * </p>
   * <p>
   * Alternatively you can always use where() to return the top level expression list.
   * </p>
   */
  ExpressionList<T> endJunction();

  /**
   * End a AND junction - synonym for endJunction().
   */
  ExpressionList<T> endAnd();

  /**
   * End a OR junction - synonym for endJunction().
   */
  ExpressionList<T> endOr();

  /**
   * End a NOT junction - synonym for endJunction().
   */
  ExpressionList<T> endNot();

  /**
   * Clears the current expression list.
   */
  ExpressionList<T> clear();
}

package io.ebean;

import io.ebean.search.Match;
import io.ebean.search.MultiMatch;
import io.ebean.search.TextCommonTerms;
import io.ebean.search.TextQueryString;
import io.ebean.search.TextSimple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.NonUniqueResultException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
 * as findList() and orderBy(). The purpose of these methods is provide a fluid
 * API. The upside of this approach is that you can build and execute a query
 * via chained methods. The down side is that this ExpressionList object has
 * more methods than you would initially expect (the ones duplicated from
 * Query).
 * </p>
 *
 * @see Query#where()
 */
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
   * Set the order by clause replacing the existing order by clause if there is
   * one.
   * <p>
   * This follows SQL syntax using commas between each property with the
   * optional asc and desc keywords representing ascending and descending order
   * respectively.
   * </p>
   * <p>
   * This is EXACTLY the same as {@link #orderBy(String)}.
   * </p>
   */
  Query<T> order(String orderByClause);

  /**
   * Return the OrderBy so that you can append an ascending or descending
   * property to the order by clause.
   * <p>
   * This will never return a null. If no order by clause exists then an 'empty'
   * OrderBy object is returned.
   * </p>
   */
  OrderBy<T> order();

  /**
   * Return the OrderBy so that you can append an ascending or descending
   * property to the order by clause.
   * <p>
   * This will never return a null. If no order by clause exists then an 'empty'
   * OrderBy object is returned.
   * </p>
   */
  OrderBy<T> orderBy();

  /**
   * Add an orderBy clause to the query.
   *
   * @see Query#orderBy(String)
   */
  Query<T> orderBy(String orderBy);

  /**
   * Add an orderBy clause to the query.
   *
   * @see Query#orderBy(String)
   */
  Query<T> setOrderBy(String orderBy);

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
   * Execute as a update query.
   *
   * @return the number of rows that were updated.
   * @see UpdateQuery
   */
  int update();

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
  @Nonnull
  List<T> findList();

  /**
   * Execute the query returning the list of Id's.
   *
   * @see Query#findIds()
   */
  @Nonnull
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
  @Nonnull
  Set<T> findSet();

  /**
   * Execute the query returning a map.
   *
   * @see Query#findMap()
   */
  @Nonnull
  <K> Map<K, T> findMap();

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
   * <p>
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
   */
  @Nonnull
  <A> List<A> findSingleAttributeList();

  /**
   * Execute a query returning a single value of a single property/column.
   * <p>
   * <pre>{@code
   *
   *  String name =
   *    Ebean.find(Customer.class)
   *      .select("name")
   *      .where().eq("id", 42)
   *      .findSingleAttribute();
   *
   * }</pre>
   */
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
  @Nonnull
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
  @Nonnull
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
  @Nonnull
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
  @Nonnull
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
  PagedList<T> findPagedList();

  /**
   * Return versions of a @History entity bean.
   * <p>
   * Generally this query is expected to be a find by id or unique predicates query.
   * It will execute the query against the history returning the versions of the bean.
   * </p>
   */
  @Nonnull
  List<Version<T>> findVersions();

  /**
   * Return versions of a @History entity bean between the 2 timestamps.
   * <p>
   * Generally this query is expected to be a find by id or unique predicates query.
   * It will execute the query against the history returning the versions of the bean.
   * </p>
   */
  @Nonnull
  List<Version<T>> findVersionsBetween(Timestamp start, Timestamp end);

  /**
   * Add some filter predicate expressions to the many property.
   */
  @Nonnull
  ExpressionList<T> filterMany(String prop);

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
   *       Ebean.find(Customer.class)
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
  Query<T> setFirstRow(int firstRow);

  /**
   * Set the maximum number of rows to fetch.
   *
   * @see Query#setMaxRows(int)
   */
  Query<T> setMaxRows(int maxRows);

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
   * <p>
   * <pre>{@code
   *
   *  List<CountedValue<Order.Status>> orderStatusCount =
   *
   *     Ebean.find(Order.class)
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
   * <p>
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
   * <p>
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
   * <p>
   * <pre>{@code
   *
   *   where().jsonGreaterThan("content", "path.other", 34)
   *
   * }</pre>
   */
  ExpressionList<T> jsonGreaterThan(String propertyName, String path, Object value);

  /**
   * Greater than or equal to - for the given path in a JSON document.
   * <p>
   * <pre>{@code
   *
   *   where().jsonGreaterOrEqual("content", "path.other", 34)
   *
   * }</pre>
   */
  ExpressionList<T> jsonGreaterOrEqual(String propertyName, String path, Object value);

  /**
   * Less than - for the given path in a JSON document.
   * <p>
   * <pre>{@code
   *
   *   where().jsonLessThan("content", "path.other", 34)
   *
   * }</pre>
   */
  ExpressionList<T> jsonLessThan(String propertyName, String path, Object value);

  /**
   * Less than or equal to - for the given path in a JSON document.
   * <p>
   * <pre>{@code
   *
   *   where().jsonLessOrEqualTo("content", "path.other", 34)
   *
   * }</pre>
   */
  ExpressionList<T> jsonLessOrEqualTo(String propertyName, String path, Object value);

  /**
   * Between - for the given path in a JSON document.
   * <p>
   * <pre>{@code
   *
   *   where().jsonBetween("content", "orderDate", lowerDateTime, upperDateTime)
   *
   * }</pre>
   */
  ExpressionList<T> jsonBetween(String propertyName, String path, Object lowerValue, Object upperValue);

  /**
   * Add an Expression to the list.
   * <p>
   * This returns the list so that add() can be chained.
   * </p>
   * <p>
   * <pre>{@code
   *
   * Query<Customer> query = Ebean.find(Customer.class);
   * query.where()
   *     .like("name","Rob%")
   *     .eq("status", Customer.ACTIVE);
   *
   * List<Customer> list = query.findList();
   * ...
   *
   * }</pre>
   */
  ExpressionList<T> add(Expression expr);

  /**
   * Add a list of Expressions to this ExpressionList.s
   */
  ExpressionList<T> addAll(ExpressionList<T> exprList);

  /**
   * Equal To - property is equal to a given value.
   */
  ExpressionList<T> eq(String propertyName, Object value);

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
   * Between - property between the two given values.
   */
  ExpressionList<T> between(String propertyName, Object value1, Object value2);

  /**
   * Between - value between the two properties.
   */
  ExpressionList<T> betweenProperties(String lowProperty, String highProperty, Object value);

  /**
   * Greater Than - property greater than the given value.
   */
  ExpressionList<T> gt(String propertyName, Object value);

  /**
   * Greater Than or Equal to - property greater than or equal to the given
   * value.
   */
  ExpressionList<T> ge(String propertyName, Object value);

  /**
   * Less Than - property less than the given value.
   */
  ExpressionList<T> lt(String propertyName, Object value);

  /**
   * Less Than or Equal to - property less than or equal to the given value.
   */
  ExpressionList<T> le(String propertyName, Object value);

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
   * <p>
   * <pre>{@code
   *
   * // create an example bean and set the properties
   * // with the query parameters you want
   * Customer example = new Customer();
   * example.setName("Rob%");
   * example.setNotes("%something%");
   *
   * List&lt;Customer&gt; list = Ebean.find(Customer.class).where()
   *     // pass the bean into the where() clause
   *     .exampleLike(example)
   *     // you can add other expressions to the same query
   *     .gt("id", 2).findList();
   *
   * }</pre>
   * <p>
   * Similarly you can create an ExampleExpression
   * <p>
   * <pre>{@code
   *
   * Customer example = new Customer();
   * example.setName("Rob%");
   * example.setNotes("%something%");
   *
   * // create a ExampleExpression with more control
   * ExampleExpression qbe = new ExampleExpression(example, true, LikeType.EQUAL_TO).includeZeros();
   *
   * List<Customer> list = Ebean.find(Customer.class).where().add(qbe).findList();
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
   * <p>
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
   * <p>
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
   * The raw expression should contain a single ? at the location of the
   * parameter.
   * </p>
   * <p>
   * When properties in the clause are fully qualified as table-column names
   * then they are not translated. logical property name names (not fully
   * qualified) will still be translated to their physical name.
   * </p>
   * <p>
   * <h4>Example:</h4>
   * <pre>{@code
   *
   *   // use a database function
   *   raw("add_days(orderDate, 10) < ?", someDate)
   *
   * }</pre>
   */
  ExpressionList<T> raw(String raw, Object value);

  /**
   * Add raw expression with an array of parameters.
   * <p>
   * The raw expression should contain the same number of ? as there are
   * parameters.
   * </p>
   * <p>
   * When properties in the clause are fully qualified as table-column names
   * then they are not translated. logical property name names (not fully
   * qualified) will still be translated to their physical name.
   * </p>
   */
  ExpressionList<T> raw(String raw, Object... values);

  /**
   * Add raw expression with no parameters.
   * <p>
   * When properties in the clause are fully qualified as table-column names
   * then they are not translated. logical property name names (not fully
   * qualified) will still be translated to their physical name.
   * </p>
   * <p>
   * <pre>{@code
   *
   *   raw("orderQty < shipQty")
   *
   * }</pre>
   */
  ExpressionList<T> raw(String raw);

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
   * <p>
   * <pre>{@code
   *
   *  // Example: Nested and()
   *
   *  Ebean.find(Customer.class)
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
   * <p>
   * Use endOr() or endJunction() to end the OR junction.
   * </p>
   * <p>
   * <pre>{@code
   *
   *  // Example: Use or() to join
   *  // two nested and() expressions
   *
   *  Ebean.find(Customer.class)
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
   * <p>
   * <pre>@{code
   *
   *    .where()
   *      .not()
   *        .gt("id", 1)
   *        .eq("anniversary", onAfter)
   *        .endNot()
   *
   * }</pre>
   * <p>
   * <pre>@{code
   *
   * // Example: nested not()
   *
   * Ebean.find(Customer.class)
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
   * End a AND junction - synonym for endJunction().
   */
  ExpressionList<T> endOr();

  /**
   * End a NOT junction - synonym for endJunction().
   */
  ExpressionList<T> endNot();

}

package com.avaje.ebean;

import com.avaje.ebean.text.PathProperties;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
public interface ExpressionList<T> extends Serializable {

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
  Query<T> apply(PathProperties pathProperties);

  /**
   * Perform an 'As of' query using history tables to return the object graph
   * as of a time in the past.
   * <p>
   *   To perform this query the DB must have underlying history tables.
   * </p>
   *
   * @param asOf the date time in the past at which you want to view the data
   */
  Query<T> asOf(Timestamp asOf);

  /**
   * Execute as a delete query deleting the 'root level' beans that match the predicates
   * in the query.
   * <p>
   * Note that if the query includes joins then the generated delete statement may not be
   * optimal depending on the database platform.
   * </p>
   *
   * @return the number of beans/rows that were deleted.
   */
  int delete();

  /**
   * Execute the query iterating over the results.
   * 
   * @see Query#findIterate()
   */
  QueryIterator<T> findIterate();

  /**
   * Execute the query process the beans one at a time.
   *
   * @see Query#findEach(QueryEachConsumer)
   */
  void findEach(QueryEachConsumer<T> consumer);

  /**
   * Execute the query processing the beans one at a time with the ability to
   * stop processing before reading all the beans.
   *
   * @see Query#findEachWhile(QueryEachWhileConsumer)
   */
  void findEachWhile(QueryEachWhileConsumer<T> consumer);

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
  List<Object> findIds();

  /**
   * Return the count of entities this query should return.
   * <p>
   * This is the number of 'top level' or 'root level' entities.
   * </p>
   */
  int findRowCount();

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
  Map<?, T> findMap();

  /**
   * Return a typed map specifying the key property and type.
   */
  <K> Map<K, T> findMap(String keyProperty, Class<K> keyType);

  /**
   * Execute the query returning a single bean.
   * 
   * @see Query#findUnique()
   */
  @Nullable
  T findUnique();

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
  FutureRowCount<T> findFutureRowCount();

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
   * Return a PagedList for this query.
   * <p>
   * The benefit of using this over just using the normal {@link Query#setFirstRow(int)} and
   * {@link Query#setMaxRows(int)} is that it additionally wraps an optional call to
   * {@link Query#findFutureRowCount()} to determine total row count, total page count etc.
   * </p>
   * <p>
   * Internally this works using {@link Query#setFirstRow(int)} and {@link Query#setMaxRows(int)} on
   * the query. This translates into SQL that uses limit offset, rownum or row_number
   * function to limit the result set.
   * </p>
   * 
   * @param pageIndex
   *          The zero based index of the page.
   * @param pageSize
   *          The number of beans to return per page.
   * @return The PagedList
   */
  PagedList<T> findPagedList(int pageIndex, int pageSize);

  /**
   * Return versions of a @History entity bean.
   * <p>
   *   Generally this query is expected to be a find by id or unique predicates query.
   *   It will execute the query against the history returning the versions of the bean.
   * </p>
   */
  List<Version<T>> findVersions();

  /**
   * Return versions of a @History entity bean between the 2 timestamps.
   * <p>
   *   Generally this query is expected to be a find by id or unique predicates query.
   *   It will execute the query against the history returning the versions of the bean.
   * </p>
   */
  List<Version<T>> findVersionsBetween(Timestamp start, Timestamp end);

  /**
   * Add some filter predicate expressions to the many property.
   */
  ExpressionList<T> filterMany(String prop);

  /**
   * Specify specific properties to fetch on the main/root bean (aka partial
   * object).
   * 
   * @see Query#select(String)
   */
  Query<T> select(String properties);

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
   * Set to true to use the query for executing this query.
   * 
   * @see Query#setUseCache(boolean)
   */
  Query<T> setUseCache(boolean useCache);

  /**
   * Set to true to use the query for executing this query.
   *
   * @see Query#setUseQueryCache(boolean)
   */
  Query<T> setUseQueryCache(boolean useCache);

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
   *
   * <pre>{@code
   *
   *   where().jsonExists("content", "path.other")
   *
   * }</pre>
   *
   * @param propertyName the property that holds a JSON document
   * @param path the nested path in the JSON document in dot notation
   */
  ExpressionList<T> jsonExists(String propertyName, String path);

  /**
   * Path does not exist - for the given path in a JSON document.
   *
   * <pre>{@code
   *
   *   where().jsonNotExists("content", "path.other")
   *
   * }</pre>
   *
   * @param propertyName the property that holds a JSON document
   * @param path the nested path in the JSON document in dot notation
   */
  ExpressionList<T> jsonNotExists(String propertyName, String path);

  /**
   * Equal to expression for the value at the given path in the JSON document.
   *
   * <pre>{@code
   *
   *   where().jsonEqualTo("content", "path.other", 34)
   *
   * }</pre>
   *
   * @param propertyName the property that holds a JSON document
   * @param path the nested path in the JSON document in dot notation
   * @param value the value used to test against the document path's value
   */
  ExpressionList<T> jsonEqualTo(String propertyName, String path, Object value);

  /**
   * Not Equal to - for the given path in a JSON document.
   *
   * <pre>{@code
   *
   *   where().jsonNotEqualTo("content", "path.other", 34)
   *
   * }</pre>
   *
   * @param propertyName the property that holds a JSON document
   * @param path the nested path in the JSON document in dot notation
   * @param value the value used to test against the document path's value
   */
  ExpressionList<T> jsonNotEqualTo(String propertyName, String path, Object value);

  /**
   * Greater than - for the given path in a JSON document.
   *
   * <pre>{@code
   *
   *   where().jsonGreaterThan("content", "path.other", 34)
   *
   * }</pre>
   */
  ExpressionList<T> jsonGreaterThan(String propertyName, String path, Object value);

  /**
   * Greater than or equal to - for the given path in a JSON document.
   *
   * <pre>{@code
   *
   *   where().jsonGreaterOrEqual("content", "path.other", 34)
   *
   * }</pre>
   */
  ExpressionList<T> jsonGreaterOrEqual(String propertyName, String path, Object value);

  /**
   * Less than - for the given path in a JSON document.
   *
   * <pre>{@code
   *
   *   where().jsonLessThan("content", "path.other", 34)
   *
   * }</pre>
   */
  ExpressionList<T> jsonLessThan(String propertyName, String path, Object value);

  /**
   * Less than or equal to - for the given path in a JSON document.
   *
   * <pre>{@code
   *
   *   where().jsonLessOrEqualTo("content", "path.other", 34)
   *
   * }</pre>
   */
  ExpressionList<T> jsonLessOrEqualTo(String propertyName, String path, Object value);

  /**
   * Between - for the given path in a JSON document.
   *
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
   *
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
   * 
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
   * 
   * Similarly you can create an ExampleExpression
   * 
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
  ExpressionList<T> idIn(List<?> idValues);

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
   * @param propertyMap
   *          a map keyed by property names.
   */
  ExpressionList<T> allEq(Map<String, Object> propertyMap);

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
   *
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
  ExpressionList<T> raw(String raw, Object[] values);

  /**
   * Add raw expression with no parameters.
   * <p>
   * When properties in the clause are fully qualified as table-column names
   * then they are not translated. logical property name names (not fully
   * qualified) will still be translated to their physical name.
   * </p>
   *
   * <pre>{@code
   *
   *   raw("orderQty < shipQty")
   *
   * }</pre>
   */
  ExpressionList<T> raw(String raw);

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
   * Return a list of expressions that will be joined by AND's.
   */
  Junction<T> conjunction();

  /**
   * Return a list of expressions that will be joined by OR's.
   */
  Junction<T> disjunction();

  /**
   * End a Conjunction or Disjunction returning the parent expression list.
   * <p>
   * Alternatively you can always use where() to return the top level expression
   * list.
   * </p>
   */
  ExpressionList<T> endJunction();

}

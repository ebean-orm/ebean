package com.avaje.ebean;

import java.io.Serializable;
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
  public Query<T> query();

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
  public Query<T> order(String orderByClause);

  /**
   * Return the OrderBy so that you can append an ascending or descending
   * property to the order by clause.
   * <p>
   * This will never return a null. If no order by clause exists then an 'empty'
   * OrderBy object is returned.
   * </p>
   */
  public OrderBy<T> order();

  /**
   * Return the OrderBy so that you can append an ascending or descending
   * property to the order by clause.
   * <p>
   * This will never return a null. If no order by clause exists then an 'empty'
   * OrderBy object is returned.
   * </p>
   */
  public OrderBy<T> orderBy();

  /**
   * Add an orderBy clause to the query.
   * 
   * @see Query#orderBy(String)
   */
  public Query<T> orderBy(String orderBy);

  /**
   * Add an orderBy clause to the query.
   * 
   * @see Query#orderBy(String)
   */
  public Query<T> setOrderBy(String orderBy);

  /**
   * Execute the query iterating over the results.
   * 
   * @see Query#findIterate()
   */
  public QueryIterator<T> findIterate();

  /**
   * Execute the query visiting the results.
   * 
   * @see Query#findVisit(QueryResultVisitor)
   */
  public void findVisit(QueryResultVisitor<T> visitor);

  /**
   * Execute the query returning a list.
   * 
   * @see Query#findList()
   */
  public List<T> findList();

  /**
   * Execute the query returning the list of Id's.
   * 
   * @see Query#findIds()
   */
  public List<Object> findIds();

  /**
   * Return the count of entities this query should return.
   * <p>
   * This is the number of 'top level' or 'root level' entities.
   * </p>
   */
  public int findRowCount();

  /**
   * Execute the query returning a set.
   * 
   * @see Query#findSet()
   */
  public Set<T> findSet();

  /**
   * Execute the query returning a map.
   * 
   * @see Query#findMap()
   */
  public Map<?, T> findMap();

  /**
   * Return a typed map specifying the key property and type.
   */
  public <K> Map<K, T> findMap(String keyProperty, Class<K> keyType);

  /**
   * Execute the query returning a single bean.
   * 
   * @see Query#findUnique()
   */
  public T findUnique();

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
  public FutureRowCount<T> findFutureRowCount();

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
  public FutureIds<T> findFutureIds();

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
  public FutureList<T> findFutureList();

  /**
   * Return a PagingList for this query.
   * <p>
   * This can be used to break up a query into multiple queries to fetch the
   * data a page at a time.
   * </p>
   * <p>
   * This typically works by using a query per page and setting
   * {@link Query#setFirstRow(int)} and and {@link Query#setMaxRows(int)} on the
   * query. This usually would translate into SQL that uses limit offset, rownum
   * or row_number function to limit the result set.
   * </p>
   * 
   * @param pageSize
   *          the number of beans fetched per Page
   * 
   */
  public PagingList<T> findPagingList(int pageSize);

  public ExpressionList<T> filterMany(String prop);

  /**
   * Specify specific properties to fetch on the main/root bean (aka partial
   * object).
   * 
   * @see Query#select(String)
   */
  public Query<T> select(String properties);

  /**
   * Specify a property (associated bean) to join and <em>fetch</em> including
   * all its properties.
   * 
   * @see Query#join(String)
   */
  public Query<T> join(String assocProperties);

  /**
   * Specify a property (associated bean) to join and <em>fetch</em> with its
   * specific properties to include (aka partial object).
   * 
   * @see Query#join(String,String)
   */
  public Query<T> join(String assocProperty, String assocProperties);

  /**
   * Set the first row to fetch.
   * 
   * @see Query#setFirstRow(int)
   */
  public Query<T> setFirstRow(int firstRow);

  /**
   * Set the maximum number of rows to fetch.
   * 
   * @see Query#setMaxRows(int)
   */
  public Query<T> setMaxRows(int maxRows);

  /**
   * Set the number of rows after which the fetching should continue in a
   * background thread.
   * 
   * @see Query#setBackgroundFetchAfter(int)
   */
  public Query<T> setBackgroundFetchAfter(int backgroundFetchAfter);

  /**
   * Set the name of the property which values become the key of a map.
   * 
   * @see Query#setMapKey(String)
   */
  public Query<T> setMapKey(String mapKey);

  /**
   * Set a QueryListener for bean by bean processing.
   * 
   * @see Query#setListener(QueryListener)
   */
  public Query<T> setListener(QueryListener<T> queryListener);

  /**
   * Set to true to use the query for executing this query.
   * 
   * @see Query#setUseCache(boolean)
   */
  public Query<T> setUseCache(boolean useCache);

  /**
   * Add expressions to the having clause.
   * <p>
   * The having clause is only used for queries based on raw sql (via SqlSelect
   * annotation etc).
   * </p>
   */
  public ExpressionList<T> having();

  /**
   * Add another expression to the where clause.
   */
  public ExpressionList<T> where();

  /**
   * Add an Expression to the list.
   * <p>
   * This returns the list so that add() can be chained.
   * </p>
   * 
   * <pre class="code">
   * Query&lt;Customer&gt; query = Ebean.createQuery(Customer.class);
   * query.where()
   *     .like(&quot;name&quot;,&quot;Rob%&quot;)
   *     .eq(&quot;status&quot;, Customer.ACTIVE);
   * List&lt;Customer&gt; list = query.findList();
   * ...
   * </pre>
   */
  public ExpressionList<T> add(Expression expr);

  /**
   * Add a list of Expressions to this ExpressionList.s
   */
  public ExpressionList<T> addAll(ExpressionList<T> exprList);

  /**
   * Equal To - property is equal to a given value.
   */
  public ExpressionList<T> eq(String propertyName, Object value);

  /**
   * Not Equal To - property not equal to the given value.
   */
  public ExpressionList<T> ne(String propertyName, Object value);

  /**
   * Case Insensitive Equal To - property equal to the given value (typically
   * using a lower() function to make it case insensitive).
   */
  public ExpressionList<T> ieq(String propertyName, String value);

  /**
   * Between - property between the two given values.
   */
  public ExpressionList<T> between(String propertyName, Object value1, Object value2);

  /**
   * Between - value between the two properties.
   */
  public ExpressionList<T> betweenProperties(String lowProperty, String highProperty, Object value);

  /**
   * Greater Than - property greater than the given value.
   */
  public ExpressionList<T> gt(String propertyName, Object value);

  /**
   * Greater Than or Equal to - property greater than or equal to the given
   * value.
   */
  public ExpressionList<T> ge(String propertyName, Object value);

  /**
   * Less Than - property less than the given value.
   */
  public ExpressionList<T> lt(String propertyName, Object value);

  /**
   * Less Than or Equal to - property less than or equal to the given value.
   */
  public ExpressionList<T> le(String propertyName, Object value);

  /**
   * Is Null - property is null.
   */
  public ExpressionList<T> isNull(String propertyName);

  /**
   * Is Not Null - property is not null.
   */
  public ExpressionList<T> isNotNull(String propertyName);

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
   * <pre class="code">
   * // create an example bean and set the properties
   * // with the query parameters you want
   * Customer example = new Customer();
   * example.setName(&quot;Rob%&quot;);
   * example.setNotes(&quot;%something%&quot;);
   * 
   * List&lt;Customer&gt; list = Ebean.find(Customer.class).where()
   *     // pass the bean into the where() clause
   *     .exampleLike(example)
   *     // you can add other expressions to the same query
   *     .gt(&quot;id&quot;, 2).findList();
   * 
   * </pre>
   * 
   * Similarly you can create an ExampleExpression
   * 
   * <pre>
   * Customer example = new Customer();
   * example.setName(&quot;Rob%&quot;);
   * example.setNotes(&quot;%something%&quot;);
   * 
   * // create a ExampleExpression with more control
   * ExampleExpression qbe = new ExampleExpression(example, true, LikeType.EQUAL_TO).includeZeros();
   * 
   * List&lt;Customer&gt; list = Ebean.find(Customer.class).where().add(qbe).findList();
   * </pre>
   */
  public ExpressionList<T> exampleLike(Object example);

  /**
   * Case insensitive version of {@link #exampleLike(Object)}
   */
  public ExpressionList<T> iexampleLike(Object example);

  /**
   * Like - property like value where the value contains the SQL wild card
   * characters % (percentage) and _ (underscore).
   */
  public ExpressionList<T> like(String propertyName, String value);

  /**
   * Case insensitive Like - property like value where the value contains the
   * SQL wild card characters % (percentage) and _ (underscore). Typically uses
   * a lower() function to make the expression case insensitive.
   */
  public ExpressionList<T> ilike(String propertyName, String value);

  /**
   * Starts With - property like value%.
   */
  public ExpressionList<T> startsWith(String propertyName, String value);

  /**
   * Case insensitive Starts With - property like value%. Typically uses a
   * lower() function to make the expression case insensitive.
   */
  public ExpressionList<T> istartsWith(String propertyName, String value);

  /**
   * Ends With - property like %value.
   */
  public ExpressionList<T> endsWith(String propertyName, String value);

  /**
   * Case insensitive Ends With - property like %value. Typically uses a lower()
   * function to make the expression case insensitive.
   */
  public ExpressionList<T> iendsWith(String propertyName, String value);

  /**
   * Contains - property like %value%.
   */
  public ExpressionList<T> contains(String propertyName, String value);

  /**
   * Case insensitive Contains - property like %value%. Typically uses a lower()
   * function to make the expression case insensitive.
   */
  public ExpressionList<T> icontains(String propertyName, String value);

  /**
   * In - using a subQuery.
   */
  public ExpressionList<T> in(String propertyName, Query<?> subQuery);

  /**
   * In - property has a value in the array of values.
   */
  public ExpressionList<T> in(String propertyName, Object... values);

  /**
   * In - property has a value in the collection of values.
   */
  public ExpressionList<T> in(String propertyName, Collection<?> values);

  /**
   * Id IN a list of id values.
   */
  public ExpressionList<T> idIn(List<?> idValues);

  /**
   * Id Equal to - ID property is equal to the value.
   */
  public ExpressionList<T> idEq(Object value);

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
  public ExpressionList<T> allEq(Map<String, Object> propertyMap);

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
   */
  public ExpressionList<T> raw(String raw, Object value);

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
  public ExpressionList<T> raw(String raw, Object[] values);

  /**
   * Add raw expression with no parameters.
   * <p>
   * When properties in the clause are fully qualified as table-column names
   * then they are not translated. logical property name names (not fully
   * qualified) will still be translated to their physical name.
   * </p>
   */
  public ExpressionList<T> raw(String raw);

  /**
   * And - join two expressions with a logical and.
   */
  public ExpressionList<T> and(Expression expOne, Expression expTwo);

  /**
   * Or - join two expressions with a logical or.
   */
  public ExpressionList<T> or(Expression expOne, Expression expTwo);

  /**
   * Negate the expression (prefix it with NOT).
   */
  public ExpressionList<T> not(Expression exp);

  /**
   * Return a list of expressions that will be joined by AND's.
   */
  public Junction<T> conjunction();

  /**
   * Return a list of expressions that will be joined by OR's.
   */
  public Junction<T> disjunction();

  /**
   * End a Conjunction or Disjunction returning the parent expression list.
   * <p>
   * Alternatively you can always use where() to return the top level expression
   * list.
   * </p>
   */
  public ExpressionList<T> endJunction();

}

package com.avaje.ebean;

import com.avaje.ebean.config.ServerConfig;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Object relational query for finding a List, Set, Map or single entity bean.
 * <p>
 * Example: Create the query using the API.
 * </p>
 * 
 * <pre class="code">
 * List&lt;Order&gt; orderList = 
 *   Ebean.find(Order.class)
 *     .fetch(&quot;customer&quot;)
 *     .fetch(&quot;details&quot;)
 *     .where()
 *       .like(&quot;customer.name&quot;,&quot;rob%&quot;)
 *       .gt(&quot;orderDate&quot;,lastWeek)
 *     .orderBy(&quot;customer.id, id desc&quot;)
 *     .setMaxRows(50)
 *     .findList();
 *   
 * ...
 * </pre>
 * 
 * <p>
 * Example: The same query using the query language
 * </p>
 * 
 * <pre class="code">
 * String oql = 
 *   	&quot;  find  order &quot;
 *   	+&quot; fetch customer &quot;
 *   	+&quot; fetch details &quot;
 *   	+&quot; where customer.name like :custName and orderDate &gt; :minOrderDate &quot;
 *   	+&quot; order by customer.id, id desc &quot;
 *   	+&quot; limit 50 &quot;;
 *   
 * Query&lt;Order&gt; query = Ebean.createQuery(Order.class, oql);
 * query.setParameter(&quot;custName&quot;, &quot;Rob%&quot;);
 * query.setParameter(&quot;minOrderDate&quot;, lastWeek);
 *   
 * List&lt;Order&gt; orderList = query.findList();
 * ...
 * </pre>
 * 
 * <p>
 * Example: Using a named query called "with.cust.and.details"
 * </p>
 * 
 * <pre class="code">
 * Query&lt;Order&gt; query = Ebean.createNamedQuery(Order.class,&quot;with.cust.and.details&quot;);
 * query.setParameter(&quot;custName&quot;, &quot;Rob%&quot;);
 * query.setParameter(&quot;minOrderDate&quot;, lastWeek);
 *   
 * List&lt;Order&gt; orderList = query.findList();
 * ...
 * </pre>
 * 
 * <h3>Autofetch</h3>
 * <p>
 * Ebean has built in support for "Autofetch". This is a mechanism where a query
 * can be automatically tuned based on profiling information that is collected.
 * </p>
 * <p>
 * This is effectively the same as automatically using select() and fetch() to
 * build a query that will fetch all the data required by the application and no
 * more.
 * </p>
 * <p>
 * It is expected that Autofetch will be the default approach for many queries
 * in a system. It is possibly not as useful where the result of a query is sent
 * to a remote client or where there is some requirement for "Read Consistency"
 * guarantees.
 * </p>
 * 
 * <h3>Query Language</h3>
 * <p>
 * <b>Partial Objects</b>
 * </p>
 * <p>
 * The <em>find</em> and <em>fetch</em> clauses support specifying a list of
 * properties to fetch. This results in objects that are "partially populated".
 * If you try to get a property that was not populated a "lazy loading" query
 * will automatically fire and load the rest of the properties of the bean (This
 * is very similar behaviour as a reference object being "lazy loaded").
 * </p>
 * <p>
 * Partial objects can be saved just like fully populated objects. If you do
 * this you should remember to include the <em>"Version"</em> property in the
 * initial fetch. If you do not include a version property then optimistic
 * concurrency checking will occur but only include the fetched properties.
 * Refer to "ALL Properties/Columns" mode of Optimistic Concurrency checking.
 * </p>
 * 
 * <pre class="code">
 * [ find  {bean type} [ ( * | {fetch properties} ) ] ]
 * [ fetch {associated bean} [ ( * | {fetch properties} ) ] ]
 * [ where {predicates} ]
 * [ order by {order by properties} ]
 * [ limit {max rows} [ offset {first row} ] ]
 * </pre>
 * 
 * <p>
 * <b>FIND</b> <b>{bean type}</b> [ ( <i>*</i> | <i>{fetch properties}</i> ) ]
 * </p>
 * <p>
 * With the find you specify the type of beans to fetch. You can optionally
 * specify a list of properties to fetch. If you do not specify a list of
 * properties ALL the properties for those beans are fetched.
 * </p>
 * <p>
 * In object graph terms the <em>find</em> clause specifies the type of bean at
 * the root level and the <em>fetch</em> clauses specify the paths of the object
 * graph to populate.
 * </p>
 * <p>
 * <b>FETCH</b> <b>{associated property}</b> [ ( <i>*</i> | <i>{fetch
 * properties}</i> ) ]
 * </p>
 * <p>
 * With the fetch you specify the associated property to fetch and populate. The
 * associated property is a OneToOnem, ManyToOne, OneToMany or ManyToMany
 * property. When the query is executed Ebean will fetch the associated data.
 * </p>
 * <p>
 * For fetch of a path we can optionally specify a list of properties to fetch.
 * If you do not specify a list of properties ALL the properties for that bean
 * type are fetched.
 * </p>
 * <p>
 * <b>WHERE</b> <b>{list of predicates}</b>
 * </p>
 * <p>
 * The list of predicates which are joined by AND OR NOT ( and ). They can
 * include named (or positioned) bind parameters. These parameters will need to
 * be bound by {@link Query#setParameter(String, Object)}.
 * </p>
 * <p>
 * <b>ORDER BY</b> <b>{order by properties}</b>
 * </p>
 * <p>
 * The list of properties to order the result. You can include ASC (ascending)
 * and DESC (descending) in the order by clause.
 * </p>
 * <p>
 * <b>LIMIT</b> <b>{max rows}</b> [ OFFSET <i>{first row}</i> ]
 * </p>
 * <p>
 * The limit offset specifies the max rows and first row to fetch. The offset is
 * optional.
 * </p>
 * <h4>Examples of Ebean's Query Language</h4>
 * <p>
 * Find orders fetching all its properties
 * </p>
 * 
 * <pre class="code">
 * find order
 * </pre>
 * 
 * <p>
 * Find orders fetching all its properties
 * </p>
 * 
 * <pre class="code">
 * find order (*)
 * </pre>
 * 
 * <p>
 * Find orders fetching its id, shipDate and status properties. Note that the id
 * property is always fetched even if it is not included in the list of fetch
 * properties.
 * </p>
 * 
 * <pre class="code">
 * find order (shipDate, status)
 * </pre>
 * 
 * <p>
 * Find orders with a named bind variable (that will need to be bound via
 * {@link Query#setParameter(String, Object)}).
 * </p>
 * 
 * <pre class="code">
 * find order
 * where customer.name like :custLike
 * </pre>
 * 
 * <p>
 * Find orders and also fetch the customer with a named bind parameter. This
 * will fetch and populate both the order and customer objects.
 * </p>
 * 
 * <pre class="code">
 * find  order
 * fetch customer
 * where customer.id = :custId
 * </pre>
 * 
 * <p>
 * Find orders and also fetch the customer, customer shippingAddress, order
 * details and related product. Note that customer and product objects will be
 * "Partial Objects" with only some of their properties populated. The customer
 * objects will have their id, name and shipping address populated. The product
 * objects (associated with each order detail) will have their id, sku and name
 * populated.
 * </p>
 * 
 * <pre class="code">
 * find  order
 * fetch customer (name)
 * fetch customer.shippingAddress
 * fetch details
 * fetch details.product (sku, name)
 * </pre>
 * 
 * <h3>Early parsing of the Query</h3>
 * <p>
 * When you get a Query object from a named query, the query statement has
 * already been parsed. You can then add to that query (add fetch paths, add to
 * the where clause) or override some of its settings (override the order by
 * clause, first rows, max rows).
 * </p>
 * <p>
 * The thought is that you can use named queries as a 'starting point' and then
 * modify the query to suit specific needs.
 * </p>
 * <h3>Building the Where clause</h3>
 * <p>
 * You can add to the where clause using Expression objects or a simple String.
 * Note that the ExpressionList has methods to add most of the common
 * expressions that you will need.
 * <ul>
 * <li>where(String addToWhereClause)</li>
 * <li>where().add(Expression expression)</li>
 * <li>where().eq(propertyName, value).like(propertyName , value)...</li>
 * </ul>
 * </p>
 * <p>
 * The full WHERE clause is constructed by appending together
 * <li>original query where clause (Named query or query.setQuery(String oql))</li>
 * <li>clauses added via query.where(String addToWhereClause)</li>
 * <li>clauses added by Expression objects</li>
 * </p>
 * <p>
 * The above is the order that these are clauses are appended to give the full
 * WHERE clause.
 * </p>
 * <h3>Design Goal</h3>
 * <p>
 * This query language is NOT designed to be a replacement for SQL. It is
 * designed to be a simple way to describe the "Object Graph" you want Ebean to
 * build for you. Each find/fetch represents a node in that "Object Graph" which
 * makes it easy to define for each node which properties you want to fetch.
 * </p>
 * <p>
 * Once you hit the limits of this language such as wanting aggregate functions
 * (sum, average, min etc) or recursive queries etc you use SQL. Ebean's goal is
 * to make it as easy as possible to use your own SQL to populate entity beans.
 * Refer to {@link RawSql} .
 * </p>
 * 
 * @param <T>
 *          the type of Entity bean this query will fetch.
 */
public interface Query<T> extends Serializable {

  /**
   * How this query should use (or not) the Lucene Index if one is defined for
   * the bean type.
   */
  public enum UseIndex {
    NO, DEFAULT, YES_IDS, YES_OBJECTS
  }

  /**
   * Explicitly specify how this query should use a Lucene Index if one is
   * defined for this bean type.
   */
  public Query<T> setUseIndex(UseIndex useIndex);

  /**
   * Return the setting for how this query should use a Lucene Index if one is
   * defined for this bean type.
   */
  public UseIndex getUseIndex();

  /**
   * Return the RawSql that was set to use for this query.
   */
  public RawSql getRawSql();

  /**
   * Set RawSql to use for this query.
   */
  public Query<T> setRawSql(RawSql rawSql);

  /**
   * Cancel the query execution if supported by the underlying database and
   * driver.
   * <p>
   * This must be called from a different thread to the query executor.
   * </p>
   */
  public void cancel();

  /**
   * Return a copy of the query.
   * <p>
   * This is so that you can use a Query as a "prototype" for creating other
   * query instances. You could create a Query with various where expressions
   * and use that as a "prototype" - using this copy() method to create a new
   * instance that you can then add other expressions then execute.
   * </p>
   */
  public Query<T> copy();

  /**
   * Return the ExpressionFactory used by this query.
   */
  public ExpressionFactory getExpressionFactory();

  /**
   * Returns true if this query was tuned by autoFetch.
   */
  public boolean isAutofetchTuned();

  /**
   * Explicitly specify whether to use Autofetch for this query.
   * <p>
   * If you do not call this method on a query the "Implicit Autofetch mode" is
   * used to determine if Autofetch should be used for a given query.
   * </p>
   * <p>
   * Autofetch can add additional fetch paths to the query and specify which
   * properties are included for each path. If you have explicitly defined some
   * fetch paths Autofetch will not remove.
   * </p>
   */
  public Query<T> setAutofetch(boolean autofetch);

  /**
   * Explicitly set a comma delimited list of the properties to fetch on the
   * 'main' entity bean (aka partial object). Note that '*' means all
   * properties.
   * 
   * <pre class="code">
   * Query&lt;Customer&gt; query = Ebean.createQuery(Customer.class);
   * 
   * // Only fetch the customer id, name and status.
   * // This is described as a &quot;Partial Object&quot;
   * query.select(&quot;name, status&quot;);
   * query.where(&quot;lower(name) like :custname&quot;).setParameter(&quot;custname&quot;, &quot;rob%&quot;);
   * 
   * List&lt;Customer&gt; customerList = query.findList();
   * </pre>
   * 
   * @param fetchProperties
   *          the properties to fetch for this bean (* = all properties).
   */
  public Query<T> select(String fetchProperties);

  /**
   * Specify a path to <em>fetch</em> with its specific properties to include
   * (aka partial object).
   * <p>
   * When you specify a join this means that property (associated bean(s)) will
   * be fetched and populated. If you specify "*" then all the properties of the
   * associated bean will be fetched and populated. You can specify a comma
   * delimited list of the properties of that associated bean which means that
   * only those properties are fetched and populated resulting in a
   * "Partial Object" - a bean that only has some of its properties populated.
   * </p>
   * 
   * <pre class="code">
   * // query orders...
   * Query&lt;Order&gt; query = Ebean.createQuery(Order.class);
   * 
   * // fetch the customer...
   * // ... getting the customer's name and phone number
   * query.fetch(&quot;customer&quot;, &quot;name, phNumber&quot;);
   * 
   * // ... also fetch the customers billing address (* = all properties)
   * query.fetch(&quot;customer.billingAddress&quot;, &quot;*&quot;);
   * </pre>
   * 
   * <p>
   * If columns is null or "*" then all columns/properties for that path are
   * fetched.
   * </p>
   * 
   * <pre class="code">
   * // fetch customers (their id, name and status)
   * Query&lt;Customer&gt; query = Ebean.createQuery(Customer.class);
   * 
   * // only fetch some of the properties of the customers
   * query.select(&quot;name, status&quot;);
   * List&lt;Customer&gt; list = query.findList();
   * </pre>
   * 
   * @param path
   *          the path of an associated (1-1,1-M,M-1,M-M) bean.
   * @param fetchProperties
   *          properties of the associated bean that you want to include in the
   *          fetch (* means all properties, null also means all properties).
   */
  public Query<T> fetch(String path, String fetchProperties);

  /**
   * Additionally specify a FetchConfig to use a separate query or lazy loading
   * to load this path.
   */
  public Query<T> fetch(String assocProperty, String fetchProperties, FetchConfig fetchConfig);

  /**
   * Specify a path to load including all its properties.
   * <p>
   * The same as {@link #fetch(String, String)} with the fetchProperties as "*".
   * </p>
   * 
   * @param path
   *          the property of an associated (1-1,1-M,M-1,M-M) bean.
   */
  public Query<T> fetch(String path);

  /**
   * Additionally specify a JoinConfig to specify a "query join" and or define
   * the lazy loading query.
   */
  public Query<T> fetch(String path, FetchConfig joinConfig);

  /**
   * Execute the query returning the list of Id's.
   * <p>
   * This query will execute against the EbeanServer that was used to create it.
   * </p>
   * 
   * @see EbeanServer#findIds(Query, Transaction)
   */
  public List<Object> findIds();

  /**
   * Execute the query iterating over the results.
   * <p>
   * Remember that with {@link QueryIterator} you must call
   * {@link QueryIterator#close()} when you have finished iterating the results
   * (typically in a finally block).
   * </p>
   * <p>
   * This query will execute against the EbeanServer that was used to create it.
   * </p>
   */
  public QueryIterator<T> findIterate();

  /**
   * Execute the query using callbacks to a visitor to process the resulting
   * beans one at a time.
   * <p>
   * Similar to findIterate() this query method does not require all the result
   * beans to be all held in memory at once and as such is useful for processing
   * large queries.
   * </p>
   * 
   * <pre class="code">
   * 
   * Query&lt;Customer&gt; query = server.find(Customer.class)
   *     .fetch(&quot;contacts&quot;, new FetchConfig().query(2))
   *     .where().gt(&quot;id&quot;, 0)
   *     .orderBy(&quot;id&quot;)
   *     .setMaxRows(2);
   * 
   * query.findVisit(new QueryResultVisitor&lt;Customer&gt;() {
   * 
   *   public boolean accept(Customer customer) {
   *     // do something with customer
   *     System.out.println(&quot;-- visit &quot; + customer);
   *     return true;
   *   }
   * });
   * </pre>
   * 
   * @param visitor
   *          the visitor used to process the queried beans.
   */
  public void findVisit(QueryResultVisitor<T> visitor);

  /**
   * Execute the query returning the list of objects.
   * <p>
   * This query will execute against the EbeanServer that was used to create it.
   * </p>
   * 
   * @see EbeanServer#findList(Query, Transaction)
   */
  public List<T> findList();

  /**
   * Execute the query returning the set of objects.
   * <p>
   * This query will execute against the EbeanServer that was used to create it.
   * </p>
   * 
   * @see EbeanServer#findSet(Query, Transaction)
   */
  public Set<T> findSet();

  /**
   * Execute the query returning a map of the objects.
   * <p>
   * This query will execute against the EbeanServer that was used to create it.
   * </p>
   * <p>
   * You can use setMapKey() so specify the property values to be used as keys
   * on the map. If one is not specified then the id property is used.
   * </p>
   * 
   * <pre class="code">
   * Query&lt;Product&gt; query = Ebean.createQuery(Product.class);
   * query.setMapKey(&quot;sku&quot;);
   * Map&lt;?, Product&gt; map = query.findMap();
   * </pre>
   * 
   * @see EbeanServer#findMap(Query, Transaction)
   */
  public Map<?, T> findMap();

  /**
   * Return a typed map specifying the key property and type.
   */
  public <K> Map<K, T> findMap(String keyProperty, Class<K> keyType);

  /**
   * Execute the query returning either a single bean or null (if no matching
   * bean is found).
   * <p>
   * If more than 1 row is found for this query then a PersistenceException is
   * thrown.
   * </p>
   * <p>
   * This is useful when your predicates dictate that your query should only
   * return 0 or 1 results.
   * </p>
   * 
   * <pre class="code">
   * // assuming the sku of products is unique...
   * Product product =
   *     Ebean.find(Product.class)
   *         .where(&quot;sku = ?&quot;)
   *         .set(1, &quot;aa113&quot;)
   *         .findUnique();
   * ...
   * </pre>
   * 
   * <p>
   * It is also useful with finding objects by their id when you want to specify
   * further join information.
   * </p>
   * 
   * <pre class="code">
   * // Fetch order 1 and additionally fetch join its order details...
   * Order order = 
   *     Ebean.find(Order.class)
   *       .setId(1)
   *       .fetch(&quot;details&quot;)
   *       .findUnique();
   *       
   * List&lt;OrderDetail&gt; details = order.getDetails();
   * ...
   * </pre>
   */
  public T findUnique();

  /**
   * Return the count of entities this query should return.
   * <p>
   * This is the number of 'top level' or 'root level' entities.
   * </p>
   */
  public int findRowCount();

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

  /**
   * Set a named bind parameter. Named parameters have a colon to prefix the
   * name.
   * 
   * <pre class="code">
   * // a query with a named parameter
   * String oql = &quot;find order where status = :orderStatus&quot;;
   * 
   * Query&lt;Order&gt; query = Ebean.createQuery(Order.class, oql);
   * 
   * // bind the named parameter
   * query.bind(&quot;orderStatus&quot;, OrderStatus.NEW);
   * List&lt;Order&gt; list = query.findList();
   * </pre>
   * 
   * @param name
   *          the parameter name
   * @param value
   *          the parameter value
   */
  public Query<T> setParameter(String name, Object value);

  /**
   * Set an ordered bind parameter according to its position. Note that the
   * position starts at 1 to be consistent with JDBC PreparedStatement. You need
   * to set a parameter value for each ? you have in the query.
   * 
   * <pre class="code">
   * // a query with a positioned parameter
   * String oql = &quot;where status = ? order by id desc&quot;;
   * 
   * Query&lt;Order&gt; query = Ebean.createQuery(Order.class, oql);
   * 
   * // bind the parameter
   * query.setParameter(1, OrderStatus.NEW);
   * 
   * List&lt;Order&gt; list = query.findList();
   * </pre>
   * 
   * @param position
   *          the parameter bind position starting from 1 (not 0)
   * @param value
   *          the parameter bind value.
   */
  public Query<T> setParameter(int position, Object value);

  /**
   * Set a listener to process the query on a row by row basis.
   * <p>
   * Use this when you want to process a large query and do not want to hold the
   * entire query result in memory.
   * </p>
   * <p>
   * It this case the rows are not loaded into the persistence context and
   * instead are processed by the query listener.
   * </p>
   * 
   * <pre class="code">
   * QueryListener&lt;Order&gt; listener = ...;
   *   
   * Query&lt;Order&gt; query  = Ebean.createQuery(Order.class);
   *   
   * // set the listener that will process each order one at a time
   * query.setListener(listener);
   *   
   * // execute the query. Note that the returned
   * // list (emptyList) will be empty ...
   * List&lt;Order&gt; emtyList = query.findList();
   * </pre>
   */
  public Query<T> setListener(QueryListener<T> queryListener);

  /**
   * Set the Id value to query. This is used with findUnique().
   * <p>
   * You can use this to have further control over the query. For example adding
   * fetch joins.
   * </p>
   * 
   * <pre class="code">
   * Query&lt;Order&gt; query = Ebean.createQuery(Order.class);
   * Order order = query.setId(1).join(&quot;details&quot;).findUnique();
   * List&lt;OrderDetail&gt; details = order.getDetails();
   * ...
   * </pre>
   */
  public Query<T> setId(Object id);

  /**
   * Add additional clause(s) to the where clause.
   * <p>
   * This typically contains named parameters which will need to be set via
   * {@link #setParameter(String, Object)}.
   * </p>
   * 
   * <pre class="code">
   * Query&lt;Order&gt; query = Ebean.createQuery(Order.class, &quot;top&quot;);
   * ...
   * if (...) {
   *   query.where(&quot;status = :status and lower(customer.name) like :custName&quot;);
   *   query.setParameter(&quot;status&quot;, Order.NEW);
   *   query.setParameter(&quot;custName&quot;, &quot;rob%&quot;);
   * }
   * </pre>
   * 
   * <p>
   * Internally the addToWhereClause string is processed by removing named
   * parameters (replacing them with ?) and by converting logical property names
   * to database column names (with table alias). The rest of the string is left
   * as is and it is completely acceptable and expected for the addToWhereClause
   * string to include sql functions and columns.
   * </p>
   * 
   * @param addToWhereClause
   *          the clause to append to the where clause which typically contains
   *          named parameters.
   * @return The query object
   */
  public Query<T> where(String addToWhereClause);

  /**
   * Add a single Expression to the where clause returning the query.
   * 
   * <pre class="code">
   * List&lt;Order&gt; newOrders = 
   *     Ebean.find(Order.class)
   * 		.where().eq(&quot;status&quot;, Order.NEW)
   * 		.findList();
   * ...
   * </pre>
   */
  public Query<T> where(Expression expression);

  /**
   * Add Expressions to the where clause with the ability to chain on the
   * ExpressionList. You can use this for adding multiple expressions to the
   * where clause.
   * 
   * <pre class="code">
   * Query&lt;Order&gt; query = Ebean.createQuery(Order.class, &quot;top&quot;);
   * ...
   * if (...) {
   *   query.where()
   *     .eq(&quot;status&quot;, Order.NEW)
   *     .ilike(&quot;customer.name&quot;,&quot;rob%&quot;);
   * }
   * </pre>
   * 
   * @see Expr
   * @return The ExpressionList for adding expressions to.
   */
  public ExpressionList<T> where();

  /**
   * This applies a filter on the 'many' property list rather than the root
   * level objects.
   * <p>
   * Typically you will use this in a scenario where the cardinality is high on
   * the 'many' property you wish to join to. Say you want to fetch customers
   * and their associated orders... but instead of getting all the orders for
   * each customer you only want to get the new orders they placed since last
   * week. In this case you can use filterMany() to filter the orders.
   * </p>
   * 
   * <pre class="code">
   * 
   * List&lt;Customer&gt; list = Ebean
   *     .find(Customer.class)
   *     // .fetch(&quot;orders&quot;, new FetchConfig().lazy())
   *     // .fetch(&quot;orders&quot;, new FetchConfig().query())
   *     .fetch(&quot;orders&quot;).where().ilike(&quot;name&quot;, &quot;rob%&quot;).filterMany(&quot;orders&quot;)
   *     .eq(&quot;status&quot;, Order.Status.NEW).gt(
   *         &quot;orderDate&quot;, lastWeek).findList();
   * 
   * </pre>
   * 
   * <p>
   * Please note you have to be careful that you add expressions to the correct
   * expression list - as there is one for the 'root level' and one for each
   * filterMany that you have.
   * </p>
   * 
   * @param propertyName
   *          the name of the many property that you want to have a filter on.
   * 
   * @return the expression list that you add filter expressions for the many
   *         to.
   */
  public ExpressionList<T> filterMany(String propertyName);

  /**
   * Add Expressions to the Having clause return the ExpressionList.
   * <p>
   * Currently only beans based on raw sql will use the having clause.
   * </p>
   * <p>
   * Note that this returns the ExpressionList (so you can add multiple
   * expressions to the query in a fluent API way).
   * </p>
   * 
   * @see Expr
   * @return The ExpressionList for adding more expressions to.
   */
  public ExpressionList<T> having();

  /**
   * Add additional clause(s) to the having clause.
   * <p>
   * This typically contains named parameters which will need to be set via
   * {@link #setParameter(String, Object)}.
   * </p>
   * 
   * <pre class="code">
   * Query&lt;ReportOrder&gt; query = Ebean.createQuery(ReportOrder.class);
   * ...
   * if (...) {
   *   query.having(&quot;score &gt; :min&quot;);
   *   query.setParameter(&quot;min&quot;, 1);
   * }
   * </pre>
   * 
   * @param addToHavingClause
   *          the clause to append to the having clause which typically contains
   *          named parameters.
   * @return The query object
   */
  public Query<T> having(String addToHavingClause);

  /**
   * Add an expression to the having clause returning the query.
   * <p>
   * Currently only beans based on raw sql will use the having clause.
   * </p>
   * <p>
   * This is similar to {@link #having()} except it returns the query rather
   * than the ExpressionList. This is useful when you want to further specify
   * something on the query.
   * </p>
   * 
   * @param addExpressionToHaving
   *          the expression to add to the having clause.
   * @return the Query object
   */
  public Query<T> having(Expression addExpressionToHaving);

  /**
   * Set the order by clause replacing the existing order by clause if there is
   * one.
   * <p>
   * This follows SQL syntax using commas between each property with the
   * optional asc and desc keywords representing ascending and descending order
   * respectively.
   * </p>
   * <p>
   * This is EXACTLY the same as {@link #order(String)}.
   * </p>
   */
  public Query<T> orderBy(String orderByClause);

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
   * <p>
   * This is EXACTLY the same as {@link #orderBy()}.
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
   * <p>
   * This is EXACTLY the same as {@link #order()}.
   * </p>
   */
  public OrderBy<T> orderBy();

  /**
   * Set an OrderBy object to replace any existing OrderBy clause.
   * <p>
   * This is EXACTLY the same as {@link #setOrderBy(OrderBy)}.
   * </p>
   */
  public Query<T> setOrder(OrderBy<T> orderBy);

  /**
   * Set an OrderBy object to replace any existing OrderBy clause.
   * <p>
   * This is EXACTLY the same as {@link #setOrder(OrderBy)}.
   * </p>
   */
  public Query<T> setOrderBy(OrderBy<T> orderBy);

  /**
   * Set whether this query uses DISTINCT.
   */
  public Query<T> setDistinct(boolean isDistinct);

  /**
   * Set this to true and the beans and collections returned will be plain
   * classes rather than Ebean generated dynamic subclasses etc.
   * <p>
   * This is *ONLY* relevant when you are not using enhancement (and using
   * dynamic subclasses instead).
   * </p>
   * <p>
   * Alternatively you can globally set the mode using ebean.vanillaMode=true in
   * ebean.properties or {@link ServerConfig#setVanillaMode(boolean)}.
   * </p>
   * 
   * @see ServerConfig#setVanillaMode(boolean)
   * @see ServerConfig#setVanillaRefMode(boolean)
   */
  public Query<T> setVanillaMode(boolean vanillaMode);

  /**
   * Return the first row value.
   */
  public int getFirstRow();

  /**
   * Set the first row to return for this query.
   * 
   * @param firstRow
   */
  public Query<T> setFirstRow(int firstRow);

  /**
   * Return the max rows for this query.
   */
  public int getMaxRows();

  /**
   * Set the maximum number of rows to return in the query.
   * 
   * @param maxRows
   *          the maximum number of rows to return in the query.
   */
  public Query<T> setMaxRows(int maxRows);

  /**
   * Set the rows after which fetching should continue in a background thread.
   * 
   * @param backgroundFetchAfter
   */
  public Query<T> setBackgroundFetchAfter(int backgroundFetchAfter);

  /**
   * Set the property to use as keys for a map.
   * <p>
   * If no property is set then the id property is used.
   * </p>
   * 
   * <pre class="code">
   * // Assuming sku is unique for products...
   *    
   * Query&lt;Product&gt; query = Ebean.createQuery(Product.class);
   *   
   * // use sku for keys...
   * query.setMapKey(&quot;sku&quot;);
   *   
   * Map&lt;?,Product&gt; productMap = query.findMap();
   * ...
   * </pre>
   * 
   * @param mapKey
   *          the property to use as keys for a map.
   */
  public Query<T> setMapKey(String mapKey);

  /**
   * Set this to true to use the bean cache.
   * <p>
   * If the query result is in cache then by default this same instance is
   * returned. In this sense it should be treated as a read only object graph.
   * </p>
   */
  public Query<T> setUseCache(boolean useBeanCache);

  /**
   * Set this to true to use the query cache.
   */
  public Query<T> setUseQueryCache(boolean useQueryCache);

  /**
   * When set to true when you want the returned beans to be read only.
   */
  public Query<T> setReadOnly(boolean readOnly);

  /**
   * When set to true all the beans from this query are loaded into the bean
   * cache.
   */
  public Query<T> setLoadBeanCache(boolean loadBeanCache);

  /**
   * Set a timeout on this query.
   * <p>
   * This will typically result in a call to setQueryTimeout() on a
   * preparedStatement. If the timeout occurs an exception will be thrown - this
   * will be a SQLException wrapped up in a PersistenceException.
   * </p>
   * 
   * @param secs
   *          the query timeout limit in seconds. Zero means there is no limit.
   */
  public Query<T> setTimeout(int secs);

  /**
   * A hint which for JDBC translates to the Statement.fetchSize().
   * <p>
   * Gives the JDBC driver a hint as to the number of rows that should be
   * fetched from the database when more rows are needed for ResultSet.
   * </p>
   */
  public Query<T> setBufferFetchSizeHint(int fetchSize);

  /**
   * Return the sql that was generated for executing this query.
   * <p>
   * This is only available after the query has been executed and provided only
   * for informational purposes.
   * </p>
   */
  public String getGeneratedSql();

  /**
   * Return the total hits matched for a lucene text search query.
   */
  public int getTotalHits();

  /**
   * executed the select with "for update" which should lock the record
   * "on read"
   */
  public Query<T> setForUpdate(boolean forUpdate);

  public boolean isForUpdate();
}

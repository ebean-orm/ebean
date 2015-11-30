package com.avaje.ebean;

import com.avaje.ebean.text.PathProperties;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Object relational query for finding a List, Set, Map or single entity bean.
 * <p>
 * Example: Create the query using the API.
 * </p>
 * 
 * <pre>{@code
 *
 * List<Order> orderList = 
 *   ebeanServer.find(Order.class)
 *     .fetch("customer")
 *     .fetch("details")
 *     .where()
 *       .like("customer.name","rob%")
 *       .gt("orderDate",lastWeek)
 *     .orderBy("customer.id, id desc")
 *     .setMaxRows(50)
 *     .findList();
 *   
 * ...
 * }</pre>
 * 
 * <p>
 * Example: The same query using the query language
 * </p>
 * 
 * <pre>{@code
 *
 * String oql = 
 *   	"  find  order "
 *   	+" fetch customer "
 *   	+" fetch details "
 *   	+" where customer.name like :custName and orderDate > :minOrderDate "
 *   	+" order by customer.id, id desc "
 *   	+" limit 50 ";
 *   
 * Query<Order> query = ebeanServer.createQuery(Order.class, oql);
 * query.setParameter("custName", "Rob%");
 * query.setParameter("minOrderDate", lastWeek);
 *   
 * List<Order> orderList = query.findList();
 * ...
 * }</pre>
 * 
 * <p>
 * Example: Using a named query called "with.cust.and.details"
 * </p>
 * 
 * <pre>{@code
 *
 * Query<Order> query = ebeanServer.createNamedQuery(Order.class,"with.cust.and.details");
 * query.setParameter("custName", "Rob%");
 * query.setParameter("minOrderDate", lastWeek);
 *   
 * List<Order> orderList = query.findList();
 * ...
 * }</pre>
 * 
 * <h3>AutoTune</h3>
 * <p>
 * Ebean has built in support for "AutoTune". This is a mechanism where a query
 * can be automatically tuned based on profiling information that is collected.
 * </p>
 * <p>
 * This is effectively the same as automatically using select() and fetch() to
 * build a query that will fetch all the data required by the application and no
 * more.
 * </p>
 * <p>
 * It is expected that AutoTune will be the default approach for many queries
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
 * <pre>{@code
 * [ find  {bean type} [ ( * | {fetch properties} ) ] ]
 * [ fetch {associated bean} [ ( * | {fetch properties} ) ] ]
 * [ where {predicates} ]
 * [ order by {order by properties} ]
 * [ limit {max rows} [ offset {first row} ] ]
 * }</pre>
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
 * <pre>{@code
 * find order
 * }</pre>
 * 
 * <p>
 * Find orders fetching all its properties
 * </p>
 * 
 * <pre>{@code
 * find order (*)
 * }</pre>
 * 
 * <p>
 * Find orders fetching its id, shipDate and status properties. Note that the id
 * property is always fetched even if it is not included in the list of fetch
 * properties.
 * </p>
 * 
 * <pre>{@code
 * find order (shipDate, status)
 * }</pre>
 * 
 * <p>
 * Find orders with a named bind variable (that will need to be bound via
 * {@link Query#setParameter(String, Object)}).
 * </p>
 * 
 * <pre>{@code
 * find order
 * where customer.name like :custLike
 * }</pre>
 * 
 * <p>
 * Find orders and also fetch the customer with a named bind parameter. This
 * will fetch and populate both the order and customer objects.
 * </p>
 * 
 * <pre>{@code
 * find  order
 * fetch customer
 * where customer.id = :custId
 * }</pre>
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
 * <pre>{@code
 * find  order
 * fetch customer (name)
 * fetch customer.shippingAddress
 * fetch details
 * fetch details.product (sku, name)
 * }</pre>
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
   * Return the RawSql that was set to use for this query.
   */
  RawSql getRawSql();

  /**
   * Set RawSql to use for this query.
   */
  Query<T> setRawSql(RawSql rawSql);

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
   * Execute the query against the draft set of tables.
   */
  Query<T> asDraft();

  /**
   * Cancel the query execution if supported by the underlying database and
   * driver.
   * <p>
   * This must be called from a different thread to the query executor.
   * </p>
   */
  void cancel();

  /**
   * Return a copy of the query.
   * <p>
   * This is so that you can use a Query as a "prototype" for creating other
   * query instances. You could create a Query with various where expressions
   * and use that as a "prototype" - using this copy() method to create a new
   * instance that you can then add other expressions then execute.
   * </p>
   */
  Query<T> copy();

  /**
   * Specify the PersistenceContextScope to use for this query.
   * <p/>
   * When this is not set the 'default' configured on {@link com.avaje.ebean.config.ServerConfig#setPersistenceContextScope(PersistenceContextScope)}
   * is used - this value defaults to {@link com.avaje.ebean.PersistenceContextScope#TRANSACTION}.
   * <p/>
   * Note that the same persistence Context is used for subsequent lazy loading and query join queries.
   * <p/>
   * Note that #findEach uses a 'per object graph' PersistenceContext so this scope is ignored for
   * queries executed as #findIterate, #findEach, #findEachWhile.
   *
   * @param scope The scope to use for this query and subsequent lazy loading.
   */
  Query<T> setPersistenceContextScope(PersistenceContextScope scope);

  /**
   * Return the ExpressionFactory used by this query.
   */
  ExpressionFactory getExpressionFactory();

  /**
   * Returns true if this query was tuned by autoTune.
   */
  boolean isAutoTuned();

  /**
   * Explicitly specify whether to use AutoTune for this query.
   * <p>
   * If you do not call this method on a query the "Implicit AutoTune mode" is
   * used to determine if AutoTune should be used for a given query.
   * </p>
   * <p>
   * AutoTune can add additional fetch paths to the query and specify which
   * properties are included for each path. If you have explicitly defined some
   * fetch paths AutoTune will not remove them.
   * </p>
   */
  Query<T> setAutoTune(boolean autoTune);

  /**
   * Set the default lazy loading batch size to use.
   * <p>
   * When lazy loading is invoked on beans loaded by this query then this sets the
   * batch size used to load those beans.
   *
   * @param lazyLoadBatchSize the number of beans to lazy load in a single batch
   */
  Query<T> setLazyLoadBatchSize(int lazyLoadBatchSize);

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
   * Explicitly set a comma delimited list of the properties to fetch on the
   * 'main' root level entity bean (aka partial object). Note that '*' means all
   * properties.
   * <p>
   * You use {@link #fetch(String, String)} to specify specific properties to fetch
   * on other non-root level paths of the object graph.
   * </p>
   *
   * <pre>{@code
   *
   * List<Customer> customers =
   *     ebeanServer.find(Customer.class)
   *     // Only fetch the customer id, name and status.
   *     // This is described as a "Partial Object"
   *     .select("name, status")
   *     .where.ilike("name", "rob%")
   *     .findList();
   *
   * }</pre>
   *
   * @param fetchProperties
   *          the properties to fetch for this bean (* = all properties).
   */
  Query<T> select(String fetchProperties);

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
   * <pre>{@code
   *
   * // query orders...
   * List<Order> orders =
   *     ebeanserver.find(Order.class)
   *       // fetch the customer...
   *       // ... getting the customers name and phone number
   *       .fetch("customer", "name, phoneNumber")
   * 
   *       // ... also fetch the customers billing address (* = all properties)
   *       .fetch("customer.billingAddress", "*")
   *       .findList();
   * }</pre>
   * 
   * <p>
   * If columns is null or "*" then all columns/properties for that path are
   * fetched.
   * </p>
   * 
   * <pre>{@code
   *
   * // fetch customers (their id, name and status)
   * List<Customer> customers =
   *     ebeanServer.find(Customer.class)
   *     .select("name, status")
   *     .fetch("contacts", "firstName,lastName,email")
   *     .findList();
   *
   * }</pre>
   * 
   * @param path
   *          the path of an associated (1-1,1-M,M-1,M-M) bean.
   * @param fetchProperties
   *          properties of the associated bean that you want to include in the
   *          fetch (* means all properties, null also means all properties).
   */
  Query<T> fetch(String path, String fetchProperties);

  /**
   * Additionally specify a FetchConfig to use a separate query or lazy loading
   * to load this path.
   *
   * <pre>{@code
   *
   * // fetch customers (their id, name and status)
   * List<Customer> customers =
   *     ebeanServer.find(Customer.class)
   *     .select("name, status")
   *     .fetch("contacts", "firstName,lastName,email", new FetchConfig().lazy(10))
   *     .findList();
   *
   * }</pre>
   */
  Query<T> fetch(String assocProperty, String fetchProperties, FetchConfig fetchConfig);

  /**
   * Specify a path to load including all its properties.
   * <p>
   * The same as {@link #fetch(String, String)} with the fetchProperties as "*".
   * </p>
   * <pre>{@code
   *
   * // fetch customers (their id, name and status)
   * List<Customer> customers =
   *     ebeanServer.find(Customer.class)
   *     // eager fetch the contacts
   *     .fetch("contacts")
   *     .findList();
   *
   * }</pre>
   *
   * @param path
   *          the property of an associated (1-1,1-M,M-1,M-M) bean.
   */
  Query<T> fetch(String path);

  /**
   * Additionally specify a JoinConfig to specify a "query join" and or define
   * the lazy loading query.
   *
   *
   * <pre>{@code
   *
   * // fetch customers (their id, name and status)
   * List<Customer> customers =
   *     ebeanServer.find(Customer.class)
   *     // lazy fetch contacts with a batch size of 100
   *     .fetch("contacts", new FetchConfig().lazy(100))
   *     .findList();
   *
   * }</pre>
   */
  Query<T> fetch(String path, FetchConfig fetchConfig);

  /**
   * Apply the path properties replacing the select and fetch clauses.
   * <p>
   * This is typically used when the PathProperties is applied to both the query and the JSON output.
   * </p>
   */
  Query<T> apply(PathProperties pathProperties);

  /**
   * Execute the query returning the list of Id's.
   * <p>
   * This query will execute against the EbeanServer that was used to create it.
   * </p>
   * 
   * @see EbeanServer#findIds(Query, Transaction)
   */
  List<Object> findIds();

  /**
   * Execute the query iterating over the results.
   * <p>
   * Remember that with {@link QueryIterator} you must call
   * {@link QueryIterator#close()} when you have finished iterating the results
   * (typically in a finally block).
   * </p>
   * <p>
   * findEach() and findEachWhile() are preferred to findIterate() as they ensure
   * the jdbc statement and resultSet are closed at the end of the iteration.
   * </p>
   * <p>
   * This query will execute against the EbeanServer that was used to create it.
   * </p>
   */
  QueryIterator<T> findIterate();



  /**
   * Execute the query processing the beans one at a time.
   * <p>
   * This method is appropriate to process very large query results as the
   * beans are consumed one at a time and do not need to be held in memory
   * (unlike #findList #findSet etc)
   * </p>
   * <p>
   * Note that internally Ebean can inform the JDBC driver that it is expecting larger
   * resultSet and specifically for MySQL this hint is required to stop it's JDBC driver
   * from buffering the entire resultSet. As such, for smaller resultSets findList() is
   * generally preferable.
   * </p>
   * <p>
   * Compared with #findEachWhile this will always process all the beans where as
   * #findEachWhile provides a way to stop processing the query result early before
   * all the beans have been read.
   * </p>
   * <p>
   * This method is functionally equivalent to findIterate() but instead of using an
   * iterator uses the QueryEachConsumer (SAM) interface which is better suited to use
   * with Java8 closures.
   * </p>
   *
   * <pre>{@code
   *
   *  ebeanServer.find(Customer.class)
   *     .where().eq("status", Status.NEW)
   *     .order().asc("id")
   *     .findEach((Customer customer) -> {
   *
   *       // do something with customer
   *       System.out.println("-- visit " + customer);
   *     });
   *
   * }</pre>
   *
   * @param consumer
   *          the consumer used to process the queried beans.
   */
  void findEach(QueryEachConsumer<T> consumer);

  /**
   * Execute the query using callbacks to a visitor to process the resulting
   * beans one at a time.
   * <p>
   * This method is functionally equivalent to findIterate() but instead of using an
   * iterator uses the QueryEachWhileConsumer (SAM) interface which is better suited to use
   * with Java8 closures.
   * </p>

   *
   * <pre>{@code
   *
   *  ebeanServer.find(Customer.class)
   *     .fetch("contacts", new FetchConfig().query(2))
   *     .where().eq("status", Status.NEW)
   *     .order().asc("id")
   *     .setMaxRows(2000)
   *     .findEachWhile((Customer customer) -> {
   *
   *       // do something with customer
   *       System.out.println("-- visit " + customer);
   *
   *       // return true to continue processing or false to stop
   *       return (customer.getId() < 40);
   *     });
   *
   * }</pre>
   *
   * @param consumer
   *          the consumer used to process the queried beans.
   */
  void findEachWhile(QueryEachWhileConsumer<T> consumer);

  /**
   * Execute the query returning the list of objects.
   * <p>
   * This query will execute against the EbeanServer that was used to create it.
   * </p>
   *
   * <pre>{@code
   *
   * List<Customer> customers =
   *     ebeanServer.find(Customer.class)
   *     .where().ilike("name", "rob%")
   *     .findList();
   *
   * }</pre>
   *
   * @see EbeanServer#findList(Query, Transaction)
   */
  List<T> findList();

  /**
   * Execute the query returning the set of objects.
   * <p>
   * This query will execute against the EbeanServer that was used to create it.
   * </p>
   *
   * <pre>{@code
   *
   * Set<Customer> customers =
   *     ebeanServer.find(Customer.class)
   *     .where().ilike("name", "rob%")
   *     .findSet();
   *
   * }</pre>
   *
   * @see EbeanServer#findSet(Query, Transaction)
   */
  Set<T> findSet();

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
   * <pre>{@code
   *
   * Map<?, Product> map =
   *   ebeanServer.find(Product.class)
   *     .setMapKey("sku")
   *     .findMap();
   *
   * }</pre>
   * 
   * @see EbeanServer#findMap(Query, Transaction)
   */
  Map<?, T> findMap();

  /**
   * Return a typed map specifying the key property and type.
   */
  <K> Map<K, T> findMap(String keyProperty, Class<K> keyType);

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
   * <pre>{@code
   *
   * // assuming the sku of products is unique...
   * Product product =
   *     ebeanServer.find(Product.class)
   *         .where().eq("sku", "aa113")
   *         .findUnique();
   * ...
   * }</pre>
   * 
   * <p>
   * It is also useful with finding objects by their id when you want to specify
   * further join information.
   * </p>
   * 
   * <pre>{@code
   *
   * // Fetch order 1 and additionally fetch join its order details...
   * Order order = 
   *     ebeanServer.find(Order.class)
   *       .setId(1)
   *       .fetch("details")
   *       .findUnique();
   *
   * // the order details were eagerly loaded
   * List<OrderDetail> details = order.getDetails();
   * ...
   * }</pre>
   */
  @Nullable
  T findUnique();

  /**
   * Return versions of a @History entity bean.
   * <p>
   *   Note that this query will work against view based history implementations
   *   but not sql2011 standards based implementations that require a start and
   *   end timestamp to be specified.
   * </p>
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
   * Return the count of entities this query should return.
   * <p>
   * This is the number of 'top level' or 'root level' entities.
   * </p>
   */
  int findRowCount();

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
   * This query will execute in it's own PersistenceContext and using its own transaction.
   * What that means is that it will not share any bean instances with other queries.
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
   * the query. This translates into SQL that uses limit offset, rownum or row_number function to
   * limit the result set.
   * </p>
   *
   * <h4>Example: typical use including total row count</h4>
   * <pre>{@code
   *
   *     // We want to find the first 100 new orders
   *     //  ... 0 means first page
   *     //  ... page size is 100
   *
   *     PagedList<Order> pagedList
   *       = ebeanServer.find(Order.class)
   *       .where().eq("status", Order.Status.NEW)
   *       .order().asc("id")
   *       .findPagedList(0, 100);
   *
   *     // Optional: initiate the loading of the total
   *     // row count in a background thread
   *     pagedList.loadRowCount();
   *
   *     // fetch and return the list in the foreground thread
   *     List<Order> orders = pagedList.getList();
   *
   *     // get the total row count (from the future)
   *     int totalRowCount = pagedList.getTotalRowCount();
   *
   * }</pre>
   *
   * @param pageIndex
   *          The zero based index of the page.
   * @param pageSize
   *          The number of beans to return per page.
   * @return The PagedList
   */
  PagedList<T> findPagedList(int pageIndex, int pageSize);

  /**
   * Set a named bind parameter. Named parameters have a colon to prefix the name.
   * 
   * <pre>{@code
   *
   * // a query with a named parameter
   * String oql = "find order where status = :orderStatus";
   * 
   * Query<Order> query = ebeanServer.find(Order.class, oql);
   * 
   * // bind the named parameter
   * query.bind("orderStatus", OrderStatus.NEW);
   * List<Order> list = query.findList();
   *
   * }</pre>
   * 
   * @param name
   *          the parameter name
   * @param value
   *          the parameter value
   */
  Query<T> setParameter(String name, Object value);

  /**
   * Set an ordered bind parameter according to its position. Note that the
   * position starts at 1 to be consistent with JDBC PreparedStatement. You need
   * to set a parameter value for each ? you have in the query.
   * 
   * <pre>{@code
   *
   * // a query with a positioned parameter
   * String oql = "where status = ? order by id desc";
   * 
   * Query<Order> query = ebeanServer.createQuery(Order.class, oql);
   * 
   * // bind the parameter
   * query.setParameter(1, OrderStatus.NEW);
   * 
   * List<Order> list = query.findList();
   *
   * }</pre>
   * 
   * @param position
   *          the parameter bind position starting from 1 (not 0)
   * @param value
   *          the parameter bind value.
   */
  Query<T> setParameter(int position, Object value);

  /**
   * Set the Id value to query. This is used with findUnique().
   * <p>
   * You can use this to have further control over the query. For example adding
   * fetch joins.
   * </p>
   * 
   * <pre>{@code
   *
   * Order order =
   *     ebeanServer.find(Order.class)
   *     .setId(1)
   *     .fetch("details")
   *     .findUnique();
   *
   * // the order details were eagerly fetched
   * List<OrderDetail> details = order.getDetails();
   *
   * }</pre>
   */
  Query<T> setId(Object id);

  /**
   * Return the Id value.
   */
  Object getId();

  /**
   * Add additional clause(s) to the where clause.
   * <p>
   * This typically contains named parameters which will need to be set via
   * {@link #setParameter(String, Object)}.
   * </p>
   * 
   * <pre>{@code
   *
   * Query<Order> query = ebeanServer.createQuery(Order.class, "top");
   * ...
   * if (...) {
   *   query.where("status = :status and lower(customer.name) like :custName");
   *   query.setParameter("status", Order.NEW);
   *   query.setParameter("custName", "rob%");
   * }
   *
   * }</pre>
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
  Query<T> where(String addToWhereClause);

  /**
   * Add a single Expression to the where clause returning the query.
   * 
   * <pre>{@code
   *
   * List<Order> newOrders = 
   *     ebeanServer.find(Order.class)
   * 		.where().eq("status", Order.NEW)
   * 		.findList();
   * ...
   *
   * }</pre>
   */
  Query<T> where(Expression expression);

  /**
   * Add Expressions to the where clause with the ability to chain on the
   * ExpressionList. You can use this for adding multiple expressions to the
   * where clause.
   * 
   * <pre>{@code
   *
   * List<Order> orders =
   *     ebeanServer.find(Order.class)
   *     .where()
   *       .eq("status", Order.NEW)
   *       .ilike("customer.name","rob%")
   *     .findList();
   *
   * }</pre>
   * 
   * @see Expr
   * @return The ExpressionList for adding expressions to.
   */
  ExpressionList<T> where();

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
   * <pre>{@code
   * 
   * List<Customer> list =
   *     ebeanServer.find(Customer.class)
   *     // .fetch("orders", new FetchConfig().lazy())
   *     // .fetch("orders", new FetchConfig().query())
   *     .fetch("orders")
   *     .where().ilike("name", "rob%")
   *     .filterMany("orders").eq("status", Order.Status.NEW).gt("orderDate", lastWeek)
   *     .findList();
   * 
   * }</pre>
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
  ExpressionList<T> filterMany(String propertyName);

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
  ExpressionList<T> having();

  /**
   * Add additional clause(s) to the having clause.
   * <p>
   * This typically contains named parameters which will need to be set via
   * {@link #setParameter(String, Object)}.
   * </p>
   * 
   * <pre>{@code
   *
   * List<ReportOrder> query =
   *     ebeanServer.find(ReportOrder.class)
   *     .having("score > :min").setParameter("min", 1)
   *     .findList();
   *
   * }</pre>
   * 
   * @param addToHavingClause
   *          the clause to append to the having clause which typically contains
   *          named parameters.
   * @return The query object
   */
  Query<T> having(String addToHavingClause);

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
  Query<T> having(Expression addExpressionToHaving);

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
  Query<T> orderBy(String orderByClause);

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
   * <p>
   * This is EXACTLY the same as {@link #orderBy()}.
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
   * <p>
   * This is EXACTLY the same as {@link #order()}.
   * </p>
   */
  OrderBy<T> orderBy();

  /**
   * Set an OrderBy object to replace any existing OrderBy clause.
   * <p>
   * This is EXACTLY the same as {@link #setOrderBy(OrderBy)}.
   * </p>
   */
  Query<T> setOrder(OrderBy<T> orderBy);

  /**
   * Set an OrderBy object to replace any existing OrderBy clause.
   * <p>
   * This is EXACTLY the same as {@link #setOrder(OrderBy)}.
   * </p>
   */
  Query<T> setOrderBy(OrderBy<T> orderBy);

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
   *          .select("name")
   *          .findList();
   *
   * }</pre>
   */
  Query<T> setDistinct(boolean isDistinct);

  /**
   * Return the first row value.
   */
  int getFirstRow();

  /**
   * Set the first row to return for this query.
   *
   * @param firstRow the first row to include in the query result.
   */
  Query<T> setFirstRow(int firstRow);

  /**
   * Return the max rows for this query.
   */
  int getMaxRows();

  /**
   * Set the maximum number of rows to return in the query.
   * 
   * @param maxRows
   *          the maximum number of rows to return in the query.
   */
  Query<T> setMaxRows(int maxRows);

  /**
   * Set the property to use as keys for a map.
   * <p>
   * If no property is set then the id property is used.
   * </p>
   * 
   * <pre>{@code
   *
   * // Assuming sku is unique for products...
   *    
   * Map<?,Product> productMap =
   *     ebeanServer.find(Product.class)
   *     // use sku for keys...
   *     .setMapKey("sku")
   *     .findMap();
   *
   * }</pre>
   * 
   * @param mapKey
   *          the property to use as keys for a map.
   */
  Query<T> setMapKey(String mapKey);

  /**
   * Set this to true to use the bean cache.
   * <p>
   * If the query result is in cache then by default this same instance is
   * returned. In this sense it should be treated as a read only object graph.
   * </p>
   */
  Query<T> setUseCache(boolean useBeanCache);

  /**
   * Set this to true to use the query cache.
   */
  Query<T> setUseQueryCache(boolean useQueryCache);

  /**
   * When set to true when you want the returned beans to be read only.
   */
  Query<T> setReadOnly(boolean readOnly);

  /**
   * When set to true all the beans from this query are loaded into the bean
   * cache.
   */
  Query<T> setLoadBeanCache(boolean loadBeanCache);

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
  Query<T> setTimeout(int secs);

  /**
   * A hint which for JDBC translates to the Statement.fetchSize().
   * <p>
   * Gives the JDBC driver a hint as to the number of rows that should be
   * fetched from the database when more rows are needed for ResultSet.
   * </p>
   */
  Query<T> setBufferFetchSizeHint(int fetchSize);

  /**
   * Return the sql that was generated for executing this query.
   * <p>
   * This is only available after the query has been executed and provided only
   * for informational purposes.
   * </p>
   */
  String getGeneratedSql();

  /**
   * executed the select with "for update" which should lock the record
   * "on read"
   */
  Query<T> setForUpdate(boolean forUpdate);

  /**
   * Return true if this query has forUpdate set.
   */
  boolean isForUpdate();
  
  /**
   * Set root table alias.
   */
  Query<T> alias(String alias);

  /**
   * Return the type of beans being queried.
   */
  Class<T> getBeanType();

  /**
   * Set true if you want to disable lazy loading.
   * <p>
   *   That is, once the object graph is returned further lazy loading is disabled.
   * </p>
   */
  Query<T> setDisableLazyLoading(boolean disableLazyLoading);

  /**
   * Returns the set of properties or paths that are unknown (do not map to known properties or paths).
   * <p>
   * Validate the query checking the where and orderBy expression paths to confirm if
   * they represent valid properties or paths for the given bean type.
   * </p>
   */
  Set<String> validate();
}

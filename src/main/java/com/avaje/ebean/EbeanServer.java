package com.avaje.ebean;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import com.avaje.ebean.annotation.CacheStrategy;
import com.avaje.ebean.cache.ServerCacheManager;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.meta.MetaInfoManager;
import com.avaje.ebean.text.csv.CsvReader;
import com.avaje.ebean.text.json.JsonContext;

/**
 * Provides the API for fetching and saving beans to a particular DataSource.
 * <p>
 * <b>Registration with the Ebean Singleton:</b><br/>
 * When a EbeanServer is constructed it can be registered with the Ebean
 * singleton (see {@link ServerConfig#setRegister(boolean)}). The Ebean
 * singleton is essentially a map of EbeanServer's that have been registered
 * with it. The EbeanServer can then be retrieved later via
 * {@link Ebean#getServer(String)}.
 * </p>
 * <p>
 * <b>The 'default' EbeanServer</b><br/>
 * One EbeanServer can be designated as the 'default' or 'primary' EbeanServer
 * (see {@link ServerConfig#setDefaultServer(boolean)}. Many methods on Ebean
 * such as {@link Ebean#find(Class)} etc are actually just a convenient way to
 * call methods on the 'default/primary' EbeanServer. This is handy for
 * applications that use a single DataSource.
 * <p>
 * There is one EbeanServer per Database (javax.sql.DataSource). One EbeanServer
 * is referred to as the <em>'default'</em> server and that is the one that
 * Ebean methods such as {@link Ebean#find(Class)} use.
 * </p>
 * <p>
 * <b>Constructing a EbeanServer</b><br/>
 * EbeanServer's are constructed by the EbeanServerFactory. They can be created
 * programmatically via {@link EbeanServerFactory#create(ServerConfig)} or they
 * can be automatically constructed on demand using configuration information in
 * the ebean.properties file.
 * </p>
 * <p>
 * Example: Get a EbeanServer
 * </p>
 * 
 * <pre>{@code
 * // Get access to the Human Resources EbeanServer/Database
 * EbeanServer hrServer = Ebean.getServer("HR");
 * 
 * 
 * // fetch contact 3 from the HR database Contact contact =
 * hrServer.find(Contact.class, new Integer(3));
 * 
 * contact.setStatus("INACTIVE"); ...
 * 
 * // save the contact back to the HR database hrServer.save(contact);
 * }</pre>
 * 
 * <p>
 * <b>EbeanServer has more API than Ebean</b><br/>
 * EbeanServer provides additional API compared with Ebean. For example it
 * provides more control over the use of Transactions that is not available in
 * the Ebean API.
 * </p>
 * <p>
 * <em>External Transactions:</em> If you wanted to use transactions created
 * externally to eBean then EbeanServer provides additional methods where you
 * can explicitly pass a transaction (that can be created externally).
 * </p>
 * <p>
 * <em>Bypass ThreadLocal Mechanism:</em> If you want to bypass the built in
 * ThreadLocal transaction management you can use the createTransaction()
 * method. Example: a single thread requires more than one transaction.
 * </p>
 * 
 * @see Ebean
 * @see EbeanServerFactory
 * @see ServerConfig
 */
public interface EbeanServer {

  /**
   * Shutdown the EbeanServer programmatically.
   * <p>
   * This method is not normally required. Ebean registers a shutdown hook and shuts down cleanly.
   * </p>
   * <p>
   * If the under underlying DataSource is the Ebean implementation then you
   * also have the option of shutting down the DataSource and deregistering the
   * JDBC driver.
   * </p>
   * 
   * @param shutdownDataSource
   *          if true then shutdown the underlying DataSource if it is the EbeanORM
   *          DataSource implementation.
   * @param deregisterDriver
   *          if true then deregister the JDBC driver if it is the EbeanORM
   *          DataSource implementation.
   */
  public void shutdown(boolean shutdownDataSource, boolean deregisterDriver);
  
  /**
   * Return the AdminAutofetch which is used to control and configure the
   * Autofetch service at runtime.
   */
  public AdminAutofetch getAdminAutofetch();

  /**
   * Return the name. This is used with {@link Ebean#getServer(String)} to get a
   * EbeanServer that was registered with the Ebean singleton.
   */
  public String getName();

  /**
   * Return the ExpressionFactory for this server.
   */
  public ExpressionFactory getExpressionFactory();

  /**
   * Return the MetaInfoManager which is used to get meta data from the EbeanServer
   * such as query execution statistics.
   */
  public MetaInfoManager getMetaInfoManager();

  /**
   * Return the BeanState for a given entity bean.
   * <p>
   * This will return null if the bean is not an enhanced entity bean.
   * </p>
   */
  public BeanState getBeanState(Object bean);

  /**
   * Return the value of the Id property for a given bean.
   */
  public Object getBeanId(Object bean);

  /**
   * Return a map of the differences between two objects of the same type.
   * <p>
   * When null is passed in for b, then the 'OldValues' of a is used for the
   * difference comparison.
   * </p>
   */
  public Map<String, ValuePair> diff(Object a, Object b);

  /**
   * Create a new instance of T that is an EntityBean.
   * <p>
   * Generally not expected to be useful (now dynamic subclassing support was removed in
   * favour of always using enhancement).
   * </p>
   */
  public <T> T createEntityBean(Class<T> type);

  /**
   * Create a CsvReader for a given beanType.
   */
  public <T> CsvReader<T> createCsvReader(Class<T> beanType);

  /**
   * Return a named Query that will have defined fetch paths, predicates etc.
   * <p>
   * The query is created from a statement that will be defined in a deployment
   * orm xml file or NamedQuery annotations. The query will typically already
   * define fetch paths, predicates, order by clauses etc so often you will just
   * need to bind required parameters and then execute the query.
   * </p>
   *
   * <pre>{@code
   *
   *   // example
   *   Query<Order> query = ebeanServer.createNamedQuery(Order.class, "new.for.customer");
   *   query.setParameter("customerId", 23);
   *   List<Order> newOrders = query.findList();
   *
   * }</pre>
   */
  public <T> Query<T> createNamedQuery(Class<T> beanType, String namedQuery);

  /**
   * Create a query using the query language.
   * <p>
   * Note that you are allowed to add additional clauses using where() as well
   * as use fetch() and setOrderBy() after the query has been created.
   * </p>
   * <p>
   * Note that this method signature used to map to named queries and that has
   * moved to {@link #createNamedQuery(Class, String)}.
   * </p>
   * 
   * <pre>{@code
   *  EbeanServer ebeanServer = ... ;
   *  String q = "find order fetch details where status = :st";
   *  
   *  List<Order> newOrders
   *        = ebeanServer.createQuery(Order.class, q)
   *             .setParameter("st", Order.Status.NEW)
   *             .findList();
   * }</pre>
   * 
   * @param query
   *          the object query
   */
  public <T> Query<T> createQuery(Class<T> beanType, String query);

  /**
   * Create a query for an entity bean and synonym for {@link #find(Class)}.
   *
   * @see #find(Class)
   */
  public <T> Query<T> createQuery(Class<T> beanType);

  /**
   * Create a query for a type of entity bean.
   * <p>
   * You can use the methods on the Query object to specify fetch paths,
   * predicates, order by, limits etc.
   * </p>
   * <p>
   * You then use findList(), findSet(), findMap() and findUnique() to execute
   * the query and return the collection or bean.
   * </p>
   * <p>
   * Note that a query executed by {@link Query#findList()}
   * {@link Query#findSet()} etc will execute against the same EbeanServer from
   * which is was created.
   * </p>
   *
   * <pre>{@code
   *
   *   // Find order 2 specifying explicitly the parts of the object graph to
   *   // eagerly fetch. In this case eagerly fetch the associated customer,
   *   // details and details.product.name
   *
   *   Order order = ebeanServer.find(Order.class)
   *     .fetch("customer")
   *     .fetch("details")
   *     .fetch("detail.product", "name")
   *     .setId(2)
   *     .findUnique();
   *
   *   // find some new orders ... with firstRow/maxRows
   *   List<Order> orders =
   *     ebeanServer.find(Order.class)
   *       .where().eq("status", Order.Status.NEW)
   *       .setFirstRow(20)
   *       .setMaxRows(10)
   *       .findList();
   *
   * }</pre>
   *
   */
  public <T> Query<T> find(Class<T> beanType);

  /**
   * Return the next unique identity value for a given bean type.
   * <p>
   * This will only work when a IdGenerator is on the bean such as for beans
   * that use a DB sequence or UUID.
   * </p>
   * <p>
   * For DB's supporting getGeneratedKeys and sequences such as Oracle10 you do
   * not need to use this method generally. It is made available for more
   * complex cases where it is useful to get an ID prior to some processing.
   * </p>
   */
  public Object nextId(Class<?> beanType);

  /**
   * Create a filter for sorting and filtering lists of entities locally without
   * going back to the database.
   * <p>
   * This produces and returns a new list with the sort and filters applied.
   * </p>
   * <p>
   * Refer to {@link Filter} for an example of its use.
   * </p>
   */
  public <T> Filter<T> filter(Class<T> beanType);

  /**
   * Sort the list in memory using the sortByClause which can contain a comma delimited
   * list of property names and keywords asc, desc, nullsHigh and nullsLow.
   * <ul>
   * <li>asc - ascending order (which is the default)</li>
   * <li>desc - Descending order</li>
   * <li>nullsHigh - Treat null values as high/large values (which is the
   * default)</li>
   * <li>nullsLow- Treat null values as low/very small values</li>
   * </ul>
   * <p>
   * If you leave off any keywords the defaults are ascending order and treating
   * nulls as high values.
   * </p>
   * <p>
   * Note that the sorting uses a Comparator and Collections.sort(); and does
   * not invoke a DB query.
   * </p>
   *
   * <pre>{@code
   *
   *   // find orders and their customers
   *   List<Order> list = ebeanServer.find(Order.class)
   *     .fetch("customer")
   *     .orderBy("id")
   *     .findList();
   *
   *   // sort by customer name ascending, then by order shipDate
   *   // ... then by the order status descending
   *   ebeanServer.sort(list, "customer.name, shipDate, status desc");
   *
   *   // sort by customer name descending (with nulls low)
   *   // ... then by the order id
   *   ebeanServer.sort(list, "customer.name desc nullsLow, id");
   *
   * }</pre>
   *
   * @param list
   *          the list of entity beans
   * @param sortByClause
   *          the properties to sort the list by
   */
  public <T> void sort(List<T> list, String sortByClause);

  /**
   * Create a named orm update. The update statement is specified via the
   * NamedUpdate annotation.
   * <p>
   * The orm update differs from the SqlUpdate in that it uses the bean name and
   * bean property names rather than table and column names.
   * </p>
   * <p>
   * Note that named update statements can be specified in raw sql (with column
   * and table names) or using bean name and bean property names. This can be
   * specified with the isSql flag.
   * </p>
   * <p>
   * Example named updates:
   * </p>
   *
   * <pre>{@code
   *   package app.data;
   *
   *   import ...
   *
   *   @NamedUpdates(value = {
   *    @NamedUpdate( name = "setTitle",
   * 	    isSql = false,
   * 		  notifyCache = false,
   * 		  update = "update topic set title = :title, postCount = :postCount where id = :id"),
   * 	  @NamedUpdate( name = "setPostCount",
   * 		  notifyCache = false,
   * 		  update = "update f_topic set post_count = :postCount where id = :id"),
   * 	  @NamedUpdate( name = "incrementPostCount",
   * 		  notifyCache = false,
   * 		  isSql = false,
   * 		  update = "update Topic set postCount = postCount + 1 where id = :id") })
   *   @Entity
   *   @Table(name = "f_topic")
   *   public class Topic { ...
   *
   * }</pre>
   *
   * <p>
   * Example using a named update:
   * </p>
   *
   * <pre>{@code
   *
   *   Update<Topic> update = ebeanServer.createNamedUpdate(Topic.class, "setPostCount");
   *   update.setParameter("postCount", 10);
   *   update.setParameter("id", 3);
   *
   *   int rows = update.execute();
   *   System.out.println("rows updated: " + rows);
   *
   * }</pre>
   */
  public <T> Update<T> createNamedUpdate(Class<T> beanType, String namedUpdate);

  /**
   * Create a orm update where you will supply the insert/update or delete
   * statement (rather than using a named one that is already defined using the
   * &#064;NamedUpdates annotation).
   * <p>
   * The orm update differs from the sql update in that it you can use the bean
   * name and bean property names rather than table and column names.
   * </p>
   * <p>
   * An example:
   * </p>
   *
   * <pre>{@code
   *
   *   // The bean name and properties - "topic","postCount" and "id"
   *
   *   // will be converted into their associated table and column names
   *   String updStatement = "update topic set postCount = :pc where id = :id";
   *
   *   Update<Topic> update = ebeanServer.createUpdate(Topic.class, updStatement);
   *
   *   update.set("pc", 9);
   *   update.set("id", 3);
   *
   *   int rows = update.execute();
   *   System.out.println("rows updated:" + rows);
   *
   * }</pre>
   */
  public <T> Update<T> createUpdate(Class<T> beanType, String ormUpdate);

  /**
   * Create a SqlQuery for executing native sql
   * query statements.
   * <p>
   * Note that you can use raw SQL with entity beans, refer to the SqlSelect
   * annotation for examples.
   * </p>
   */
  public SqlQuery createSqlQuery(String sql);

  /**
   * Create a named sql query.
   * <p>
   * The query statement will be defined in a deployment orm xml file.
   * </p>
   *
   * @param namedQuery
   *          the name of the query
   */
  public SqlQuery createNamedSqlQuery(String namedQuery);

  /**
   * Create a sql update for executing native dml statements.
   * <p>
   * Use this to execute a Insert Update or Delete statement. The statement will
   * be native to the database and contain database table and column names.
   * </p>
   * <p>
   * See {@link SqlUpdate} for example usage.
   * </p>
   * <p>
   * Where possible it would be expected practice to put the statement in a orm
   * xml file (named update) and use {@link #createNamedSqlUpdate(String)} .
   * </p>
   */
  public SqlUpdate createSqlUpdate(String sql);

  /**
   * Create a CallableSql to execute a given stored procedure.
   */
  public CallableSql createCallableSql(String callableSql);

  /**
   * Create a named sql update.
   * <p>
   * The statement (an Insert Update or Delete statement) will be defined in a
   * deployment orm xml file.
   * </p>
   *
   * <pre>{@code
   *
   *   // Use a namedQuery
   *   UpdateSql update = Ebean.createNamedSqlUpdate("update.topic.count");
   *
   *   update.setParameter("count", 1);
   *   update.setParameter("topicId", 50);
   *
   *   int modifiedCount = update.execute();
   *
   * }</pre>
   */
  public SqlUpdate createNamedSqlUpdate(String namedQuery);

  /**
   * Register a TransactionCallback on the currently active transaction.
   * <p/>
   * If there is no currently active transaction then a PersistenceException is thrown.
   *
   * @param transactionCallback The transaction callback to be registered with the current transaction.
   *
   * @throws PersistenceException If there is no currently active transaction
   */
  public void register(TransactionCallback transactionCallback) throws PersistenceException;

  /**
   * Create a new transaction that is not held in TransactionThreadLocal.
   * <p>
   * You will want to do this if you want multiple Transactions in a single
   * thread or generally use transactions outside of the TransactionThreadLocal
   * management.
   * </p>
   */
  public Transaction createTransaction();

  /**
   * Create a new transaction additionally specifying the isolation level.
   * <p>
   * Note that this transaction is NOT stored in a thread local.
   * </p>
   */
  public Transaction createTransaction(TxIsolation isolation);

  /**
   * Start a new explicit transaction putting it into a ThreadLocal.
   * <p>
   * The transaction is stored in a ThreadLocal variable and typically you only
   * need to use the returned Transaction <em>IF</em> you wish to do things like
   * use batch mode, change the transaction isolation level, use savepoints or
   * log comments to the transaction log.
   * </p>
   * <p>
   * Example of using a transaction to span multiple calls to find(), save()
   * etc.
   * </p>
   *
   * <pre>{@code
   *
   *    // start a transaction (stored in a ThreadLocal)
   *    ebeanServer.beginTransaction();
   *    try {
   * 	    Order order = ebeanServer.find(Order.class,10);
   *
   * 	    ebeanServer.save(order);
   *
   * 	    ebeanServer.commitTransaction();
   *
   *    } finally {
   * 	    // rollback if we didn't commit
   * 	    // i.e. an exception occurred before commitTransaction().
   * 	    ebeanServer.endTransaction();
   *    }
   *
   * }</pre>
   *
   * <h3>Transaction options:</h3>
   * <pre>{@code
   *
   *     Transaction txn = ebeanServer.beginTransaction();
   *     try {
   *       // explicitly turn on/off JDBC batch use
   *       txn.setBatchMode(true);
   *       txn.setBatchSize(50);
   *
   *       // control flushing when mixing save and queries
   *       txn.setBatchFlushOnQuery(false);
   *
   *       // turn off persist cascade if needed
   *       txn.setPersistCascade(false);
   *
   *       // for large batch insert processing when we do not
   *       // ... need the generatedKeys, don't get them
   *       txn.setBatchGetGeneratedKeys(false);
   *
   *       // explicitly flush the JDBC batch buffer
   *       txn.flushBatch();
   *
   *       ...
   *
   *       txn.commit();
   *
   *    } finally {
   *       // rollback if necessary
   *       txn.end();
   *    }
   *
   * }</pre>
   *
   * <p>
   * If you want to externalise the transaction management then you use
   * createTransaction() and pass the transaction around to the various methods on
   * EbeanServer yourself.
   * </p>
   */
  public Transaction beginTransaction();

  /**
   * Start a transaction additionally specifying the isolation level.
   */
  public Transaction beginTransaction(TxIsolation isolation);

  /**
   * Returns the current transaction or null if there is no current transaction in scope.
   */
  public Transaction currentTransaction();

  /**
   * Commit the current transaction.
   */
  public void commitTransaction();

  /**
   * Rollback the current transaction.
   */
  public void rollbackTransaction();

  /**
   * If the current transaction has already been committed do nothing otherwise
   * rollback the transaction.
   * <p>
   * Useful to put in a finally block to ensure the transaction is ended, rather
   * than a rollbackTransaction() in each catch block.
   * </p>
   * <p>
   * Code example:
   * 
   * <pre>{@code
   *
   *   ebeanServer.beginTransaction();
   *   try {
   *     // do some fetching and or persisting ...
   * 
   *     // commit at the end
   *     ebeanServer.commitTransaction();
   * 
   *   } finally {
   *     // if commit didn't occur then rollback the transaction
   *     ebeanServer.endTransaction();
   *   }
   *
   * }</pre>
   * 
   * </p>
   *
   */
  public void endTransaction();

  /**
   * Refresh the values of a bean.
   * <p>
   * Note that this resets OneToMany and ManyToMany properties so that if they
   * are accessed a lazy load will refresh the many property.
   * </p>
   */
  public void refresh(Object bean);

  /**
   * Refresh a many property of an entity bean.
   * 
   * @param bean
   *          the entity bean containing the 'many' property
   * @param propertyName
   *          the 'many' property to be refreshed
   *
   */
  public void refreshMany(Object bean, String propertyName);

  /**
   * Find a bean using its unique id.
   *
   * <pre>{@code
   *   // Fetch order 1
   *   Order order = ebeanServer.find(Order.class, 1);
   * }</pre>
   *
   * <p>
   * If you want more control over the query then you can use createQuery() and
   * Query.findUnique();
   * </p>
   *
   * <pre>{@code
   *   // ... additionally fetching customer, customer shipping address,
   *   // order details, and the product associated with each order detail.
   *   // note: only product id and name is fetch (its a "partial object").
   *   // note: all other objects use "*" and have all their properties fetched.
   *
   *   Query<Order> query = ebeanServer.find(Order.class)
   *     .setId(1)
   *     .fetch("customer")
   *     .fetch("customer.shippingAddress")
   *     .fetch("details")
   *     .query();
   *
   *   // fetch associated products but only fetch their product id and name
   *   query.fetch("details.product", "name");
   *
   *
   *   Order order = query.findUnique();
   *
   *   // traverse the object graph...
   *
   *   Customer customer = order.getCustomer();
   *   Address shippingAddress = customer.getShippingAddress();
   *   List<OrderDetail> details = order.getDetails();
   *   OrderDetail detail0 = details.get(0);
   *   Product product = detail0.getProduct();
   *   String productName = product.getName();
   *
   * }</pre>
   *
   * @param beanType
   *          the type of entity bean to fetch
   * @param id
   *          the id value
   */
  public <T> T find(Class<T> beanType, Object id);

  /**
   * Get a reference object.
   * <p>
   * This will not perform a query against the database unless some property other
   * that the id property is accessed.
   * </p>
   * <p>
   * It is most commonly used to set a 'foreign key' on another bean like:
   * </p>
   * <pre>{@code
   *
   *   Product product = ebeanServer.getReference(Product.class, 1);
   *
   *   OrderDetail orderDetail = new OrderDetail();
   *   // set the product 'foreign key'
   *   orderDetail.setProduct(product);
   *   orderDetail.setQuantity(42);
   *   ...
   *
   *   ebeanServer.save(orderDetail);
   *
   *
   * }</pre>
   *
   * <h3>Lazy loading characteristics</h3>
   * <pre>{@code
   *
   *   Product product = ebeanServer.getReference(Product.class, 1);
   *
   *   // You can get the id without causing a fetch/lazy load
   *   Long productId = product.getId();
   *
   *   // If you try to get any other property a fetch/lazy loading will occur
   *   // This will cause a query to execute...
   *   String name = product.getName();
   *
   * }</pre>
   *
   * @param beanType
   *          the type of entity bean
   * @param id
   *          the id value
   */
  public <T> T getReference(Class<T> beanType, Object id);

  /**
   * Return the number of 'top level' or 'root' entities this query should
   * return.
   *
   * @see Query#findRowCount()
   * @see com.avaje.ebean.Query#findFutureRowCount()
   */
  public <T> int findRowCount(Query<T> query, Transaction transaction);

  /**
   * Return the Id values of the query as a List.
   *
   * @see com.avaje.ebean.Query#findIds()
   */
  public <T> List<Object> findIds(Query<T> query, Transaction transaction);

  /**
   * Return a QueryIterator for the query.
   * <p>
   * Generally using {@link #findEach(Query, QueryEachConsumer, Transaction)} or
   * {@link #findEachWhile(Query, QueryEachWhileConsumer, Transaction)} is preferred
   * to findIterate(). The reason is that those methods automatically take care of
   * closing the queryIterator (and the underlying jdbc statement and resultSet).
   * </p>
   * <p>
   * This is similar to findEach in that not all the result beans need to be held
   * in memory at the same time and as such is good for processing large queries.
   * </p>
   *
   * @see Query#findEach(QueryEachConsumer)
   * @see Query#findEachWhile(QueryEachWhileConsumer)
   */
  public <T> QueryIterator<T> findIterate(Query<T> query, Transaction transaction);

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
   *
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
   * @see Query#findEach(QueryEachConsumer)
   * @see Query#findEachWhile(QueryEachWhileConsumer)
   */
  public <T> void findEach(Query<T> query, QueryEachConsumer<T> consumer, Transaction transaction);

  /**
   * Execute the query visiting the each bean one at a time.
   * <p>
   * Compared to findEach() this provides the ability to stop processing the query
   * results early by returning false for the QueryEachWhileConsumer.
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
   *
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
   * @see Query#findEach(QueryEachConsumer)
   * @see Query#findEachWhile(QueryEachWhileConsumer)
   */
  public <T> void findEachWhile(Query<T> query, QueryEachWhileConsumer<T> consumer, Transaction transaction);

  /**
   * Deprecated in favor of #findEachWhile which is functionally exactly the same
   * but has a much better name.
   * <p>
   * Execute the query visiting the results. This is similar to findIterate in
   * that not all the result beans need to be held in memory at the same time
   * and as such is go for processing large queries.
   * </p>
   *
   * @deprecated
   */
  public <T> void findVisit(Query<T> query, QueryResultVisitor<T> visitor, Transaction transaction);

  /**
   * Execute a query returning a list of beans.
   * <p>
   * Generally you are able to use {@link Query#findList()} rather than
   * explicitly calling this method. You could use this method if you wish to
   * explicitly control the transaction used for the query.
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
   * @param <T>
   *          the type of entity bean to fetch.
   * @param query
   *          the query to execute.
   * @param transaction
   *          the transaction to use (can be null).
   * @return the list of fetched beans.
   *
   * @see Query#findList()
   */
  public <T> List<T> findList(Query<T> query, Transaction transaction);

  /**
   * Execute find row count query in a background thread.
   * <p>
   * This returns a Future object which can be used to cancel, check the
   * execution status (isDone etc) and get the value (with or without a
   * timeout).
   * </p>
   * 
   * @param query
   *          the query to execute the row count on
   * @param transaction
   *          the transaction (can be null).
   * @return a Future object for the row count query
   *
   * @see com.avaje.ebean.Query#findFutureRowCount()
   */
  public <T> FutureRowCount<T> findFutureRowCount(Query<T> query, Transaction transaction);

  /**
   * Execute find Id's query in a background thread.
   * <p>
   * This returns a Future object which can be used to cancel, check the
   * execution status (isDone etc) and get the value (with or without a
   * timeout).
   * </p>
   * 
   * @param query
   *          the query to execute the fetch Id's on
   * @param transaction
   *          the transaction (can be null).
   * @return a Future object for the list of Id's
   *
   * @see com.avaje.ebean.Query#findFutureIds()
   */
  public <T> FutureIds<T> findFutureIds(Query<T> query, Transaction transaction);

  /**
   * Execute find list query in a background thread returning a FutureList object.
   * <p>
   * This returns a Future object which can be used to cancel, check the
   * execution status (isDone etc) and get the value (with or without a timeout).
   * <p>
   * This query will execute in it's own PersistenceContext and using its own transaction.
   * What that means is that it will not share any bean instances with other queries.
   *
   *
   * @param query
   *          the query to execute in the background
   * @param transaction
   *          the transaction (can be null).
   * @return a Future object for the list result of the query
   *
   * @see Query#findFutureList()
   */
  public <T> FutureList<T> findFutureList(Query<T> query, Transaction transaction);

  /**
   * Execute find list SQL query in a background thread.
   * <p>
   * This returns a Future object which can be used to cancel, check the
   * execution status (isDone etc) and get the value (with or without a
   * timeout).
   * </p>
   *
   * @param query
   *          the query to execute in the background
   * @param transaction
   *          the transaction (can be null).
   * @return a Future object for the list result of the query
   */
  public SqlFutureList findFutureList(SqlQuery query, Transaction transaction);

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
   *
   * @see Query#findPagedList(int, int)
   */
  public <T> PagedList<T> findPagedList(Query<T> query, Transaction transaction, int pageIndex, int pageSize);

  /**
   * Execute the query returning a set of entity beans.
   * <p>
   * Generally you are able to use {@link Query#findSet()} rather than
   * explicitly calling this method. You could use this method if you wish to
   * explicitly control the transaction used for the query.
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
   * @param <T>
   *          the type of entity bean to fetch.
   * @param query
   *          the query to execute
   * @param transaction
   *          the transaction to use (can be null).
   * @return the set of fetched beans.
   *
   * @see Query#findSet()
   */
  public <T> Set<T> findSet(Query<T> query, Transaction transaction);

  /**
   * Execute the query returning the entity beans in a Map.
   * <p>
   * Generally you are able to use {@link Query#findMap()} rather than
   * explicitly calling this method. You could use this method if you wish to
   * explicitly control the transaction used for the query.
   * </p>
   * 
   * @param <T>
   *          the type of entity bean to fetch.
   * @param query
   *          the query to execute.
   * @param transaction
   *          the transaction to use (can be null).
   * @return the map of fetched beans.
   *
   * @see Query#findMap()
   */
  public <T> Map<?, T> findMap(Query<T> query, Transaction transaction);

  /**
   * Execute the query returning at most one entity bean. This will throw a
   * PersistenceException if the query finds more than one result.
   * <p>
   * Generally you are able to use {@link Query#findUnique()} rather than
   * explicitly calling this method. You could use this method if you wish to
   * explicitly control the transaction used for the query.
   * </p>
   * 
   * @param <T>
   *          the type of entity bean to fetch.
   * @param query
   *          the query to execute.
   * @param transaction
   *          the transaction to use (can be null).
   * @return the list of fetched beans.
   *
   * @see Query#findUnique()
   */
  public <T> T findUnique(Query<T> query, Transaction transaction);

  /**
   * Execute the sql query returning a list of MapBean.
   * <p>
   * Generally you are able to use {@link SqlQuery#findList()} rather than
   * explicitly calling this method. You could use this method if you wish to
   * explicitly control the transaction used for the query.
   * </p>
   * 
   * @param query
   *          the query to execute.
   * @param transaction
   *          the transaction to use (can be null).
   * @return the list of fetched MapBean.
   *
   * @see SqlQuery#findList()
   */
  public List<SqlRow> findList(SqlQuery query, Transaction transaction);

  /**
   * Execute the sql query returning a set of MapBean.
   * <p>
   * Generally you are able to use {@link SqlQuery#findSet()} rather than
   * explicitly calling this method. You could use this method if you wish to
   * explicitly control the transaction used for the query.
   * </p>
   * 
   * @param query
   *          the query to execute.
   * @param transaction
   *          the transaction to use (can be null).
   * @return the set of fetched MapBean.
   *
   * @see SqlQuery#findSet()
   */
  public Set<SqlRow> findSet(SqlQuery query, Transaction transaction);

  /**
   * Execute the sql query returning a map of MapBean.
   * <p>
   * Generally you are able to use {@link SqlQuery#findMap()} rather than
   * explicitly calling this method. You could use this method if you wish to
   * explicitly control the transaction used for the query.
   * </p>
   * 
   * @param query
   *          the query to execute.
   * @param transaction
   *          the transaction to use (can be null).
   * @return the set of fetched MapBean.
   *
   * @see SqlQuery#findMap()
   */
  public Map<?, SqlRow> findMap(SqlQuery query, Transaction transaction);

  /**
   * Execute the sql query returning a single MapBean or null.
   * <p>
   * This will throw a PersistenceException if the query found more than one
   * result.
   * </p>
   * <p>
   * Generally you are able to use {@link SqlQuery#findUnique()} rather than
   * explicitly calling this method. You could use this method if you wish to
   * explicitly control the transaction used for the query.
   * </p>
   * 
   * @param query
   *          the query to execute.
   * @param transaction
   *          the transaction to use (can be null).
   * @return the fetched MapBean or null if none was found.
   *
   * @see SqlQuery#findUnique()
   */
  public SqlRow findUnique(SqlQuery query, Transaction transaction);

  /**
   * Either Insert or Update the bean depending on its state.
   * <p>
   * If there is no current transaction one will be created and committed for
   * you automatically.
   * </p>
   * <p>
   * Save can cascade along relationships. For this to happen you need to
   * specify a cascade of CascadeType.ALL or CascadeType.PERSIST on the
   * OneToMany, OneToOne or ManyToMany annotation.
   * </p>
   * <p>
   * In this example below the details property has a CascadeType.ALL set so
   * saving an order will also save all its details.
   * </p>
   *
   * <pre>{@code
   *   public class Order { ...
   *
   * 	   @OneToMany(cascade=CascadeType.ALL, mappedBy="order")
   * 	   @JoinColumn(name="order_id")
   * 	   List<OrderDetail> details;
   * 	   ...
   *   }
   * }</pre>
   *
   * <p>
   * When a save cascades via a OneToMany or ManyToMany Ebean will automatically
   * set the 'parent' object to the 'detail' object. In the example below in
   * saving the order and cascade saving the order details the 'parent' order
   * will be set against each order detail when it is saved.
   * </p>
   */
  public void save(Object bean) throws OptimisticLockException;

  /**
   * Save all the beans in the iterator.
   */
  public int save(Iterator<?> it) throws OptimisticLockException;

  /**
   * Save all the beans in the collection.
   */
  public int save(Collection<?> beans) throws OptimisticLockException;

  /**
   * Delete the bean.
   * <p>
   * If there is no current transaction one will be created and committed for
   * you automatically.
   * </p>
   */
  public void delete(Object bean) throws OptimisticLockException;

  /**
   * Delete all the beans from an Iterator.
   */
  public int delete(Iterator<?> it) throws OptimisticLockException;

  /**
   * Delete all the beans in the collection.
   */
  public int delete(Collection<?> c) throws OptimisticLockException;

  /**
   * Delete the bean given its type and id.
   */
  public int delete(Class<?> beanType, Object id);

  /**
   * Delete the bean given its type and id with an explicit transaction.
   */
  public int delete(Class<?> beanType, Object id, Transaction transaction);

  /**
   * Delete several beans given their type and id values.
   */
  public void delete(Class<?> beanType, Collection<?> ids);

  /**
   * Delete several beans given their type and id values with an explicit
   * transaction.
   */
  public void delete(Class<?> beanType, Collection<?> ids, Transaction transaction);

  /**
   * Execute a Sql Update Delete or Insert statement. This returns the number of
   * rows that where updated, deleted or inserted. If is executed in batch then
   * this returns -1. You can get the actual rowCount after commit() from
   * updateSql.getRowCount().
   * <p>
   * If you wish to execute a Sql Select natively then you should use the
   * FindByNativeSql object.
   * </p>
   * <p>
   * Note that the table modification information is automatically deduced and
   * you do not need to call the Ebean.externalModification() method when you
   * use this method.
   * </p>
   * <p>
   * Example:
   * </p>
   *
   * <pre>{@code
   *
   *   // example that uses 'named' parameters
   *   String s = "UPDATE f_topic set post_count = :count where id = :id"
   *
   *   SqlUpdate update = ebeanServer.createSqlUpdate(s);
   *
   *   update.setParameter("id", 1);
   *   update.setParameter("count", 50);
   *
   *   int modifiedCount = ebeanServer.execute(update);
   *
   *   String msg = "There where " + modifiedCount + "rows updated";
   *
   * }</pre>
   *
   * @param sqlUpdate
   *          the update sql potentially with bind values
   *
   * @return the number of rows updated or deleted. -1 if executed in batch.
   *
   * @see CallableSql
   */
  public int execute(SqlUpdate sqlUpdate);

  /**
   * Execute a ORM insert update or delete statement using the current
   * transaction.
   * <p>
   * This returns the number of rows that where inserted, updated or deleted.
   * </p>
   */
  public int execute(Update<?> update);

  /**
   * Execute a ORM insert update or delete statement with an explicit
   * transaction.
   */
  public int execute(Update<?> update, Transaction t);

  /**
   * For making calls to stored procedures.
   * <p>
   * Example:
   * </p>
   *
   * <pre>{@code
   *
   *   String sql = "{call sp_order_modify(?,?,?)}";
   *
   *   CallableSql cs = ebeanServer.createCallableSql(sql);
   *   cs.setParameter(1, 27);
   *   cs.setParameter(2, "SHIPPED");
   *   cs.registerOut(3, Types.INTEGER);
   *
   *   ebeanServer.execute(cs);
   *
   *   // read the out parameter
   *   Integer returnValue = (Integer) cs.getObject(3);
   *
   * }</pre>
   *
   * @see CallableSql
   * @see Ebean#execute(SqlUpdate)
   */
  public int execute(CallableSql callableSql);

  /**
   * Inform Ebean that tables have been modified externally. These could be the
   * result of from calling a stored procedure, other JDBC calls or external
   * programs including other frameworks.
   * <p>
   * If you use ebeanServer.execute(UpdateSql) then the table modification information
   * is automatically deduced and you do not need to call this method yourself.
   * </p>
   * <p>
   * This information is used to invalidate objects out of the cache and
   * potentially text indexes. This information is also automatically broadcast
   * across the cluster.
   * </p>
   * <p>
   * If there is a transaction then this information is placed into the current
   * transactions event information. When the transaction is committed this
   * information is registered (with the transaction manager). If this
   * transaction is rolled back then none of the transaction event information
   * registers including the information you put in via this method.
   * </p>
   * <p>
   * If there is NO current transaction when you call this method then this
   * information is registered immediately (with the transaction manager).
   * </p>
   *
   * @param tableName
   *          the name of the table that was modified
   * @param inserted
   *          true if rows where inserted into the table
   * @param updated
   *          true if rows on the table where updated
   * @param deleted
   *          true if rows on the table where deleted
   */
  public void externalModification(String tableName, boolean inserted, boolean updated, boolean deleted);

  /**
   * Find a entity bean with an explicit transaction.
   * 
   * @param <T>
   *          the type of entity bean to find
   * @param beanType
   *          the type of entity bean to find
   * @param uid
   *          the bean id value
   * @param transaction
   *          the transaction to use (can be null)
   */
  public <T> T find(Class<T> beanType, Object uid, Transaction transaction);

  /**
   * Insert or update a bean with an explicit transaction.
   */
  public void save(Object bean, Transaction transaction) throws OptimisticLockException;

  /**
   * Save all the beans in the iterator with an explicit transaction.
   */
  public int save(Iterator<?> it, Transaction transaction) throws OptimisticLockException;

  /**
   * Save all the beans in the collection with an explicit transaction.
   */
  public int save(Collection<?> beans, Transaction transaction) throws OptimisticLockException;

  /**
   * Marks the entity bean as dirty.
   * <p>
   * This is used so that when a bean that is otherwise unmodified is updated the version
   * property is updated.
   * <p>
   * An unmodified bean that is saved or updated is normally skipped and this marks the bean as
   * dirty so that it is not skipped.
   * 
   * <pre>{@code
   * 
   * Customer customer = ebeanServer.find(Customer, id);
   * 
   * // mark the bean as dirty so that a save() or update() will
   * // increment the version property
   * ebeanServer.markAsDirty(customer);
   * ebeanServer.save(customer);
   * 
   * }</pre>
   */
  public void markAsDirty(Object bean);
  
  /**
   * Saves the bean using an update. If you know you are updating a bean then it is preferrable to
   * use this update() method rather than save().
   * <p>
   * <b>Stateless updates:</b> Note that the bean does not have to be previously fetched to call
   * update().You can create a new instance and set some of its properties programmatically for via
   * JSON/XML marshalling etc. This is described as a 'stateless update'.
   * </p>
   * <p>
   * <b>Optimistic Locking: </b> Note that if the version property is not set when update() is
   * called then no optimistic locking is performed (internally ConcurrencyMode.NONE is used).
   * </p>
   * <p>
   * <b>{@link ServerConfig#setUpdatesDeleteMissingChildren(boolean)}: </b> When cascade saving to a
   * OneToMany or ManyToMany the updatesDeleteMissingChildren setting controls if any other children
   * that are in the database but are not in the collection are deleted.
   * </p>
   * <p>
   * <b>{@link ServerConfig#setUpdateChangesOnly(boolean)}: </b> The updateChangesOnly setting
   * controls if only the changed properties are included in the update or if all the loaded
   * properties are included instead.
   * </p>
   * 
   * <pre>{@code
   * 
   * // A 'stateless update' example
   * Customer customer = new Customer();
   * customer.setId(7);
   * customer.setName("ModifiedNameNoOCC");
   * ebeanServer.update(customer);
   * 
   * }</pre>
   * 
   * @see ServerConfig#setUpdatesDeleteMissingChildren(boolean)
   * @see ServerConfig#setUpdateChangesOnly(boolean)
   */
  public void update(Object bean) throws OptimisticLockException;

  /**
   * Update a bean additionally specifying a transaction.
   */
  public void update(Object bean, Transaction t) throws OptimisticLockException;

  /**
   * Update a bean additionally specifying a transaction and the deleteMissingChildren setting.
   * 
   * @param bean
   *          the bean to update
   * @param transaction
   *          the transaction to use (can be null).
   * @param deleteMissingChildren
   *          specify false if you do not want 'missing children' of a OneToMany
   *          or ManyToMany to be automatically deleted.

   */
  public void update(Object bean, Transaction transaction, boolean deleteMissingChildren) throws OptimisticLockException;

  /**
   * Update a collection of beans. If there is no current transaction one is created and used to
   * update all the beans in the collection.
   */
  public void update(Collection<?> beans) throws OptimisticLockException;

  /**
   * Update a collection of beans with an explicit transaction.
   */
  public void update(Collection<?> beans, Transaction transaction) throws OptimisticLockException;
  
  /**
   * Insert the bean.
   * <p>
   * Compared to save() this forces bean to perform an insert rather than trying to decide
   * based on the bean state. As such this is useful when you fetch beans from one database
   * and want to insert them into another database (and you want to explicitly insert them).
   * </p>
   */
  public void insert(Object bean);

  /**
   * Insert the bean with a transaction.
   */
  public void insert(Object bean, Transaction t);

  /**
   * Insert a collection of beans. If there is no current transaction one is created and used to
   * insert all the beans in the collection.
   */
  public void insert(Collection<?> beans);

  /**
   * Insert a collection of beans with an explicit transaction.
   */
  public void insert(Collection<?> beans, Transaction t);

  /**
   * Delete the associations (from the intersection table) of a ManyToMany given
   * the owner bean and the propertyName of the ManyToMany collection.
   * <p>
   * Typically these deletions occur automatically when persisting a ManyToMany
   * collection and this provides a way to invoke those deletions directly.
   * </p>
   * 
   * @return the number of associations deleted (from the intersection table).
   */
  public int deleteManyToManyAssociations(Object ownerBean, String propertyName);

  /**
   * Delete the associations (from the intersection table) of a ManyToMany given
   * the owner bean and the propertyName of the ManyToMany collection.
   * <p>
   * Additionally specify a transaction to use.
   * </p>
   * <p>
   * Typically these deletions occur automatically when persisting a ManyToMany
   * collection and this provides a way to invoke those deletions directly.
   * </p>
   * 
   * @return the number of associations deleted (from the intersection table).
   */
  public int deleteManyToManyAssociations(Object ownerBean, String propertyName, Transaction t);

  /**
   * Save the associations of a ManyToMany given the owner bean and the
   * propertyName of the ManyToMany collection.
   * <p>
   * Typically the saving of these associations (inserting into the intersection
   * table) occurs automatically when persisting a ManyToMany. This provides a
   * way to invoke those insertions directly.
   * </p>
   */
  public void saveManyToManyAssociations(Object ownerBean, String propertyName);

  /**
   * Save the associations of a ManyToMany given the owner bean and the
   * propertyName of the ManyToMany collection.
   * <p>
   * Typically the saving of these associations (inserting into the intersection
   * table) occurs automatically when persisting a ManyToMany. This provides a
   * way to invoke those insertions directly.
   * </p>
   */
  public void saveManyToManyAssociations(Object ownerBean, String propertyName, Transaction t);

  /**
   * Save the associated collection or bean given the property name.
   * <p>
   * This is similar to performing a save cascade on a specific property
   * manually.
   * </p>
   * <p>
   * Note that you can turn on/off cascading for a transaction via
   * {@link Transaction#setPersistCascade(boolean)}
   * </p>
   * 
   * @param ownerBean
   *          the bean instance holding the property we want to save
   * @param propertyName
   *          the property we want to save
   */
  public void saveAssociation(Object ownerBean, String propertyName);

  /**
   * Save the associated collection or bean given the property name with a
   * specific transaction.
   * <p>
   * This is similar to performing a save cascade on a specific property
   * manually.
   * </p>
   * <p>
   * Note that you can turn on/off cascading for a transaction via
   * {@link Transaction#setPersistCascade(boolean)}
   * </p>
   * 
   * @param ownerBean
   *          the bean instance holding the property we want to save
   * @param propertyName
   *          the property we want to save
   */
  public void saveAssociation(Object ownerBean, String propertyName, Transaction t);

  /**
   * Delete the bean with an explicit transaction.
   */
  public void delete(Object bean, Transaction t) throws OptimisticLockException;

  /**
   * Delete all the beans from an iterator.
   */
  public int delete(Iterator<?> it, Transaction t) throws OptimisticLockException;

  /**
   * Execute explicitly passing a transaction.
   */
  public int execute(SqlUpdate updSql, Transaction t);

  /**
   * Execute explicitly passing a transaction.
   */
  public int execute(CallableSql callableSql, Transaction t);

  /**
   * Execute a TxRunnable in a Transaction with an explicit scope.
   * <p>
   * The scope can control the transaction type, isolation and rollback
   * semantics.
   * </p>
   *
   * <pre>{@code
   *
   *   // set specific transactional scope settings
   *   TxScope scope = TxScope.requiresNew().setIsolation(TxIsolation.SERIALIZABLE);
   *
   *   ebeanServer.execute(scope, new TxRunnable() {
   * 	   public void run() {
   * 		   User u1 = Ebean.find(User.class, 1);
   * 		   ...
   * 	   }
   *   });
   *
   * }</pre>
   */
  public void execute(TxScope scope, TxRunnable r);

  /**
   * Execute a TxRunnable in a Transaction with the default scope.
   * <p>
   * The default scope runs with REQUIRED and by default will rollback on any
   * exception (checked or runtime).
   * </p>
   *
   * <pre>{@code
   *
   *    ebeanServer.execute(new TxRunnable() {
   *      public void run() {
   *        User u1 = ebeanServer.find(User.class, 1);
   *        User u2 = ebeanServer.find(User.class, 2);
   *
   *        u1.setName("u1 mod");
   *        u2.setName("u2 mod");
   *
   *        ebeanServer.save(u1);
   *        ebeanServer.save(u2);
   *      }
   *    });
   *
   * }</pre>
   */
  public void execute(TxRunnable r);

  /**
   * Execute a TxCallable in a Transaction with an explicit scope.
   * <p>
   * The scope can control the transaction type, isolation and rollback
   * semantics.
   * </p>
   *
   * <pre>{@code
   *
   *   // set specific transactional scope settings
   *   TxScope scope = TxScope.requiresNew().setIsolation(TxIsolation.SERIALIZABLE);
   *
   *   ebeanServer.execute(scope, new TxCallable<String>() {
   * 	   public String call() {
   * 		   User u1 = ebeanServer.find(User.class, 1);
   * 		   ...
   * 		   return u1.getEmail();
   * 	   }
   *   });
   *
   * }</pre>
   */
  public <T> T execute(TxScope scope, TxCallable<T> c);

  /**
   * Execute a TxCallable in a Transaction with the default scope.
   * <p>
   * The default scope runs with REQUIRED and by default will rollback on any
   * exception (checked or runtime).
   * </p>
   * <p>
   * This is basically the same as TxRunnable except that it returns an Object
   * (and you specify the return type via generics).
   * </p>
   *
   * <pre>{@code
   *
   *   ebeanServer.execute(new TxCallable<String>() {
   *     public String call() {
   *       User u1 = ebeanServer.find(User.class, 1);
   *       User u2 = ebeanServer.find(User.class, 2);
   *
   *       u1.setName("u1 mod");
   *       u2.setName("u2 mod");
   *
   *       ebeanServer.save(u1);
   *       ebeanServer.save(u2);
   *
   *       return u1.getEmail();
   *     }
   *   });
   *
   * }</pre>
   */
  public <T> T execute(TxCallable<T> c);

  /**
   * Return the manager of the server cache ("L2" cache).
   * 
   */
  public ServerCacheManager getServerCacheManager();

  /**
   * Return the BackgroundExecutor service for asynchronous processing of
   * queries.
   */
  public BackgroundExecutor getBackgroundExecutor();

  /**
   * Run the cache warming queries on all bean types that have one defined.
   * <p>
   * A cache warming query can be defined via {@link CacheStrategy}.
   * </p>
   */
  public void runCacheWarming();

  /**
   * Run the cache warming query for a specific bean type.
   * <p>
   * A cache warming query can be defined via {@link CacheStrategy}.
   * </p>
   */
  public void runCacheWarming(Class<?> beanType);

  /**
   * Return the JsonContext for reading/writing JSON.
   * @deprecated Please use #json instead.
   */
  public JsonContext createJsonContext();

  /**
   * Return the JsonContext for reading/writing JSON.
   * <p>
   * This instance is safe to be used concurrently by multiple threads and this
   * method is cheap to call.
   * </p>
   *
   * <h3>Simple example:</h3>
   * <pre>{@code
   *
   *     JsonContext json = ebeanServer.json();
   *     String jsonOutput = json.toJson(list);
   *     System.out.println(jsonOutput);
   *
   * }</pre>
   *
   * <h3>Using PathProperties:</h3>
   * <pre>{@code
   *
   *     // specify just the properties we want
   *     PathProperties paths = PathProperties.parse("name, status, anniversary");
   *
   *     List<Customer> customers =
   *       ebeanServer.find(Customer.class)
   *         // apply those paths to the query (only fetch what we need)
   *         .apply(paths)
   *         .where().ilike("name", "rob%")
   *         .findList();
   *
   *     // ... get the json
   *     JsonContext jsonContext = ebeanServer.json();
   *     String json = jsonContext.toJson(customers, paths);
   *
   * }</pre>
   *
   * @see com.avaje.ebean.text.PathProperties
   * @see Query#apply(com.avaje.ebean.text.PathProperties)
   */
  public JsonContext json();

}

package io.ebean;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import io.ebean.annotation.Platform;
import io.ebean.annotation.TxIsolation;
import io.ebean.cache.ServerCacheManager;
import io.ebean.config.DatabaseConfig;
import io.ebean.meta.MetaInfoManager;
import io.ebean.plugin.Property;
import io.ebean.plugin.SpiServer;
import io.ebean.text.json.JsonContext;

import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import javax.sql.DataSource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Provides the API for fetching and saving beans to a particular database.
 *
 * <h5>Registration with the DB singleton</h5>
 * <p>
 * When a Database instance is created it can be registered with the DB
 * singleton (see {@link DatabaseConfig#setRegister(boolean)}). The DB
 * singleton is essentially a map of Database's that have been registered
 * with it.
 * <p>
 * The Database can then be retrieved later via {@link DB#byName(String)}.
 *
 * <h5>The 'default' Database</h5>
 * <p>
 * One Database can be designated as the 'default' or 'primary' Database
 * (see {@link DatabaseConfig#setDefaultServer(boolean)}). Many methods on DB
 * such as {@link DB#find(Class)} etc are actually just a convenient way to
 * call methods on the 'default/primary' Database.
 *
 * <h5>Constructing a Database</h5>
 * <p>
 * Databases are constructed by the DatabaseFactory. They can be created
 * programmatically via {@link DatabaseFactory#create(DatabaseBuilder)} or they
 * can be automatically constructed on demand using configuration information in
 * the application.properties file.
 *
 * <h5>Example: Get a Database</h5>
 * <pre>{@code
 *
 *   // Get access to the Human Resources Database
 *   Database hrDatabase = DB.byName("hr");
 *
 *
 *   // fetch contact 3 from the HR database
 *   Contact contact = hrDatabase.find(Contact.class, new Integer(3));
 *
 *   contact.setStatus("INACTIVE"); ...
 *
 *   // save the contact back to the HR database
 *   hrDatabase.save(contact);
 *
 * }</pre>
 *
 * <h5>Database vs DB API</h5>
 * <p>
 * Database provides additional API compared with DB. For example, it
 * provides more control over the use of Transactions that is not available in
 * the DB API.
 *
 * <p>
 * <em>External Transactions:</em> If you wanted to use transactions created
 * externally to Ebean then Database provides additional methods where you
 * can explicitly pass a transaction (that can be created externally).
 *
 * <p>
 * <em>Bypass ThreadLocal Mechanism:</em> If you want to bypass the built-in
 * ThreadLocal transaction management you can use the createTransaction()
 * method. Example: a single thread requires more than one transaction.
 *
 * @see DB
 * @see DatabaseFactory
 * @see DatabaseConfig
 */
@NullMarked
public interface Database {

  /**
   * Return a new database builder.
   * <pre>{@code
   *
   *   // build the 'default' database using configuration
   *   // from application.properties / application.yaml
   *
   *   Database db = Database.builder()
   *     .loadFromProperties()
   *     .build();
   *
   * }</pre>
   */
  static DatabaseBuilder builder() {
    return new DatabaseConfig();
  }

  /**
   * Shutdown the Database instance.
   */
  void shutdown();

  /**
   * Shutdown the Database instance programmatically.
   * <p>
   * This method is not normally required. Ebean registers a shutdown hook and shuts down cleanly.
   * <p>
   * If the under underlying DataSource is the Ebean implementation then you
   * also have the option of shutting down the DataSource and de-registering the
   * JDBC driver.
   *
   * @param shutdownDataSource if true then shutdown the underlying DataSource if it is the Ebean
   *                           DataSource implementation.
   * @param deregisterDriver   if true then deregister the JDBC driver if it is the Ebean
   *                           DataSource implementation.
   */
  void shutdown(boolean shutdownDataSource, boolean deregisterDriver);

  /**
   * Return AutoTune which is used to control the AutoTune service at runtime.
   */
  AutoTune autoTune();

  /**
   * Return the associated DataSource for this Database instance.
   */
  DataSource dataSource();

  /**
   * Return the associated read only DataSource for this Database instance (can be null).
   */
  DataSource readOnlyDataSource();

  /**
   * Return the name. This is used with {@link DB#byName(String)} to get a
   * Database that was registered with the DB singleton.
   */
  String name();

  /**
   * Return the ExpressionFactory for this database.
   */
  ExpressionFactory expressionFactory();

  /**
   * Return the MetaInfoManager which is used to get meta data from the Database
   * such as query execution statistics.
   */
  MetaInfoManager metaInfo();

  /**
   * Return the platform used for this database instance.
   * <p>
   * Note many platforms have multiple specific platform types so often we want to
   * get the base platform via {@link Platform#base()}.
   *
   * <pre>{@code
   *
   *  Platform platform = database.platform().base();
   *  if (platform == Platform.MYSQL) {
   *    // do MySql specific function
   *  }
   *
   * }</pre>
   *
   * @return platform for this database instance
   */
  Platform platform();

  /**
   * Return the extended API intended for use by plugins.
   */
  SpiServer pluginApi();

  /**
   * Return the BeanState for a given entity bean.
   * <p>
   * This will throw an IllegalArgumentException if the bean is not an enhanced entity bean.
   */
  BeanState beanState(Object bean);

  /**
   * Return the value of the Id property for a given bean.
   */
  Object beanId(Object bean);

  /**
   * Set the Id value onto the bean converting the type of the id value if necessary.
   * <p>
   * For example, if the id value passed in is a String but ought to be a Long or UUID etc
   * then it will automatically be converted.
   *
   * @param bean The entity bean to set the id value on.
   * @param id   The id value to set.
   */
  Object beanId(Object bean, Object id);

  /**
   * Return a map of the differences between two objects of the same type.
   * <p>
   * When null is passed in for b, then the 'OldValues' of a is used for the
   * difference comparison.
   */
  Map<String, ValuePair> diff(Object newBean, Object oldBean);

  /**
   * Create a new instance of T that is an EntityBean.
   * <p>
   * Useful if you use BeanPostConstructListeners or &#64;PostConstruct Annotations.
   * In this case you should not use "new Bean...()". Making all bean constructors protected
   * could be a good idea here.
   */
  <T> T createEntityBean(Class<T> type);


  /**
   * Create an Update query to perform a bulk update.
   * <p>
   * <pre>{@code
   *
   *  int rows = database
   *      .update(Customer.class)
   *      .set("status", Customer.Status.ACTIVE)
   *      .set("updtime", new Timestamp(System.currentTimeMillis()))
   *      .where()
   *        .gt("id", 1000)
   *        .update();
   *
   * }</pre>
   *
   * @param beanType The type of entity bean to update
   * @param <T>      The type of entity bean
   * @return The update query to use
   */
  <T> UpdateQuery<T> update(Class<T> beanType);

  /**
   * Create a named query.
   * <p>
   * For RawSql the named query is expected to be in ebean.xml.
   *
   * @param beanType   The type of entity bean
   * @param namedQuery The name of the query
   * @param <T>        The type of entity bean
   * @return The query
   */
  <T> Query<T> createNamedQuery(Class<T> beanType, String namedQuery);

  /**
   * Create a query for an entity bean and synonym for {@link #find(Class)}.
   *
   * @see #find(Class)
   */
  <T> Query<T> createQuery(Class<T> beanType);

  /**
   * Parse the Ebean query language statement returning the query which can then
   * be modified (add expressions, change order by clause, change maxRows, change
   * fetch and select paths etc).
   * <p>
   * <h3>Example</h3>
   * <pre>{@code
   *
   *   // Find order additionally fetching the customer, details and details.product name.
   *
   *   String ormQuery = "fetch customer fetch details fetch details.product (name) where id = :orderId ";
   *
   *   Query<Order> query = DB.createQuery(Order.class, ormQuery);
   *   query.setParameter("orderId", 2);
   *
   *   Order order = query.findOne();
   *
   *   // This is the same as:
   *
   *   Order order = DB.find(Order.class)
   *     .fetch("customer")
   *     .fetch("details")
   *     .fetch("detail.product", "name")
   *     .setId(2)
   *     .findOne();
   *
   * }</pre>
   *
   * @param beanType The type of bean to fetch
   * @param ormQuery The Ebean ORM query
   * @param <T>      The type of the entity bean
   * @return The query with expressions defined as per the parsed query statement
   */
  <T> Query<T> createQuery(Class<T> beanType, String ormQuery);

  /**
   * Create a query for a type of entity bean.
   * <p>
   * You can use the methods on the Query object to specify fetch paths,
   * predicates, order by, limits etc.
   * <p>
   * You then use findList(), findSet(), findMap() and findOne() to execute
   * the query and return the collection or bean.
   * <p>
   * Note that a query executed by {@link Query#findList()}
   * {@link Query#findSet()} etc will execute against the same Database from
   * which is was created.
   *
   * <pre>{@code
   *
   *   // Find order 2 specifying explicitly the parts of the object graph to
   *   // eagerly fetch. In this case eagerly fetch the associated customer,
   *   // details and details.product.name
   *
   *   Order order = database.find(Order.class)
   *     .fetch("customer")
   *     .fetch("details")
   *     .fetch("detail.product", "name")
   *     .setId(2)
   *     .findOne();
   *
   *   // find some new orders ... with firstRow/maxRows
   *   List<Order> orders =
   *     database.find(Order.class)
   *       .where().eq("status", Order.Status.NEW)
   *       .setFirstRow(20)
   *       .setMaxRows(10)
   *       .findList();
   *
   * }</pre>
   */
  <T> Query<T> find(Class<T> beanType);

  /**
   * Create a query using native SQL.
   * <p>
   * The native SQL can contain named parameters or positioned parameters.
   *
   * <pre>{@code
   *
   *   String sql = "select c.id, c.name from customer c where c.name like ? order by c.name";
   *
   *   Query<Customer> query = database.findNative(Customer.class, sql);
   *   query.setParameter(1, "Rob%");
   *
   *   List<Customer> customers = query.findList();
   *
   * }</pre>
   *
   * @param beanType  The type of entity bean to fetch
   * @param nativeSql The SQL that can contain named or positioned parameters
   * @return The query to set parameters and execute
   */
  <T> Query<T> findNative(Class<T> beanType, String nativeSql);

  /**
   * Return the next unique identity value for a given bean type.
   * <p>
   * This will only work when a IdGenerator is on the bean such as for beans
   * that use a DB sequence or UUID.
   * <p>
   * For DB's supporting getGeneratedKeys and sequences such as Oracle10 you do
   * not need to use this method generally. It is made available for more
   * complex cases where it is useful to get an ID prior to some processing.
   */
  Object nextId(Class<?> beanType);

  /**
   * Create a filter for sorting and filtering lists of entities locally without
   * going back to the database.
   * <p>
   * This produces and returns a new list with the sort and filters applied.
   * <p>
   * Refer to {@link Filter} for an example of its use.
   */
  <T> Filter<T> filter(Class<T> beanType);

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
   * <p>
   * Note that the sorting uses a Comparator and Collections.sort(); and does
   * not invoke a DB query.
   *
   * <pre>{@code
   *
   *   // find orders and their customers
   *   List<Order> list = database.find(Order.class)
   *     .fetch("customer")
   *     .order("id")
   *     .findList();
   *
   *   // sort by customer name ascending, then by order shipDate
   *   // ... then by the order status descending
   *   database.sort(list, "customer.name, shipDate, status desc");
   *
   *   // sort by customer name descending (with nulls low)
   *   // ... then by the order id
   *   database.sort(list, "customer.name desc nullsLow, id");
   *
   * }</pre>
   *
   * @param list         the list of entity beans
   * @param sortByClause the properties to sort the list by
   */
  <T> void sort(List<T> list, String sortByClause);

  /**
   * Create an orm update where you will supply the insert/update or delete
   * statement (rather than using a named one that is already defined using the
   * &#064;NamedUpdates annotation).
   * <p>
   * The orm update differs from the sql update in that it you can use the bean
   * name and bean property names rather than table and column names.
   * <p>
   * An example:
   *
   * <pre>{@code
   *
   *   // The bean name and properties - "topic","postCount" and "id"
   *
   *   // will be converted into their associated table and column names
   *   String updStatement = "update topic set postCount = :pc where id = :id";
   *
   *   Update<Topic> update = database.createUpdate(Topic.class, updStatement);
   *
   *   update.set("pc", 9);
   *   update.set("id", 3);
   *
   *   int rows = update.execute();
   *   System.out.println("rows updated:" + rows);
   *
   * }</pre>
   */
  <T> Update<T> createUpdate(Class<T> beanType, String ormUpdate);

  /**
   * Create a Query for DTO beans.
   * <p>
   * DTO beans are just normal bean like classes with public constructor(s) and setters.
   * They do not need to be registered with DB before use.
   *
   * @param dtoType The type of the DTO bean the rows will be mapped into.
   * @param sql     The SQL query to execute.
   * @param <T>     The type of the DTO bean.
   */
  <T> DtoQuery<T> findDto(Class<T> dtoType, String sql);

  /**
   * Create a named Query for DTO beans.
   * <p>
   * DTO beans are just normal bean like classes with public constructor(s) and setters.
   * They do not need to be registered with DB before use.
   *
   * @param dtoType    The type of the DTO bean the rows will be mapped into.
   * @param namedQuery The name of the query
   * @param <T>        The type of the DTO bean.
   */
  <T> DtoQuery<T> createNamedDtoQuery(Class<T> dtoType, String namedQuery);

  /**
   * Look to execute a native sql query that does not return beans but instead
   * returns SqlRow or direct access to ResultSet.
   *
   * <p>
   * Refer to {@link DtoQuery} for native sql queries returning DTO beans.
   * <p>
   * Refer to {@link #findNative(Class, String)} for native sql queries returning entity beans.
   */
  SqlQuery sqlQuery(String sql);

  /**
   * Look to execute a native sql insert update or delete statement.
   * <p>
   * Use this to execute a Insert Update or Delete statement. The statement will
   * be native to the database and contain database table and column names.
   *
   * <p>
   * See {@link SqlUpdate} for example usage.
   *
   * @return The SqlUpdate instance to set parameters and execute
   */
  SqlUpdate sqlUpdate(String sql);

  /**
   * Create a CallableSql to execute a given stored procedure.
   */
  CallableSql createCallableSql(String callableSql);

  /**
   * Register a TransactionCallback on the currently active transaction.
   * <p>
   * If there is no currently active transaction then a PersistenceException is thrown.
   *
   * @param transactionCallback The transaction callback to be registered with the current transaction.
   * @throws PersistenceException If there is no currently active transaction
   */
  void register(TransactionCallback transactionCallback) throws PersistenceException;

  /**
   * Create a new transaction that is not held in TransactionThreadLocal.
   * <p>
   * You will want to do this if you want multiple Transactions in a single
   * thread or generally use transactions outside of the TransactionThreadLocal
   * management.
   */
  Transaction createTransaction();

  /**
   * Create a new transaction additionally specifying the isolation level.
   * <p>
   * Note that this transaction is NOT stored in a thread local.
   */
  Transaction createTransaction(TxIsolation isolation);

  /**
   * Start a transaction with 'REQUIRED' semantics.
   * <p>
   * With REQUIRED semantics if an active transaction already exists that transaction will be used.
   * <p>
   * The transaction is stored in a ThreadLocal variable and typically you only
   * need to use the returned Transaction <em>IF</em> you wish to do things like
   * use batch mode, change the transaction isolation level, use savepoints or
   * log comments to the transaction log.
   * <p>
   * Example of using a transaction to span multiple calls to find(), save() etc.
   *
   * <h3>Using try with resources</h3>
   * <pre>{@code
   *
   *   // start a transaction (stored in a ThreadLocal)
   *   try (Transaction txn = database.beginTransaction()) {
   *
   *     Order order = database.find(Order.class, 10);
   *     ...
   *     database.save(order);
   *
   *     txn.commit();
   *   }
   *
   * }</pre>
   *
   * <h3>Using try finally block</h3>
   * <pre>{@code
   *
   *   // start a transaction (stored in a ThreadLocal)
   *   Transaction txn = database.beginTransaction();
   *   try {
   * 	   Order order = database.find(Order.class,10);
   *
   *     database.save(order);
   *     txn.commit();
   *
   *   } finally {
   *     txn.end();
   *   }
   * }</pre>
   *
   * <h3>Transaction options</h3>
   * <pre>{@code
   *
   *   try (Transaction txn = database.beginTransaction()) {
   *
   *     // explicitly turn on/off JDBC batch use
   *     txn.setBatchMode(true);
   *     txn.setBatchSize(50);
   *
   *     // control flushing when mixing save and queries
   *     txn.setBatchFlushOnQuery(false);
   *
   *     // turn off persist cascade if needed
   *     txn.setPersistCascade(false);
   *
   *     // for large batch insert processing when we do not
   *     // ... need the generatedKeys, don't get them
   *     txn.setBatchGetGeneratedKeys(false);
   *
   *     // explicitly flush the JDBC batch buffer
   *     txn.flush();
   *
   *     ...
   *
   *     txn.commit();
   *   }
   *
   * }</pre>
   * <p>
   * If you want to externalise the transaction management then you use
   * createTransaction() and pass the transaction around to the various methods on
   * Database yourself.
   */
  Transaction beginTransaction();

  /**
   * Start a transaction additionally specifying the isolation level.
   */
  Transaction beginTransaction(TxIsolation isolation);

  /**
   * Start a transaction typically specifying REQUIRES_NEW or REQUIRED semantics.
   * <p>
   * Note that this provides an try finally alternative to using {@link #executeCall(TxScope, Callable)} or
   * {@link #execute(TxScope, Runnable)}.
   *
   * <h3>REQUIRES_NEW example:</h3>
   * <pre>{@code
   * // Start a new transaction. If there is a current transaction
   * // suspend it until this transaction ends
   * try (Transaction txn = database.beginTransaction(TxScope.requiresNew())) {
   *
   *   ...
   *
   *   // commit the transaction
   *   txn.commit();
   *
   *   // At end this transaction will:
   *   //  A) will rollback transaction if it has not been committed
   *   //  B) will restore a previously suspended transaction
   * }
   *
   * }</pre>
   * <p>
   * <h3>REQUIRED example:</h3>
   * <pre>{@code
   *
   * // start a new transaction if there is not a current transaction
   * try (Transaction txn = database.beginTransaction(TxScope.required())) {
   *
   *   ...
   *
   *   // commit the transaction if it was created or
   *   // do nothing if there was already a current transaction
   *   txn.commit();
   * }
   *
   * }</pre>
   */
  Transaction beginTransaction(TxScope scope);

  /**
   * Returns the current transaction or null if there is no current transaction in scope.
   */
  Transaction currentTransaction();

  /**
   * Flush the JDBC batch on the current transaction.
   * <p>
   * This only is useful when JDBC batch is used. Flush occurs automatically when the
   * transaction commits or batch size is reached. This manually flushes the JDBC batch
   * buffer.
   * <p>
   * This is the same as <code>currentTransaction().flush()</code>.
   */
  void flush();

  /**
   * Refresh the values of a bean.
   * <p>
   * Note that this resets OneToMany and ManyToMany properties so that if they
   * are accessed a lazy load will refresh the many property.
   */
  void refresh(Object bean);

  /**
   * Refresh a many property of an entity bean.
   *
   * @param bean         the entity bean containing the 'many' property
   * @param propertyName the 'many' property to be refreshed
   */
  void refreshMany(Object bean, String propertyName);

  /**
   * Find a bean using its unique id.
   * <p>
   * <pre>{@code
   *   // Fetch order 1
   *   Order order = database.find(Order.class, 1);
   * }</pre>
   *
   * <p>
   * If you want more control over the query then you can use createQuery() and
   * Query.findOne();
   *
   * <pre>{@code
   *   // ... additionally fetching customer, customer shipping address,
   *   // order details, and the product associated with each order detail.
   *   // note: only product id and name is fetch (its a "partial object").
   *   // note: all other objects use "*" and have all their properties fetched.
   *
   *   Query<Order> query = database.find(Order.class)
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
   *   Order order = query.findOne();
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
   * @param beanType the type of entity bean to fetch
   * @param id       the id value
   */
  @Nullable
  <T> T find(Class<T> beanType, Object id);

  /**
   * Get a reference object.
   * <p>
   * This will not perform a query against the database unless some property other
   * that the id property is accessed.
   * <p>
   * It is most commonly used to set a 'foreign key' on another bean like:
   *
   * <pre>{@code
   *
   *   Product product = database.getReference(Product.class, 1);
   *
   *   OrderDetail orderDetail = new OrderDetail();
   *   // set the product 'foreign key'
   *   orderDetail.setProduct(product);
   *   orderDetail.setQuantity(42);
   *   ...
   *
   *   database.save(orderDetail);
   *
   *
   * }</pre>
   * <p>
   * <h3>Lazy loading characteristics</h3>
   * <pre>{@code
   *
   *   Product product = database.getReference(Product.class, 1);
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
   * @param beanType the type of entity bean
   * @param id       the id value
   */
  <T> T reference(Class<T> beanType, Object id);

  /**
   * Return the extended API for Database.
   * <p>
   * The extended API has the options for executing queries that take an explicit
   * transaction as an argument.
   * <p>
   * Typically, we only need to use the extended API when we do NOT want to use the
   * usual ThreadLocal based mechanism to obtain the current transaction but instead
   * supply the transaction explicitly.
   */
  ExtendedServer extended();

  /**
   * Either Insert or Update the bean depending on its state.
   * <p>
   * If there is no current transaction one will be created and committed for
   * you automatically.
   * <p>
   * Save can cascade along relationships. For this to happen you need to
   * specify a cascade of CascadeType.ALL or CascadeType.PERSIST on the
   * OneToMany, OneToOne or ManyToMany annotation.
   * <p>
   * In this example below the details property has a CascadeType.ALL set so
   * saving an order will also save all its details.
   *
   * <pre>{@code
   *   public class Order { ...
   *
   *     @OneToMany(cascade=CascadeType.ALL, mappedBy="order")
   *     List<OrderDetail> details;
   *     ...
   *   }
   * }</pre>
   * <p>
   * When a save cascades via a OneToMany or ManyToMany Ebean will automatically
   * set the 'parent' object to the 'detail' object. In the example below in
   * saving the order and cascade saving the order details the 'parent' order
   * will be set against each order detail when it is saved.
   */
  void save(Object bean) throws OptimisticLockException;

  /**
   * Save all the beans in the collection.
   */
  int saveAll(Collection<?> beans) throws OptimisticLockException;

  /**
   * Save all the beans.
   */
  int saveAll(Object... beans) throws OptimisticLockException;

  /**
   * Delete the bean.
   * <p>
   * This will return true if the bean was deleted successfully or JDBC batch is being used.
   * <p>
   * If there is no current transaction one will be created and committed for
   * you automatically.
   * <p>
   * If the Bean does not have a version property (or loaded version property) and
   * the bean does not exist then this returns false indicating that nothing was
   * deleted. Note that, if JDBC batch mode is used then this always returns true.
   */
  boolean delete(Object bean) throws OptimisticLockException;

  /**
   * Delete the bean with an explicit transaction.
   * <p>
   * This will return true if the bean was deleted successfully or JDBC batch is being used.
   * <p>
   * If the Bean does not have a version property (or loaded version property) and
   * the bean does not exist then this returns false indicating that nothing was
   * deleted. However, if JDBC batch mode is used then this always returns true.
   */
  boolean delete(Object bean, Transaction transaction) throws OptimisticLockException;

  /**
   * Delete a bean permanently without soft delete.
   */
  boolean deletePermanent(Object bean) throws OptimisticLockException;

  /**
   * Delete a bean permanently without soft delete using an explicit transaction.
   */
  boolean deletePermanent(Object bean, Transaction transaction) throws OptimisticLockException;

  /**
   * Delete all the beans in the collection permanently without soft delete.
   */
  int deleteAllPermanent(Collection<?> beans) throws OptimisticLockException;

  /**
   * Delete all the beans in the collection permanently without soft delete using an explicit transaction.
   */
  int deleteAllPermanent(Collection<?> beans, Transaction transaction) throws OptimisticLockException;

  /**
   * Delete the bean given its type and id.
   */
  int delete(Class<?> beanType, Object id);

  /**
   * Delete the bean given its type and id with an explicit transaction.
   */
  int delete(Class<?> beanType, Object id, Transaction transaction);

  /**
   * Delete permanent given the bean type and id.
   */
  int deletePermanent(Class<?> beanType, Object id);

  /**
   * Delete permanent given the bean type and id with an explicit transaction.
   */
  int deletePermanent(Class<?> beanType, Object id, Transaction transaction);

  /**
   * Delete all the beans in the collection.
   */
  int deleteAll(Collection<?> beans) throws OptimisticLockException;

  /**
   * Delete all the beans in the collection using an explicit transaction.
   */
  int deleteAll(Collection<?> beans, Transaction transaction) throws OptimisticLockException;

  /**
   * Delete several beans given their type and id values.
   */
  int deleteAll(Class<?> beanType, Collection<?> ids);

  /**
   * Delete several beans given their type and id values with an explicit transaction.
   */
  int deleteAll(Class<?> beanType, Collection<?> ids, Transaction transaction);

  /**
   * Delete permanent for several beans given their type and id values.
   */
  int deleteAllPermanent(Class<?> beanType, Collection<?> ids);

  /**
   * Delete permanent for several beans given their type and id values with an explicit transaction.
   */
  int deleteAllPermanent(Class<?> beanType, Collection<?> ids, Transaction transaction);

  /**
   * Execute a Sql Update Delete or Insert statement. This returns the number of
   * rows that where updated, deleted or inserted. If is executed in batch then
   * this returns -1. You can get the actual rowCount after commit() from
   * updateSql.getRowCount().
   * <p>
   * If you wish to execute a Sql Select natively then you should use the
   * SqlQuery object or DtoQuery.
   * <p>
   * Note that the table modification information is automatically deduced and
   * you do not need to call the DB.externalModification() method when you
   * use this method.
   * <p>
   * Example:
   *
   * <pre>{@code
   *
   *   // example that uses 'named' parameters
   *   String s = "UPDATE f_topic set post_count = :count where id = :id"
   *
   *   SqlUpdate update = database.createSqlUpdate(s);
   *
   *   update.setParameter("id", 1);
   *   update.setParameter("count", 50);
   *
   *   int modifiedCount = database.execute(update);
   *
   *   String msg = "There where " + modifiedCount + "rows updated";
   *
   * }</pre>
   *
   * @param sqlUpdate the update sql potentially with bind values
   * @return the number of rows updated or deleted. -1 if executed in batch.
   * @see CallableSql
   */
  int execute(SqlUpdate sqlUpdate);

  /**
   * Execute a ORM insert update or delete statement using the current
   * transaction.
   * <p>
   * This returns the number of rows that where inserted, updated or deleted.
   */
  int execute(Update<?> update);

  /**
   * Execute a ORM insert update or delete statement with an explicit
   * transaction.
   */
  int execute(Update<?> update, Transaction transaction);

  /**
   * For making calls to stored procedures.
   * <p>
   * Example:
   *
   * <pre>{@code
   *
   *   String sql = "{call sp_order_modify(?,?,?)}";
   *
   *   CallableSql cs = database.createCallableSql(sql);
   *   cs.setParameter(1, 27);
   *   cs.setParameter(2, "SHIPPED");
   *   cs.registerOut(3, Types.INTEGER);
   *   cs.execute();
   *
   *   // read the out parameter
   *   Integer returnValue = (Integer) cs.getObject(3);
   *
   * }</pre>
   */
  int execute(CallableSql callableSql);

  /**
   * Inform Ebean that tables have been modified externally. These could be the
   * result of from calling a stored procedure, other JDBC calls or external
   * programs including other frameworks.
   * <p>
   * If you use database.execute(UpdateSql) then the table modification information
   * is automatically deduced and you do not need to call this method yourself.
   * <p>
   * This information is used to invalidate objects out of the cache and
   * potentially text indexes. This information is also automatically broadcast
   * across the cluster.
   * <p>
   * If there is a transaction then this information is placed into the current
   * transactions event information. When the transaction is committed this
   * information is registered (with the transaction manager). If this
   * transaction is rolled back then none of the transaction event information
   * registers including the information you put in via this method.
   * <p>
   * If there is NO current transaction when you call this method then this
   * information is registered immediately (with the transaction manager).
   *
   * @param tableName the name of the table that was modified
   * @param inserted  true if rows where inserted into the table
   * @param updated   true if rows on the table where updated
   * @param deleted   true if rows on the table where deleted
   */
  void externalModification(String tableName, boolean inserted, boolean updated, boolean deleted);

  /**
   * Find a entity bean with an explicit transaction.
   *
   * @param <T>         the type of entity bean to find
   * @param beanType    the type of entity bean to find
   * @param id          the bean id value
   * @param transaction the transaction to use (can be null)
   */
  @Nullable
  <T> T find(Class<T> beanType, Object id, Transaction transaction);

  /**
   * Insert or update a bean with an explicit transaction.
   */
  void save(Object bean, Transaction transaction) throws OptimisticLockException;

  /**
   * Save all the beans in the collection with an explicit transaction.
   */
  int saveAll(Collection<?> beans, Transaction transaction) throws OptimisticLockException;

  /**
   * This method checks the uniqueness of a bean. I.e. if the save will work. It will return the
   * properties that violates an unique / primary key. This may be done in an UI save action to
   * validate if the user has entered correct values.
   * <p>
   * Note: This method queries the DB for uniqueness of all indices, so do not use it in a batch update.
   * <p>
   * Note: This checks only the root bean!
   * <p>
   * <pre>{@code
   *   // there is a unique constraint on title
   *
   *   Document doc = new Document();
   *   doc.setTitle("One flew over the cuckoo's nest");
   *   doc.setBody("clashes with doc1");
   *
   *   Set<Property> properties = DB.checkUniqueness(doc);
   *
   *   if (properties.isEmpty()) {
   *     // it is unique ... carry on
   *
   *   } else {
   *     // build a user friendly message
   *     // to return message back to user
   *
   *     String uniqueProperties = properties.toString();
   *
   *     StringBuilder msg = new StringBuilder();
   *
   *     properties.forEach((it)-> {
   *       Object propertyValue = it.getVal(doc);
   *       String propertyName = it.getName();
   *       msg.append(" property["+propertyName+"] value["+propertyValue+"]");
   *     });
   *
   *     // uniqueProperties > [title]
   *     //       custom msg > property[title] value[One flew over the cuckoo's nest]
   *
   *   }
   *
   * }</pre>
   *
   * @param bean The entity bean to check uniqueness on
   * @return a set of Properties if constraint validation was detected or empty list.
   */
  Set<Property> checkUniqueness(Object bean);

  /**
   * Same as {@link #checkUniqueness(Object)}. but with given transaction.
   */
  Set<Property> checkUniqueness(Object bean, Transaction transaction);

  /**
   * Marks the entity bean as dirty.
   * <p>
   * This is used so that when a bean that is otherwise unmodified is updated the version
   * property is updated.
   * <p>
   * An unmodified bean that is saved or updated is normally skipped and this marks the bean as
   * dirty so that it is not skipped.
   * <p>
   * <pre>{@code
   *   Customer customer = database.find(Customer, id);
   *
   *   // mark the bean as dirty so that a save() or update() will
   *   // increment the version property
   *   database.markAsDirty(customer);
   *   database.save(customer);
   *
   * }</pre>
   */
  void markAsDirty(Object bean);

  /**
   * Saves the bean using an update. If you know you are updating a bean then it is preferable to
   * use this update() method rather than save().
   * <p>
   * <b>Stateless updates:</b> Note that the bean does not have to be previously fetched to call
   * update().You can create a new instance and set some of its properties programmatically for via
   * JSON/XML marshalling etc. This is described as a 'stateless update'.
   * <p>
   * <b>Optimistic Locking: </b> Note that if the version property is not set when update() is
   * called then no optimistic locking is performed (internally ConcurrencyMode.NONE is used).
   *
   * <pre>{@code
   *
   *   // A 'stateless update' example
   *   Customer customer = new Customer();
   *   customer.setId(7);
   *   customer.setName("ModifiedNameNoOCC");
   *   database.update(customer);
   *
   * }</pre>
   */
  void update(Object bean) throws OptimisticLockException;

  /**
   * Update a bean additionally specifying a transaction.
   */
  void update(Object bean, Transaction transaction) throws OptimisticLockException;

  /**
   * Update a collection of beans. If there is no current transaction one is created and used to
   * update all the beans in the collection.
   */
  void updateAll(Collection<?> beans) throws OptimisticLockException;

  /**
   * Update a collection of beans with an explicit transaction.
   */
  void updateAll(Collection<?> beans, Transaction transaction) throws OptimisticLockException;

  /**
   * Merge the bean using the default merge options (no paths specified, default delete).
   *
   * @param bean The bean to merge
   */
  void merge(Object bean);

  /**
   * Merge the bean using the given merge options.
   *
   * @param bean    The bean to merge
   * @param options The options to control the merge
   */
  void merge(Object bean, MergeOptions options);

  /**
   * Merge the bean using the given merge options and a transaction.
   *
   * @param bean    The bean to merge
   * @param options The options to control the merge
   */
  void merge(Object bean, MergeOptions options, Transaction transaction);

  /**
   * Insert the bean.
   * <p>
   * Compared to save() this forces bean to perform an insert rather than trying to decide
   * based on the bean state. As such this is useful when you fetch beans from one database
   * and want to insert them into another database (and you want to explicitly insert them).
   */
  void insert(Object bean);

  /**
   * Insert the bean with options (ON CONFLICT DO UPDATE | DO NOTHING).
   * <p>
   * Currently, this is limited to use with Postgres only,
   * <p>
   * When using this ebean will look to determine the unique columns by looking at
   * the mapping like {@code @Column(unique=true} and {@code @Index(unique=true}.
   */
  void insert(Object bean, InsertOptions insertOptions);

  /**
   * Insert the bean with a transaction.
   */
  void insert(Object bean, Transaction transaction);

  /**
   * Insert the beans with options (ON CONFLICT DO UPDATE | DO NOTHING) and transaction.
   * <p>
   * Currently, this is limited to use with Postgres only,
   */
  void insert(Object bean, InsertOptions insertOptions, Transaction transaction);

  /**
   * Insert a collection of beans. If there is no current transaction one is created and used to
   * insert all the beans in the collection.
   */
  void insertAll(Collection<?> beans);

  /**
   * Insert the beans with options - typically ON CONFLICT DO UPDATE | DO NOTHING.
   * <p>
   * Currently, this is limited to use with Postgres only,
   */
  void insertAll(Collection<?> beans, InsertOptions options);

  /**
   * Insert a collection of beans with an explicit transaction.
   */
  void insertAll(Collection<?> beans, Transaction transaction);

  /**
   * Insert the beans with options (ON CONFLICT DO UPDATE | DO NOTHING) and transaction.
   * <p>
   * Currently, this is limited to use with Postgres only,
   */
  void insertAll(Collection<?> beans, InsertOptions options, Transaction transaction);

  /**
   * Execute explicitly passing a transaction.
   */
  int execute(SqlUpdate updSql, Transaction transaction);

  /**
   * Execute explicitly passing a transaction.
   */
  int execute(CallableSql callableSql, Transaction transaction);

  /**
   * Execute a Runnable in a Transaction with an explicit scope.
   * <p>
   * The scope can control the transaction type, isolation and rollback semantics.
   *
   * <pre>{@code
   *  // set specific transactional scope settings
   *  TxScope scope = TxScope.requiresNew().setIsolation(TxIsolation.SERIALIZABLE);
   *
   *  database.execute(scope, new Runnable() {
   *    public void run() {
   *      User u1 = database.find(User.class, 1);
   *      ...
   *    }
   *  });
   *
   * }</pre>
   */
  void execute(TxScope scope, Runnable runnable);

  /**
   * Execute a Runnable in a Transaction with the default scope.
   * <p>
   * The default scope runs with REQUIRED and by default will rollback on any
   * exception (checked or runtime).
   *
   * <pre>{@code
   *  database.execute(() -> {
   *
   *    User u1 = database.find(User.class, 1);
   *    User u2 = database.find(User.class, 2);
   *
   *    u1.setName("u1 mod");
   *    u2.setName("u2 mod");
   *
   *    u1.save();
   *    u2.save();
   *  });
   *
   * }</pre>
   */
  void execute(Runnable runnable);

  /**
   * Execute a TxCallable in a Transaction with an explicit scope.
   * <p>
   * The scope can control the transaction type, isolation and rollback
   * semantics.
   *
   * <pre>{@code
   *   // set specific transactional scope settings
   *   TxScope scope = TxScope.requiresNew().setIsolation(TxIsolation.SERIALIZABLE);
   *
   *   database.executeCall(scope, new Callable<String>() {
   *     public String call() {
   *       User u1 = database.find(User.class, 1);
   *       ...
   *       return u1.getEmail();
   *     }
   *   });
   *
   * }</pre>
   */
  <T> T executeCall(TxScope scope, Callable<T> callable);

  /**
   * Execute a TxCallable in a Transaction with the default scope.
   * <p>
   * The default scope runs with REQUIRED and by default will rollback on any
   * exception (checked or runtime).
   *
   * <pre>{@code
   *   database.executeCall(new Callable<String>() {
   *     public String call() {
   *       User u1 = database.find(User.class, 1);
   *       User u2 = database.find(User.class, 2);
   *
   *       u1.setName("u1 mod");
   *       u2.setName("u2 mod");
   *
   *       database.save(u1);
   *       database.save(u2);
   *
   *       return u1.getEmail();
   *     }
   *   });
   * }</pre>
   */
  <T> T executeCall(Callable<T> callable);

  /**
   * Return the manager of the server cache ("L2" cache).
   */
  ServerCacheManager cacheManager();

  /**
   * Return the BackgroundExecutor service for asynchronous processing of queries.
   */
  BackgroundExecutor backgroundExecutor();

  /**
   * Return the JsonContext for reading/writing JSON.
   * <p>
   * This instance is safe to be used concurrently by multiple threads and this
   * method is cheap to call.
   *
   * <h3>Simple example:</h3>
   * <pre>{@code
   *   JsonContext json = database.json();
   *   String jsonOutput = json.toJson(list);
   *   System.out.println(jsonOutput);
   * }</pre>
   *
   * <p>
   * <h3>Using PathProperties:</h3>
   * <pre>{@code
   *   // specify just the properties we want
   *   PathProperties paths = PathProperties.parse("name, status, anniversary");
   *
   *   List<Customer> customers =
   *     database.find(Customer.class)
   *       // apply those paths to the query (only fetch what we need)
   *       .apply(paths)
   *       .where().ilike("name", "rob%")
   *       .findList();
   *
   *   // ... get the json
   *   JsonContext jsonContext = database.json();
   *   String json = jsonContext.toJson(customers, paths);
   *
   * }</pre>
   *
   * @see FetchPath
   * @see Query#apply(FetchPath)
   */
  JsonContext json();

  /**
   * Return a ScriptRunner for running SQL or DDL scripts.
   * <p/>
   * Intended to use mostly in testing to run seed SQL scripts or truncate table scripts etc.
   */
  ScriptRunner script();

  /**
   * Return the Document store.
   */
  DocumentStore docStore();

  /**
   * Publish a single bean given its type and id returning the resulting live bean.
   * <p>
   * The values are published from the draft to the live bean.
   *
   * @param <T>         the type of the entity bean
   * @param beanType    the type of the entity bean
   * @param id          the id of the entity bean
   * @param transaction the transaction the publish process should use (can be null)
   */
  @Nullable
  <T> T publish(Class<T> beanType, Object id, Transaction transaction);

  /**
   * Publish a single bean given its type and id returning the resulting live bean.
   * This will use the current transaction or create one if required.
   * <p>
   * The values are published from the draft to the live bean.
   *
   * @param <T>      the type of the entity bean
   * @param beanType the type of the entity bean
   * @param id       the id of the entity bean
   */
  @Nullable
  <T> T publish(Class<T> beanType, Object id);

  /**
   * Publish the beans that match the query returning the resulting published beans.
   * <p>
   * The values are published from the draft beans to the live beans.
   *
   * @param <T>         the type of the entity bean
   * @param query       the query used to select the draft beans to publish
   * @param transaction the transaction the publish process should use (can be null)
   */
  <T> List<T> publish(Query<T> query, Transaction transaction);

  /**
   * Publish the beans that match the query returning the resulting published beans.
   * This will use the current transaction or create one if required.
   * <p>
   * The values are published from the draft beans to the live beans.
   *
   * @param <T>   the type of the entity bean
   * @param query the query used to select the draft beans to publish
   */
  <T> List<T> publish(Query<T> query);

  /**
   * Restore the draft bean back to the live state.
   * <p>
   * The values from the live beans are set back to the draft bean and the
   * <code>@DraftDirty</code> and <code>@DraftReset</code> properties are reset.
   *
   * @param <T>         the type of the entity bean
   * @param beanType    the type of the entity bean
   * @param id          the id of the entity bean to restore
   * @param transaction the transaction the restore process should use (can be null)
   */
  @Nullable
  <T> T draftRestore(Class<T> beanType, Object id, Transaction transaction);

  /**
   * Restore the draft bean back to the live state.
   * <p>
   * The values from the live beans are set back to the draft bean and the
   * <code>@DraftDirty</code> and <code>@DraftReset</code> properties are reset.
   *
   * @param <T>      the type of the entity bean
   * @param beanType the type of the entity bean
   * @param id       the id of the entity bean to restore
   */
  @Nullable
  <T> T draftRestore(Class<T> beanType, Object id);

  /**
   * Restore the draft beans matching the query back to the live state.
   * <p>
   * The values from the live beans are set back to the draft bean and the
   * <code>@DraftDirty</code> and <code>@DraftReset</code> properties are reset.
   *
   * @param <T>         the type of the entity bean
   * @param query       the query used to select the draft beans to restore
   * @param transaction the transaction the restore process should use (can be null)
   */
  <T> List<T> draftRestore(Query<T> query, Transaction transaction);

  /**
   * Restore the draft beans matching the query back to the live state.
   * <p>
   * The values from the live beans are set back to the draft bean and the
   * <code>@DraftDirty</code> and <code>@DraftReset</code> properties are reset.
   *
   * @param <T>   the type of the entity bean
   * @param query the query used to select the draft beans to restore
   */
  <T> List<T> draftRestore(Query<T> query);

  /**
   * Returns the set of properties/paths that are unknown (do not map to known properties or paths).
   * <p>
   * Validate the query checking the where and orderBy expression paths to confirm if
   * they represent valid properties/path for the given bean type.
   */
  <T> Set<String> validateQuery(Query<T> query);

  /**
   * Load and lock the bean using {@code select for update}.
   * <p>
   * This should be executed inside a transaction and results in the bean being loaded or
   * refreshed from the database and a database row lock held via {@code select for update}.
   * <p>
   * The bean needs to have an ID property set and can be a reference bean (only has ID)
   * or partially or fully populated bean. This will load all the properties of the bean
   * from the database using {@code select for update} obtaining a database row lock (using WAIT).
   *
   * @param bean The entity bean that we wish to obtain a database lock on.
   */
  void lock(Object bean);

  /**
   * Truncate all the given tables.
   */
  void truncate(String... tables);

  /**
   * Truncate the base tables for the given bean types.
   */
  void truncate(Class<?>... beanTypes);

}

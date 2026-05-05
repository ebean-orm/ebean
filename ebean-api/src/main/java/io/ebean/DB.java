package io.ebean;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import io.ebean.annotation.TxIsolation;
import io.ebean.cache.ServerCacheManager;
import io.ebean.plugin.Property;
import io.ebean.text.json.JsonContext;

import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * DB is a registry of {@link Database} by name.
 * <p>
 * DB additionally provides a convenient way to use the 'default' Database.
 * <p>
 * <h3>Default database</h3>
 * One of the Database instances can be registered as the "default database"
 * and can be obtained using <code>DB.getDefault()</code>
 *
 * <pre>{@code
 *
 * Database database = DB.getDefault();
 *
 * }</pre>
 *
 * <h3>Named database</h3>
 * <p>
 * Multiple database instances can be registered with DB and we can obtain them
 * using <code>DB.byName()</code>
 *
 * <pre>{@code
 *
 * Database hrDatabase = DB.byName("hr");
 *
 * }</pre>
 *
 * <h3>Convenience methods</h3>
 * <p>
 * DB has methods like {@link #find(Class)} and {@link #save(Object)} which are
 * just convenience for using the default database.
 *
 * <pre>{@code
 *
 * // fetch using the default database
 * Order order = DB.find(Order.class, 10);
 *
 * // is the same as
 * Database database = DB.getDefault();
 * Order order = database.find(Order.class, 10);
 *
 * }</pre>
 */
@NullMarked
public final class DB {

  private static final DbContext context = DbContext.getInstance();

  /**
   * Hide constructor.
   */
  private DB() {
  }

  /**
   * Backdoor for registering a mock implementation of Database as the default database.
   */
  protected static Database mock(String name, Database server, boolean defaultServer) {
    return context.mock(name, server, defaultServer);
  }

  /**
   * Return the default database.
   */
  public static Database getDefault() {
    return context.getDefault();
  }

  /**
   * Return the database for the given name.
   *
   * @param name The name of the database
   */
  public static Database byName(String name) {
    return context.get(name);
  }

  /**
   * Return the ScriptRunner for the default database.
   * <p>
   * Useful to run SQL scripts that are resources. For example a test script
   * for inserting seed data for a particular test.
   *
   * <pre>{@code
   *
   *   DB.script().run("/scripts/test-script.sql")
   *
   * }</pre>
   */
  public static ScriptRunner script() {
    return getDefault().script();
  }

  /**
   * Return the ExpressionFactory from the default database.
   * <p>
   * The ExpressionFactory is used internally by the query and ExpressionList to
   * build the WHERE and HAVING clauses. Alternatively you can use the
   * ExpressionFactory directly to create expressions to add to the query where
   * clause.
   * <p>
   * Alternatively you can use the {@link Expr} as a shortcut to the
   * ExpressionFactory of the 'Default' database.
   * <p>
   * You generally need to the an ExpressionFactory (or {@link Expr}) to build
   * an expression that uses OR like Expression e = Expr.or(..., ...);
   */
  public static ExpressionFactory expressionFactory() {
    return getDefault().expressionFactory();
  }

  /**
   * Return the next identity value for a given bean type.
   * <p>
   * This will only work when a IdGenerator is on this bean type such as a DB
   * sequence or UUID.
   * <p>
   * For DB's supporting getGeneratedKeys and sequences such as Oracle10 you do
   * not need to use this method generally. It is made available for more
   * complex cases where it is useful to get an ID prior to some processing.
   */
  public static Object nextId(Class<?> beanType) {
    return getDefault().nextId(beanType);
  }

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
   * <pre>{@code
   *
   *   try (Transaction transaction = DB.beginTransaction()) {
   *
   *     Order order = DB.find(Order.class, 42);
   *     order.setStatus(Status.COMPLETE);
   *     order.save();
   *
   *     transaction.commit();
   *   }
   *
   * }</pre>
   * <p>
   * If we want to externalise the transaction management then we do this via Database.
   * With Database we can pass the transaction to the various find(), save() and execute()
   * methods. This gives us the ability to create the transactions externally from Ebean
   * and use the transaction explicitly via the various methods available on Database.
   */
  public static Transaction beginTransaction() {
    return getDefault().beginTransaction();
  }

  /**
   * Create a new transaction that is not held in TransactionThreadLocal.
   * <p>
   * You will want to do this if you want multiple Transactions in a single
   * thread or generally use transactions outside of the TransactionThreadLocal
   * management.
   */
  public static Transaction createTransaction() {
    return getDefault().createTransaction();
  }

  /**
   * Start a transaction additionally specifying the isolation level.
   *
   * @param isolation the Transaction isolation level
   */
  public static Transaction beginTransaction(TxIsolation isolation) {
    return getDefault().beginTransaction(isolation);
  }

  /**
   * Start a transaction typically specifying REQUIRES_NEW or REQUIRED semantics.
   * <p>
   * Note that this provides an try finally alternative to using {@link #executeCall(TxScope, Callable)} or
   * {@link #execute(TxScope, Runnable)}.
   * <p>
   * <h3>REQUIRES_NEW example:</h3>
   * <pre>{@code
   * // Start a new transaction. If there is a current transaction
   * // suspend it until this transaction ends
   *
   * try (Transaction txn = DB.beginTransaction(TxScope.requiresNew())) {
   *   ...
   *
   *   // commit the transaction
   *   txn.commit();
   * }
   * }</pre>
   *
   * <h3>REQUIRED example:</h3>
   * <pre>{@code
   * // start a new transaction if there is not a current transaction
   *
   * try (Transaction txn = DB.beginTransaction(TxScope.required())) {
   *   ...
   *
   *   // commit the transaction if it was created or
   *   // do nothing if there was already a current transaction
   *   txn.commit();
   * }
   * }</pre>
   */
  public static Transaction beginTransaction(TxScope scope) {
    return getDefault().beginTransaction(scope);
  }

  /**
   * Returns the current transaction or null if there is no current transaction in scope.
   */
  public static Transaction currentTransaction() {
    return getDefault().currentTransaction();
  }

  /**
   * The batch will be flushing automatically but, you can use this to explicitly
   * flush the batch if you like.
   * <p>
   * Flushing occurs automatically when:
   * </p>
   * <ul>
   * <li>the batch size is reached</li>
   * <li>A query is executed on the same transaction</li>
   * <li>UpdateSql or CallableSql are mixed with bean save and delete</li>
   * <li>Transaction commit occurs</li>
   * <li>A getter method is called on a batched bean</li>
   * </ul>
   */
  public static void flush() {
    currentTransaction().flush();
  }

  /**
   * Register a TransactionCallback on the currently active transaction.
   * <p>
   * If there is no currently active transaction then a PersistenceException is thrown.
   *
   * @param transactionCallback the transaction callback to be registered with the current transaction
   * @throws PersistenceException if there is no currently active transaction
   */
  public static void register(TransactionCallback transactionCallback) throws PersistenceException {
    getDefault().register(transactionCallback);
  }

  /**
   * Mark the current transaction as rollback only.
   */
  public static void setRollbackOnly() {
    getDefault().currentTransaction().setRollbackOnly();
  }

  /**
   * Return a map of the differences between two objects of the same type.
   * <p>
   * When null is passed in for b, then the 'OldValues' of a is used for the
   * difference comparison.
   */
  public static Map<String, ValuePair> diff(Object a, Object b) {
    return getDefault().diff(a, b);
  }

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
   * When a save cascades via a OneToMany or ManyToMany Ebean will automatically
   * set the 'parent' object to the 'detail' object. In the example below in
   * saving the order and cascade saving the order details the 'parent' order
   * will be set against each order detail when it is saved.
   */
  public static void save(Object bean) throws OptimisticLockException {
    getDefault().save(bean);
  }

  /**
   * Insert the bean. This is useful when you set the Id property on a bean and
   * want to explicitly insert it.
   */
  public static void insert(Object bean) {
    getDefault().insert(bean);
  }

  /**
   * Insert a collection of beans.
   */
  public static void insertAll(Collection<?> beans) {
    getDefault().insertAll(beans);
  }

  /**
   * Marks the entity bean as dirty.
   * <p>
   * This is used so that when a bean that is otherwise unmodified is updated with the version
   * property updated.
   * <p>
   * An unmodified bean that is saved or updated is normally skipped and this marks the bean as
   * dirty so that it is not skipped.
   * <pre>{@code
   *
   *   Customer customer = DB.find(Customer, id);
   *
   *   // mark the bean as dirty so that a save() or update() will
   *   // increment the version property
   *   DB.markAsDirty(customer);
   *   DB.save(customer);
   *
   * }</pre>
   */
  public static void markAsDirty(Object bean) throws OptimisticLockException {
    getDefault().markAsDirty(bean);
  }

  /**
   * Saves the bean using an update. If you know you are updating a bean then it is preferrable to
   * use this update() method rather than save().
   * <p>
   * <b>Stateless updates:</b> Note that the bean does not have to be previously fetched to call
   * update().You can create a new instance and set some of its properties programmatically for via
   * JSON/XML marshalling etc. This is described as a 'stateless update'.
   * <p>
   * <b>Optimistic Locking: </b> Note that if the version property is not set when update() is
   * called then no optimistic locking is performed (internally ConcurrencyMode.NONE is used).
   * <p>
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
  public static void update(Object bean) throws OptimisticLockException {
    getDefault().update(bean);
  }

  /**
   * Update the beans in the collection.
   */
  public static void updateAll(Collection<?> beans) throws OptimisticLockException {
    getDefault().updateAll(beans);
  }

  /**
   * Merge the bean using the default merge options.
   *
   * @param bean The bean to merge
   */
  public static void merge(Object bean) {
    getDefault().merge(bean);
  }

  /**
   * Merge the bean using the given merge options.
   *
   * @param bean    The bean to merge
   * @param options The options to control the merge
   */
  public static void merge(Object bean, MergeOptions options) {
    getDefault().merge(bean, options);
  }

  /**
   * Save all the beans from a Collection.
   */
  public static int saveAll(Collection<?> beans) throws OptimisticLockException {
    return getDefault().saveAll(beans);
  }

  /**
   * Save all the beans from a Collection.
   */
  public static int saveAll(Object... beans) throws OptimisticLockException {
    return getDefault().saveAll(beans);
  }

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
   *
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
   *  }
   *
   * }</pre>
   *
   * @param bean The entity bean to check uniqueness on
   * @return a set of Properties if constraint validation was detected or empty list.
   */
  public static Set<Property> checkUniqueness(Object bean) {
    return getDefault().checkUniqueness(bean);
  }

  /**
   * Same as {@link #checkUniqueness(Object)} but with given transaction.
   */
  public static Set<Property> checkUniqueness(Object bean, Transaction transaction) {
    return getDefault().checkUniqueness(bean, transaction);
  }

  /**
   * Delete the bean.
   * <p>
   * This will return true if the bean was deleted successfully or JDBC batch is being used.
   * <p>
   * If there is no current transaction one will be created and committed for
   * you automatically.
   * <p>
   * If the bean is configured with <code>@SoftDelete</code> then this will perform a soft
   * delete rather than a hard/permanent delete.
   * <p>
   * If the Bean does not have a version property (or loaded version property) and
   * the bean does not exist then this returns false indicating that nothing was
   * deleted. Note that, if JDBC batch mode is used then this always returns true.
   */
  public static boolean delete(Object bean) throws OptimisticLockException {
    return getDefault().delete(bean);
  }

  /**
   * Delete the bean in permanent fashion (will not use soft delete).
   */
  public static boolean deletePermanent(Object bean) throws OptimisticLockException {
    return getDefault().deletePermanent(bean);
  }

  /**
   * Delete the bean given its type and id.
   */
  public static int delete(Class<?> beanType, Object id) {
    return getDefault().delete(beanType, id);
  }

  /**
   * Delete permanent the bean given its type and id.
   */
  public static int deletePermanent(Class<?> beanType, Object id) {
    return getDefault().deletePermanent(beanType, id);
  }

  /**
   * Delete several beans given their type and id values.
   */
  public static int deleteAll(Class<?> beanType, Collection<?> ids) {
    return getDefault().deleteAll(beanType, ids);
  }

  /**
   * Delete permanent several beans given their type and id values.
   */
  public static int deleteAllPermanent(Class<?> beanType, Collection<?> ids) {
    return getDefault().deleteAllPermanent(beanType, ids);
  }

  /**
   * Delete all the beans in the Collection.
   */
  public static int deleteAll(Collection<?> beans) throws OptimisticLockException {
    return getDefault().deleteAll(beans);
  }

  /**
   * Delete permanent all the beans in the Collection (will not use soft delete).
   */
  public static int deleteAllPermanent(Collection<?> beans) throws OptimisticLockException {
    return getDefault().deleteAllPermanent(beans);
  }

  /**
   * Refresh the values of a bean.
   * <p>
   * Note that this resets OneToMany and ManyToMany properties so that if they
   * are accessed a lazy load will refresh the many property.
   */
  public static void refresh(Object bean) {
    getDefault().refresh(bean);
  }

  /**
   * Refresh a 'many' property of a bean.
   *
   * <pre>{@code
   *
   *   Order order = ...;
   *   ...
   *   // refresh the order details...
   *   DB.refreshMany(order, "details");
   *
   * }</pre>
   *
   * @param bean             the entity bean containing the List Set or Map to refresh.
   * @param manyPropertyName the property name of the List Set or Map to refresh.
   */
  public static void refreshMany(Object bean, String manyPropertyName) {
    getDefault().refreshMany(bean, manyPropertyName);
  }

  /**
   * Get a reference object.
   * <p>
   * This is sometimes described as a proxy (with lazy loading).
   *
   * <pre>{@code
   *
   *   Product product = DB.getReference(Product.class, 1);
   *
   *   // You can get the id without causing a fetch/lazy load
   *   Integer productId = product.getId();
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
  public static <T> T reference(Class<T> beanType, Object id) {
    return getDefault().reference(beanType, id);
  }

  /**
   * Sort the list using the sortByClause which can contain a comma delimited
   * list of property names and keywords asc, desc, nullsHigh and nullsLow.
   * <ul>
   * <li>asc - ascending order (which is the default)</li>
   * <li>desc - Descending order</li>
   * <li>nullsHigh - Treat null values as high/large values (which is the default)</li>
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
   *   List<Order> list = DB.find(Order.class)
   *     .fetch("customer")
   *     .orderBy("id")
   *     .findList();
   *
   *   // sort by customer name ascending, then by order shipDate
   *   // ... then by the order status descending
   *   DB.sort(list, "customer.name, shipDate, status desc");
   *
   *   // sort by customer name descending (with nulls low)
   *   // ... then by the order id
   *   DB.sort(list, "customer.name desc nullsLow, id");
   *
   * }</pre>
   *
   * @param list         the list of entity beans
   * @param sortByClause the properties to sort the list by
   */
  public static <T> void sort(List<T> list, String sortByClause) {
    getDefault().sort(list, sortByClause);
  }

  /**
   * Find a bean using its unique id. This will not use caching.
   * <pre>{@code
   *
   *   // Fetch order 1
   *   Order order = DB.find(Order.class, 1);
   *
   * }</pre>
   * <p>
   * If you want more control over the query then you can use createQuery() and Query.findOne();
   *
   * <pre>{@code
   *
   *   // ... additionally fetching customer, customer shipping address,
   *   // order details, and the product associated with each order detail.
   *   // note: only product id and name is fetch (its a "partial object").
   *   // note: all other objects use "*" and have all their properties fetched.
   *
   *   Query<Order> query = DB.find(Order.class)
   *     .setId(1)
   *     .fetch("customer")
   *     .fetch("customer.shippingAddress")
   *     .fetch("details")
   *     .query();
   *
   *   // fetch associated products but only fetch their product id and name
   *   query.fetch("details.product", "name");
   *
   *   // traverse the object graph...
   *
   *   Order order = query.findOne();
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
  public static <T> T find(Class<T> beanType, Object id) {
    return getDefault().find(beanType, id);
  }

  /**
   * Look to execute a native sql query that does not return beans but instead
   * returns SqlRow or uses {@link RowMapper}.
   * <p>
   * Refer to {@link DtoQuery} for native sql queries returning DTO beans.
   * <p>
   * Refer to {@link #findNative(Class, String)} for native sql queries returning entity beans.
   */
  public static SqlQuery sqlQuery(String sql) {
    return getDefault().sqlQuery(sql);
  }

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
  public static SqlUpdate sqlUpdate(String sql) {
    return getDefault().sqlUpdate(sql);
  }

  /**
   * Create a CallableSql to execute a given stored procedure.
   *
   * @see CallableSql
   */
  public static CallableSql createCallableSql(String sql) {
    return getDefault().createCallableSql(sql);
  }

  /**
   * Create a orm update where you will supply the insert/update or delete
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
   *   Update<Topic> update = DB.createUpdate(Topic.class, updStatement);
   *
   *   update.set("pc", 9);
   *   update.set("id", 3);
   *
   *   int rows = update.execute();
   *   System.out.println("rows updated:" + rows);
   *
   * }</pre>
   */
  public static <T> Update<T> createUpdate(Class<T> beanType, String ormUpdate) {
    return getDefault().createUpdate(beanType, ormUpdate);
  }


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
  public static <T> Query<T> createNamedQuery(Class<T> beanType, String namedQuery) {
    return getDefault().createNamedQuery(beanType, namedQuery);
  }

  /**
   * Create a query for a type of entity bean.
   * <p>
   * You can use the methods on the Query object to specify fetch paths,
   * predicates, order by, limits etc.
   * <p>
   * You then use findList(), findSet(), findMap() and findOne() to execute
   * the query and return the collection or bean.
   * <p>
   * Note that a query executed by {@link Query#findList()} etc will execute against
   * the same database from which is was created.
   *
   * @param beanType the class of entity to be fetched
   * @return A ORM Query for this beanType
   */
  public static <T> Query<T> createQuery(Class<T> beanType) {
    return getDefault().createQuery(beanType);
  }

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
   *   String eql = "fetch customer fetch details fetch details.product (name) where id = :orderId ";
   *
   *   Query<Order> query = DB.createQuery(Order.class, eql);
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
   * @param eql      The Ebean query
   * @param <T>      The type of the entity bean
   * @return The query with expressions defined as per the parsed query statement
   */
  public static <T> Query<T> createQuery(Class<T> beanType, String eql) {
    return getDefault().createQuery(beanType, eql);
  }

  /**
   * Create a query for a type of entity bean.
   * <p>
   * This is actually the same as {@link #createQuery(Class)}. The reason it
   * exists is that people used to JPA will probably be looking for a
   * createQuery method (the same as entityManager).
   *
   * @param beanType the type of entity bean to find
   * @return A ORM Query object for this beanType
   */
  public static <T> Query<T> find(Class<T> beanType) {
    return getDefault().find(beanType);
  }

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
  public static <T> Query<T> findNative(Class<T> beanType, String nativeSql) {
    return getDefault().findNative(beanType, nativeSql);
  }

  /**
   * Create a Query for DTO beans.
   * <p>
   * DTO beans are just normal bean like classes with public constructor(s) and setters.
   * They do not need to be registered with Ebean before use.
   *
   * @param dtoType The type of the DTO bean the rows will be mapped into.
   * @param sql     The SQL query to execute.
   * @param <T>     The type of the DTO bean.
   */
  public static <T> DtoQuery<T> findDto(Class<T> dtoType, String sql) {
    return getDefault().findDto(dtoType, sql);
  }

  /**
   * Create an Update query to perform a bulk update.
   * <p>
   * <pre>{@code
   *
   *  int rows = DB.update(Customer.class)
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
  public static <T> UpdateQuery<T> update(Class<T> beanType) {
    return getDefault().update(beanType);
  }

  /**
   * Create a filter for sorting and filtering lists of entities locally without
   * going back to the database.
   * <p>
   * This produces and returns a new list with the sort and filters applied.
   * <p>
   * Refer to {@link Filter} for an example of its use.
   */
  public static <T> Filter<T> filter(Class<T> beanType) {
    return getDefault().filter(beanType);
  }

  /**
   * Execute a TxRunnable in a Transaction with an explicit scope.
   * <p>
   * The scope can control the transaction type, isolation and rollback
   * semantics.
   *
   * <pre>{@code
   *
   * // set specific transactional scope settings
   * TxScope scope = TxScope.requiresNew().setIsolation(TxIsolation.SERIALIZABLE);
   *
   * DB.execute(scope, new TxRunnable() {
   *   public void run() {
   *     User u1 = DB.find(User.class, 1);
   *     ...
   *   }
   * });
   *
   * }</pre>
   */
  public static void execute(TxScope scope, Runnable r) {
    getDefault().execute(scope, r);
  }

  /**
   * Execute a Runnable in a Transaction with the default scope.
   * <p>
   * The default scope runs with REQUIRED and by default will rollback on any
   * exception (checked or runtime).
   *
   * <pre>{@code
   *
   * DB.execute(() -> {
   *
   *   User u1 = DB.find(User.class, 1);
   *   User u2 = DB.find(User.class, 2);
   *
   *   u1.setName("u1 mod");
   *   u2.setName("u2 mod");
   *
   *   DB.save(u1);
   *   DB.save(u2);
   * });
   * }</pre>
   */
  public static void execute(Runnable r) {
    getDefault().execute(r);
  }

  /**
   * Execute a Callable in a Transaction with an explicit scope.
   * <p>
   * The scope can control the transaction type, isolation and rollback
   * semantics.
   *
   * <pre>{@code
   *
   * // set specific transactional scope settings
   * TxScope scope = TxScope.requiresNew().setIsolation(TxIsolation.SERIALIZABLE);
   *
   * DB.executeCall(scope, new Callable<String>() {
   *   public String call() {
   *     User u1 = DB.find(User.class, 1);
   *     ...
   *     return u1.getEmail();
   *   }
   * });
   * }</pre>
   */
  public static <T> T executeCall(TxScope scope, Callable<T> c) {
    return getDefault().executeCall(scope, c);
  }

  /**
   * Execute a Callable in a Transaction with the default scope.
   * <p>
   * The default scope runs with REQUIRED and by default will rollback on any
   * exception (checked or runtime).
   * <p>
   * This is basically the same as TxRunnable except that it returns an Object
   * (and you specify the return type via generics).
   *
   * <pre>{@code
   *
   * DB.executeCall(() -> {
   *
   *   User u1 = DB.find(User.class, 1);
   *   User u2 = DB.find(User.class, 2);
   *
   *   u1.setName("u1 mod");
   *   u2.setName("u2 mod");
   *
   *   DB.save(u1);
   *   DB.save(u2);
   *
   *   return u1.getEmail();
   * });
   * }</pre>
   */
  public static <T> T executeCall(Callable<T> c) {
    return getDefault().executeCall(c);
  }

  /**
   * Inform Ebean that tables have been modified externally. These could be the
   * result of from calling a stored procedure, other JDBC calls or external
   * programs including other frameworks.
   * <p>
   * If you use DB.execute(UpdateSql) then the table modification information
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
   * @param inserts   true if rows where inserted into the table
   * @param updates   true if rows on the table where updated
   * @param deletes   true if rows on the table where deleted
   */
  public static void externalModification(String tableName, boolean inserts, boolean updates, boolean deletes) {
    getDefault().externalModification(tableName, inserts, updates, deletes);
  }

  /**
   * Return the BeanState for a given entity bean.
   * <p>
   * This will return null if the bean is not an enhanced entity bean.
   */
  public static BeanState beanState(Object bean) {
    return getDefault().beanState(bean);
  }

  /**
   * Return the value of the Id property for a given bean.
   */
  public static Object beanId(Object bean) {
    return getDefault().beanId(bean);
  }

  /**
   * Load and lock the bean using {@code select for update}.
   * <p>
   * This should be executed inside a transaction.
   * <p>
   * The bean needs to have an ID property set and can be a reference bean (only has ID)
   * or partially or fully populated bean. This will load all the properties of the bean
   * from the database using {@code select for update}.
   *
   * @param bean The entity bean that we wish to obtain a database lock on.
   */
  public static void lock(Object bean) {
    getDefault().lock(bean);
  }

  /**
   * Return the manager of the level 2 cache ("L2" cache).
   */
  public static ServerCacheManager cacheManager() {
    return getDefault().cacheManager();
  }

  /**
   * Return the BackgroundExecutor service for asynchronous processing of
   * queries.
   */
  public static BackgroundExecutor backgroundExecutor() {
    return getDefault().backgroundExecutor();
  }

  /**
   * Return the JsonContext for reading/writing JSON.
   */
  public static JsonContext json() {
    return getDefault().json();
  }

  /**
   * Truncate the base tables for the given bean types.
   */
  public static void truncate(Class<?>... types) {
    getDefault().truncate(types);
  }

  /**
   * Truncate the given tables.
   */
  public static void truncate(String... tables) {
    getDefault().truncate(tables);
  }

}

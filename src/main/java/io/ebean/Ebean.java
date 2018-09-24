package io.ebean;

import io.ebean.annotation.TxIsolation;
import io.ebean.cache.ServerCacheManager;
import io.ebean.config.ServerConfig;
import io.ebean.plugin.Property;
import io.ebean.text.csv.CsvReader;
import io.ebean.text.json.JsonContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This Ebean object is effectively a singleton that holds a map of registered
 * {@link EbeanServer}s. It additionally provides a convenient way to use the
 * 'default' EbeanServer.
 * <p>
 * If you are using a Dependency Injection framework such as
 * <strong>Spring</strong> or <strong>Guice</strong> you will probably
 * <strong>NOT</strong> use this Ebean singleton object. Instead you will
 * configure and construct EbeanServer instances using {@link ServerConfig} and
 * {@link EbeanServerFactory} and inject those EbeanServer instances into your
 * data access objects.
 * </p>
 * <p>
 * In documentation "Ebean singleton" refers to this object.
 * </p>
 * <ul>
 * <li>There is one EbeanServer per Database (javax.sql.DataSource).</li>
 * <li>EbeanServers can be 'registered' with the Ebean singleton (put into its
 * map). Registered EbeanServer's can later be retrieved via
 * {@link #getServer(String)}.</li>
 * <li>One EbeanServer can be referred to as the 'default' EbeanServer. For
 * convenience, the Ebean singleton (this object) provides methods such as
 * {@link #find(Class)} that proxy through to the 'default' EbeanServer. This
 * can be useful for applications that use a single database.</li>
 * </ul>
 * <p>
 * For developer convenience Ebean has static methods that proxy through to the
 * methods on the <em>'default'</em> EbeanServer. These methods are provided for
 * developers who are mostly using a single database. Many developers will be
 * able to use the methods on Ebean rather than get a EbeanServer.
 * </p>
 * <p>
 * EbeanServers can be created and used without ever needing or using the Ebean
 * singleton. Refer to {@link ServerConfig#setRegister(boolean)}.
 * </p>
 * <p>
 * You can either programmatically create/register EbeanServers via
 * {@link EbeanServerFactory} or they can automatically be created and
 * registered when you first use the Ebean singleton. When EbeanServers are
 * created automatically they are configured using information in the
 * ebean.properties file.
 * </p>
 * <pre>{@code
 *
 *   // fetch shipped orders (and also their customer)
 *   List<Order> list = Ebean.find(Order.class)
 * 	  .fetch("customer")
 * 	  .where()
 * 	  .eq("status.code", Order.Status.SHIPPED)
 * 	  .findList();
 *
 *   // read/use the order list ...
 *   for (Order order : list) {
 * 	   Customer customer = order.getCustomer();
 * 	   ...
 *   }
 *
 * }</pre>
 * <pre>{@code
 *
 *   // fetch order 10, modify and save
 *   Order order = Ebean.find(Order.class, 10);
 *
 *   OrderStatus shipped = Ebean.getReference(OrderStatus.class,"SHIPPED");
 *   order.setStatus(shipped);
 *   order.setShippedDate(shippedDate);
 *   ...
 *
 *   // implicitly creates a transaction and commits
 *   Ebean.save(order);
 *
 * }</pre>
 * <p>
 * When you have multiple databases and need access to a specific one the
 * {@link #getServer(String)} method provides access to the EbeanServer for that
 * specific database.
 * </p>
 * <pre>{@code
 *
 *   // Get access to the Human Resources EbeanServer/Database
 *   EbeanServer hrDb = Ebean.getServer("hr");
 *
 *   // fetch contact 3 from the HR database
 *   Contact contact = hrDb.find(Contact.class, 3);
 *
 *   contact.setName("I'm going to change");
 *   ...
 *
 *   // save the contact back to the HR database
 *   hrDb.save(contact);
 *
 * }</pre>
 */
public final class Ebean {
  private static final Logger logger = LoggerFactory.getLogger(Ebean.class);

  static {
    EbeanVersion.getVersion(); // initalizes the version class and logs the version.
  }

  /**
   * Manages creation and cache of EbeanServers.
   */
  private static final Ebean.ServerManager serverMgr = new Ebean.ServerManager();

  /**
   * Helper class for managing fast and safe access and creation of
   * EbeanServers.
   */
  private static final class ServerManager {

    /**
     * Cache for fast concurrent read access.
     */
    private final ConcurrentHashMap<String, EbeanServer> concMap = new ConcurrentHashMap<>();

    /**
     * Cache for synchronized read, creation and put. Protected by the monitor object.
     */
    private final HashMap<String, EbeanServer> syncMap = new HashMap<>();

    private final Object monitor = new Object();

    /**
     * The 'default' EbeanServer.
     */
    private EbeanServer defaultServer;

    private ServerManager() {

      try {
        if (!PrimaryServer.isSkip()) {
          // look to see if there is a default server defined
          String defaultName = PrimaryServer.getDefaultServerName();
          logger.debug("defaultName:{}", defaultName);
          if (defaultName != null && !defaultName.trim().isEmpty()) {
            defaultServer = getWithCreate(defaultName.trim());
          }
        }
      } catch (Throwable e) {
        logger.error("Error trying to create the default EbeanServer", e);
        throw new RuntimeException(e);
      }
    }

    private EbeanServer getDefaultServer() {
      if (defaultServer == null) {
        String msg = "The default EbeanServer has not been defined?";
        msg += " This is normally set via the ebean.datasource.default property.";
        msg += " Otherwise it should be registered programmatically via registerServer()";
        throw new PersistenceException(msg);
      }
      return defaultServer;
    }

    private EbeanServer get(String name) {
      if (name == null || name.isEmpty()) {
        return defaultServer;
      }
      // non-synchronized read
      EbeanServer server = concMap.get(name);
      if (server != null) {
        return server;
      }
      // synchronized read, create and put
      return getWithCreate(name);
    }

    /**
     * Synchronized read, create and put of EbeanServers.
     */
    private EbeanServer getWithCreate(String name) {

      synchronized (monitor) {

        EbeanServer server = syncMap.get(name);
        if (server == null) {
          // register when creating server this way
          server = EbeanServerFactory.create(name);
          register(server, false);
        }
        return server;
      }
    }

    /**
     * Register a server so we can get it by its name.
     */
    private void register(EbeanServer server, boolean isDefaultServer) {
      registerWithName(server.getName(), server, isDefaultServer);
    }

    private void registerWithName(String name, EbeanServer server, boolean isDefaultServer) {
      synchronized (monitor) {
        concMap.put(name, server);
        syncMap.put(name, server);
        if (isDefaultServer) {
          defaultServer = server;
        }
      }
    }

  }

  private Ebean() {
  }

  /**
   * Get the EbeanServer for a given DataSource. If name is null this will
   * return the 'default' EbeanServer.
   * <p>
   * This is provided to access EbeanServer for databases other than the
   * 'default' database. EbeanServer also provides more control over
   * transactions and the ability to use transactions created externally to
   * Ebean.
   * </p>
   * <pre>{@code
   * // use the "hr" database
   * EbeanServer hrDatabase = Ebean.getServer("hr");
   *
   * Person person = hrDatabase.find(Person.class, 10);
   * }</pre>
   *
   * @param name the name of the server, can use null for the 'default server'
   */
  public static EbeanServer getServer(String name) {
    return serverMgr.get(name);
  }

  /**
   * Returns the default EbeanServer.
   * <p>
   * This is equivalent to <code>Ebean.getServer(null);</code>
   * </p>
   */
  public static EbeanServer getDefaultServer() {
    return serverMgr.getDefaultServer();
  }

  /**
   * Return the ExpressionFactory from the default server.
   * <p>
   * The ExpressionFactory is used internally by the query and ExpressionList to
   * build the WHERE and HAVING clauses. Alternatively you can use the
   * ExpressionFactory directly to create expressions to add to the query where
   * clause.
   * </p>
   * <p>
   * Alternatively you can use the {@link Expr} as a shortcut to the
   * ExpressionFactory of the 'Default' EbeanServer.
   * </p>
   * <p>
   * You generally need to the an ExpressionFactory (or {@link Expr}) to build
   * an expression that uses OR like Expression e = Expr.or(..., ...);
   * </p>
   */
  public static ExpressionFactory getExpressionFactory() {
    return serverMgr.getDefaultServer().getExpressionFactory();
  }

  /**
   * Register the server with this Ebean singleton. Specify if the registered
   * server is the primary/default server.
   */
  public static void register(EbeanServer server, boolean defaultServer) {
    serverMgr.register(server, defaultServer);
  }

  /**
   * Backdoor for registering a mock implementation of EbeanServer as the default server.
   */
  protected static EbeanServer mock(String name, EbeanServer server, boolean defaultServer) {
    EbeanServer originalPrimaryServer = serverMgr.defaultServer;
    serverMgr.registerWithName(name, server, defaultServer);
    return originalPrimaryServer;
  }

  /**
   * Return the next identity value for a given bean type.
   * <p>
   * This will only work when a IdGenerator is on this bean type such as a DB
   * sequence or UUID.
   * </p>
   * <p>
   * For DB's supporting getGeneratedKeys and sequences such as Oracle10 you do
   * not need to use this method generally. It is made available for more
   * complex cases where it is useful to get an ID prior to some processing.
   * </p>
   */
  public static Object nextId(Class<?> beanType) {
    return serverMgr.getDefaultServer().nextId(beanType);
  }

  /**
   * Start a transaction with 'REQUIRED' semantics.
   * <p>
   * With REQUIRED semantics if an active transaction already exists that transaction will be used.
   * </p>
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
   * <pre>{@code
   *
   *   // start a transaction (stored in a ThreadLocal)
   *   Ebean.beginTransaction();
   *   try {
   * 	   Order order = Ebean.find(Order.class,10); ...
   *
   * 	   Ebean.save(order);
   *
   * 	   Ebean.commitTransaction();
   *
   *   } finally {
   * 	   // rollback if we didn't commit
   * 	   // i.e. an exception occurred before commitTransaction().
   * 	   Ebean.endTransaction();
   *   }
   *
   * }</pre>
   * <p>
   * If you want to externalise the transaction management then you should be
   * able to do this via EbeanServer. Specifically with EbeanServer you can pass
   * the transaction to the various find() and save() execute() methods. This
   * gives you the ability to create the transactions yourself externally from
   * Ebean and pass those transactions through to the various methods available
   * on EbeanServer.
   * </p>
   */
  public static Transaction beginTransaction() {
    return serverMgr.getDefaultServer().beginTransaction();
  }

  /**
   * Start a transaction additionally specifying the isolation level.
   *
   * @param isolation the Transaction isolation level
   */
  public static Transaction beginTransaction(TxIsolation isolation) {
    return serverMgr.getDefaultServer().beginTransaction(isolation);
  }

  /**
   * Start a transaction typically specifying REQUIRES_NEW or REQUIRED semantics.
   * <p>
   * Note that this provides an try finally alternative to using {@link #executeCall(TxScope, Callable)} or
   * {@link #execute(TxScope, Runnable)}.
   * </p>
   * <p>
   * <h3>REQUIRES_NEW example:</h3>
   * <pre>{@code
   * // Start a new transaction. If there is a current transaction
   * // suspend it until this transaction ends
   * Transaction txn = Ebean.beginTransaction(TxScope.requiresNew());
   * try {
   *
   *   ...
   *
   *   // commit the transaction
   *   txn.commit();
   *
   * } finally {
   *   // end this transaction which:
   *   //  A) will rollback transaction if it has not been committed already
   *   //  B) will restore a previously suspended transaction
   *   txn.end();
   * }
   *
   * }</pre>
   * <h3>REQUIRED example:</h3>
   * <pre>{@code
   *
   * // start a new transaction if there is not a current transaction
   * Transaction txn = Ebean.beginTransaction(TxScope.required());
   * try {
   *
   *   ...
   *
   *   // commit the transaction if it was created or
   *   // do nothing if there was already a current transaction
   *   txn.commit();
   *
   * } finally {
   *   // end this transaction which will rollback the transaction
   *   // if it was created for this try finally scope and has not
   *   // already been committed
   *   txn.end();
   * }
   *
   * }</pre>
   */
  public static Transaction beginTransaction(TxScope scope) {
    return serverMgr.getDefaultServer().beginTransaction(scope);
  }

  /**
   * Returns the current transaction or null if there is no current transaction
   * in scope.
   */
  public static Transaction currentTransaction() {
    return serverMgr.getDefaultServer().currentTransaction();
  }

  /**
   * The batch will be flushing automatically but you can use this to explicitly
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
   * <p/>
   * If there is no currently active transaction then a PersistenceException is thrown.
   *
   * @param transactionCallback the transaction callback to be registered with the current transaction
   * @throws PersistenceException if there is no currently active transaction
   */
  public static void register(TransactionCallback transactionCallback) throws PersistenceException {
    serverMgr.getDefaultServer().register(transactionCallback);
  }

  /**
   * Commit the current transaction.
   */
  public static void commitTransaction() {
    serverMgr.getDefaultServer().commitTransaction();
  }

  /**
   * Rollback the current transaction.
   */
  public static void rollbackTransaction() {
    serverMgr.getDefaultServer().rollbackTransaction();
  }

  /**
   * If the current transaction has already been committed do nothing otherwise
   * rollback the transaction.
   * <p>
   * Useful to put in a finally block to ensure the transaction is ended, rather
   * than a rollbackTransaction() in each catch block.
   * </p>
   * <p>
   * Code example:
   * </p>
   * <pre>{@code
   *   Ebean.beginTransaction();
   *   try {
   *     // do some fetching and or persisting
   *
   *     // commit at the end
   *     Ebean.commitTransaction();
   *
   *   } finally {
   *     // if commit didn't occur then rollback the transaction
   *     Ebean.endTransaction();
   *   }
   * }</pre>
   */
  public static void endTransaction() {
    serverMgr.getDefaultServer().endTransaction();
  }

  /**
   * Mark the current transaction as rollback only.
   */
  public static void setRollbackOnly() {
    serverMgr.getDefaultServer().currentTransaction().setRollbackOnly();
  }

  /**
   * Return a map of the differences between two objects of the same type.
   * <p>
   * When null is passed in for b, then the 'OldValues' of a is used for the
   * difference comparison.
   * </p>
   */
  public static Map<String, ValuePair> diff(Object a, Object b) {
    return serverMgr.getDefaultServer().diff(a, b);
  }

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
   * <pre>{@code
   *   public class Order { ...
   *
   * 	   @OneToMany(cascade=CascadeType.ALL, mappedBy="order")
   * 	   List<OrderDetail> details;
   * 	   ...
   *   }
   * }</pre>
   * <p>
   * When a save cascades via a OneToMany or ManyToMany Ebean will automatically
   * set the 'parent' object to the 'detail' object. In the example below in
   * saving the order and cascade saving the order details the 'parent' order
   * will be set against each order detail when it is saved.
   * </p>
   */
  public static void save(Object bean) throws OptimisticLockException {
    serverMgr.getDefaultServer().save(bean);
  }

  /**
   * Insert the bean. This is useful when you set the Id property on a bean and
   * want to explicitly insert it.
   */
  public static void insert(Object bean) {
    serverMgr.getDefaultServer().insert(bean);
  }

  /**
   * Insert a collection of beans.
   */
  public static void insertAll(Collection<?> beans) {
    serverMgr.getDefaultServer().insertAll(beans);
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
   *   Customer customer = Ebean.find(Customer, id);
   *
   *   // mark the bean as dirty so that a save() or update() will
   *   // increment the version property
   *   Ebean.markAsDirty(customer);
   *   Ebean.save(customer);
   *
   * }</pre>
   */
  public static void markAsDirty(Object bean) throws OptimisticLockException {
    serverMgr.getDefaultServer().markAsDirty(bean);
  }

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
   * <pre>{@code
   *
   *   // A 'stateless update' example
   *   Customer customer = new Customer();
   *   customer.setId(7);
   *   customer.setName("ModifiedNameNoOCC");
   *   ebeanServer.update(customer);
   *
   * }</pre>
   *
   * @see ServerConfig#setUpdatesDeleteMissingChildren(boolean)
   * @see ServerConfig#setUpdateChangesOnly(boolean)
   */
  public static void update(Object bean) throws OptimisticLockException {
    serverMgr.getDefaultServer().update(bean);
  }

  /**
   * Update the beans in the collection.
   */
  public static void updateAll(Collection<?> beans) throws OptimisticLockException {
    serverMgr.getDefaultServer().updateAll(beans);
  }

  /**
   * Merge the bean using the default merge options.
   *
   * @param bean The bean to merge
   */
  public static void merge(Object bean) {
    serverMgr.getDefaultServer().merge(bean);
  }

  /**
   * Merge the bean using the given merge options.
   *
   * @param bean    The bean to merge
   * @param options The options to control the merge
   */
  public static void merge(Object bean, MergeOptions options) {
    serverMgr.getDefaultServer().merge(bean, options);
  }

  /**
   * Save all the beans from a Collection.
   */
  public static int saveAll(Collection<?> beans) throws OptimisticLockException {
    return serverMgr.getDefaultServer().saveAll(beans);
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
   *   Set<Property> properties = server().checkUniqueness(doc);
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
  @Nonnull
  public static Set<Property> checkUniqueness(Object bean) {
    return serverMgr.getDefaultServer().checkUniqueness(bean);
  }

  /**
   * Same as {@link #checkUniqueness(Object)}. but with given transaction.
   */
  @Nonnull
  public static Set<Property> checkUniqueness(Object bean, Transaction transaction) {
    return serverMgr.getDefaultServer().checkUniqueness(bean, transaction);
  }

  /**
   * Delete the bean.
   * <p>
   * This will return true if the bean was deleted successfully or JDBC batch is being used.
   * </p>
   * <p>
   * If there is no current transaction one will be created and committed for
   * you automatically.
   * </p>
   * <p>
   * If the bean is configured with <code>@SoftDelete</code> then this will perform a soft
   * delete rather than a hard/permanent delete.
   * </p>
   * <p>
   * If the Bean does not have a version property (or loaded version property) and
   * the bean does not exist then this returns false indicating that nothing was
   * deleted. Note that, if JDBC batch mode is used then this always returns true.
   * </p>
   */
  public static boolean delete(Object bean) throws OptimisticLockException {
    return serverMgr.getDefaultServer().delete(bean);
  }

  /**
   * Delete the bean in permanent fashion (will not use soft delete).
   */
  public static boolean deletePermanent(Object bean) throws OptimisticLockException {
    return serverMgr.getDefaultServer().deletePermanent(bean);
  }

  /**
   * Delete the bean given its type and id.
   */
  public static int delete(Class<?> beanType, Object id) {
    return serverMgr.getDefaultServer().delete(beanType, id);
  }

  /**
   * Delete permanent the bean given its type and id.
   */
  public static int deletePermanent(Class<?> beanType, Object id) {
    return serverMgr.getDefaultServer().deletePermanent(beanType, id);
  }

  /**
   * Delete several beans given their type and id values.
   */
  public static int deleteAll(Class<?> beanType, Collection<?> ids) {
    return serverMgr.getDefaultServer().deleteAll(beanType, ids);
  }

  /**
   * Delete permanent several beans given their type and id values.
   */
  public static int deleteAllPermanent(Class<?> beanType, Collection<?> ids) {
    return serverMgr.getDefaultServer().deleteAllPermanent(beanType, ids);
  }

  /**
   * Delete all the beans in the Collection.
   */
  public static int deleteAll(Collection<?> beans) throws OptimisticLockException {
    return serverMgr.getDefaultServer().deleteAll(beans);
  }

  /**
   * Delete permanent all the beans in the Collection (will not use soft delete).
   */
  public static int deleteAllPermanent(Collection<?> beans) throws OptimisticLockException {
    return serverMgr.getDefaultServer().deleteAllPermanent(beans);
  }

  /**
   * Refresh the values of a bean.
   * <p>
   * Note that this resets OneToMany and ManyToMany properties so that if they
   * are accessed a lazy load will refresh the many property.
   * </p>
   */
  public static void refresh(Object bean) {
    serverMgr.getDefaultServer().refresh(bean);
  }

  /**
   * Refresh a 'many' property of a bean.
   * <pre>{@code
   *
   *   Order order = ...;
   *   ...
   *   // refresh the order details...
   *   Ebean.refreshMany(order, "details");
   *
   * }</pre>
   *
   * @param bean             the entity bean containing the List Set or Map to refresh.
   * @param manyPropertyName the property name of the List Set or Map to refresh.
   */
  public static void refreshMany(Object bean, String manyPropertyName) {
    serverMgr.getDefaultServer().refreshMany(bean, manyPropertyName);
  }

  /**
   * Get a reference object.
   * <p>
   * This is sometimes described as a proxy (with lazy loading).
   * </p>
   * <pre>{@code
   *
   *   Product product = Ebean.getReference(Product.class, 1);
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
  public static <T> T getReference(Class<T> beanType, Object id) {
    return serverMgr.getDefaultServer().getReference(beanType, id);
  }

  /**
   * Sort the list using the sortByClause which can contain a comma delimited
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
   * <pre>{@code
   *
   *   // find orders and their customers
   *   List<Order> list = Ebean.find(Order.class)
   *     .fetch("customer")
   *     .orderBy("id")
   *     .findList();
   *
   *   // sort by customer name ascending, then by order shipDate
   *   // ... then by the order status descending
   *   Ebean.sort(list, "customer.name, shipDate, status desc");
   *
   *   // sort by customer name descending (with nulls low)
   *   // ... then by the order id
   *   Ebean.sort(list, "customer.name desc nullsLow, id");
   *
   * }</pre>
   *
   * @param list         the list of entity beans
   * @param sortByClause the properties to sort the list by
   */
  public static <T> void sort(List<T> list, String sortByClause) {
    serverMgr.getDefaultServer().sort(list, sortByClause);
  }

  /**
   * Find a bean using its unique id. This will not use caching.
   * <pre>{@code
   *
   *   // Fetch order 1
   *   Order order = Ebean.find(Order.class, 1);
   *
   * }</pre>
   * <p>
   * If you want more control over the query then you can use createQuery() and
   * Query.findOne();
   * </p>
   * <pre>{@code
   *
   *   // ... additionally fetching customer, customer shipping address,
   *   // order details, and the product associated with each order detail.
   *   // note: only product id and name is fetch (its a "partial object").
   *   // note: all other objects use "*" and have all their properties fetched.
   *
   *   Query<Order> query = Ebean.find(Order.class)
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
    return serverMgr.getDefaultServer().find(beanType, id);
  }

  /**
   * Create a SqlQuery for executing native sql
   * query statements.
   * <p>
   * Note that you can use raw SQL with entity beans, refer to the SqlSelect
   * annotation for examples.
   * </p>
   */
  public static SqlQuery createSqlQuery(String sql) {
    return serverMgr.getDefaultServer().createSqlQuery(sql);
  }

  /**
   * Create a sql update for executing native dml statements.
   * <p>
   * Use this to execute a Insert Update or Delete statement. The statement will
   * be native to the database and contain database table and column names.
   * </p>
   * <p>
   * See {@link SqlUpdate} for example usage.
   * </p>
   */
  public static SqlUpdate createSqlUpdate(String sql) {
    return serverMgr.getDefaultServer().createSqlUpdate(sql);
  }

  /**
   * Create a CallableSql to execute a given stored procedure.
   *
   * @see CallableSql
   */
  public static CallableSql createCallableSql(String sql) {
    return serverMgr.getDefaultServer().createCallableSql(sql);
  }

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
   * <pre>{@code
   *
   *   // The bean name and properties - "topic","postCount" and "id"
   *
   *   // will be converted into their associated table and column names
   *   String updStatement = "update topic set postCount = :pc where id = :id";
   *
   *   Update<Topic> update = Ebean.createUpdate(Topic.class, updStatement);
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

    return serverMgr.getDefaultServer().createUpdate(beanType, ormUpdate);
  }

  /**
   * Create a CsvReader for a given beanType.
   */
  public static <T> CsvReader<T> createCsvReader(Class<T> beanType) {

    return serverMgr.getDefaultServer().createCsvReader(beanType);
  }

  /**
   * Create a named query.
   * <p>
   * For RawSql the named query is expected to be in ebean.xml.
   * </p>
   *
   * @param beanType   The type of entity bean
   * @param namedQuery The name of the query
   * @param <T>        The type of entity bean
   * @return The query
   */
  public static <T> Query<T> createNamedQuery(Class<T> beanType, String namedQuery) {
    return serverMgr.getDefaultServer().createNamedQuery(beanType, namedQuery);
  }

  /**
   * Create a query for a type of entity bean.
   * <p>
   * You can use the methods on the Query object to specify fetch paths,
   * predicates, order by, limits etc.
   * </p>
   * <p>
   * You then use findList(), findSet(), findMap() and findOne() to execute
   * the query and return the collection or bean.
   * </p>
   * <p>
   * Note that a query executed by {@link Query#findList()}
   * {@link Query#findSet()} etc will execute against the same EbeanServer from
   * which is was created.
   * </p>
   *
   * @param beanType the class of entity to be fetched
   * @return A ORM Query object for this beanType
   */
  public static <T> Query<T> createQuery(Class<T> beanType) {

    return serverMgr.getDefaultServer().createQuery(beanType);
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
   *   Query<Order> query = Ebean.createQuery(Order.class, eql);
   *   query.setParameter("orderId", 2);
   *
   *   Order order = query.findOne();
   *
   *   // This is the same as:
   *
   *   Order order = Ebean.find(Order.class)
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

    return serverMgr.getDefaultServer().createQuery(beanType, eql);
  }

  /**
   * Create a query for a type of entity bean.
   * <p>
   * This is actually the same as {@link #createQuery(Class)}. The reason it
   * exists is that people used to JPA will probably be looking for a
   * createQuery method (the same as entityManager).
   * </p>
   *
   * @param beanType the type of entity bean to find
   * @return A ORM Query object for this beanType
   */
  public static <T> Query<T> find(Class<T> beanType) {

    return serverMgr.getDefaultServer().find(beanType);
  }

  /**
   * Create a query using native SQL.
   * <p>
   * The native SQL can contain named parameters or positioned parameters.
   * </p>
   * <pre>{@code
   *
   *   String sql = "select c.id, c.name from customer c where c.name like ? order by c.name";
   *
   *   Query<Customer> query = ebeanServer.findNative(Customer.class, sql);
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
    return serverMgr.getDefaultServer().findNative(beanType, nativeSql);
  }

  /**
   * Create a Query for DTO beans.
   * <p>
   * DTO beans are just normal bean like classes with public constructor(s) and setters.
   * They do not need to be registered with Ebean before use.
   * </p>
   *
   * @param dtoType The type of the DTO bean the rows will be mapped into.
   * @param sql     The SQL query to execute.
   * @param <T>     The type of the DTO bean.
   */
  public static <T> DtoQuery<T> findDto(Class<T> dtoType, String sql) {
    return serverMgr.getDefaultServer().findDto(dtoType, sql);
  }

  /**
   * Create an Update query to perform a bulk update.
   * <p>
   * <pre>{@code
   *
   *  int rows = Ebean.update(Customer.class)
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
    return serverMgr.getDefaultServer().update(beanType);
  }

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
  public static <T> Filter<T> filter(Class<T> beanType) {
    return serverMgr.getDefaultServer().filter(beanType);
  }

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
   * <pre>{@code
   *
   *   // example that uses 'named' parameters
   *   String s = "UPDATE f_topic set post_count = :count where id = :id"
   *
   *   SqlUpdate update = Ebean.createSqlUpdate(s);
   *
   *   update.setParameter("id", 1);
   *   update.setParameter("count", 50);
   *
   *   int modifiedCount = Ebean.execute(update);
   *
   *   String msg = "There where " + modifiedCount + "rows updated";
   *
   * }</pre>
   *
   * @param sqlUpdate the update sql potentially with bind values
   * @return the number of rows updated or deleted. -1 if executed in batch.
   * @see SqlUpdate
   * @see CallableSql
   * @see Ebean#execute(CallableSql)
   */
  public static int execute(SqlUpdate sqlUpdate) {
    return serverMgr.getDefaultServer().execute(sqlUpdate);
  }

  /**
   * For making calls to stored procedures.
   * <p>
   * Example:
   * </p>
   * <pre>{@code
   *
   *   String sql = "{call sp_order_modify(?,?,?)}";
   *
   *   CallableSql cs = Ebean.createCallableSql(sql);
   *   cs.setParameter(1, 27);
   *   cs.setParameter(2, "SHIPPED");
   *   cs.registerOut(3, Types.INTEGER);
   *
   *   Ebean.execute(cs);
   *
   *   // read the out parameter
   *   Integer returnValue = (Integer) cs.getObject(3);
   *
   * }</pre>
   *
   * @see CallableSql
   * @see Ebean#execute(SqlUpdate)
   */
  public static int execute(CallableSql callableSql) {
    return serverMgr.getDefaultServer().execute(callableSql);
  }

  /**
   * Execute a TxRunnable in a Transaction with an explicit scope.
   * <p>
   * The scope can control the transaction type, isolation and rollback
   * semantics.
   * </p>
   * <pre>{@code
   *
   *   // set specific transactional scope settings
   *   TxScope scope = TxScope.requiresNew().setIsolation(TxIsolation.SERIALIZABLE);
   *
   *   Ebean.execute(scope, new TxRunnable() {
   * 	   public void run() {
   * 		   User u1 = Ebean.find(User.class, 1);
   * 		   ...
   * 	   }
   *   });
   *
   * }</pre>
   */
  public static void execute(TxScope scope, Runnable r) {
    serverMgr.getDefaultServer().execute(scope, r);
  }

  /**
   * Execute a Runnable in a Transaction with the default scope.
   * <p>
   * The default scope runs with REQUIRED and by default will rollback on any
   * exception (checked or runtime).
   * </p>
   * <pre>{@code
   *
   *   Ebean.execute(() -> {
   *
   *       User u1 = Ebean.find(User.class, 1);
   *       User u2 = Ebean.find(User.class, 2);
   *
   *       u1.setName("u1 mod");
   *       u2.setName("u2 mod");
   *
   *       Ebean.save(u1);
   *       Ebean.save(u2);
   *
   *   });
   *
   * }</pre>
   */
  public static void execute(Runnable r) {
    serverMgr.getDefaultServer().execute(r);
  }

  /**
   * Execute a Callable in a Transaction with an explicit scope.
   * <p>
   * The scope can control the transaction type, isolation and rollback
   * semantics.
   * </p>
   * <pre>{@code
   *
   *   // set specific transactional scope settings
   *   TxScope scope = TxScope.requiresNew().setIsolation(TxIsolation.SERIALIZABLE);
   *
   *   Ebean.executeCall(scope, new Callable<String>() {
   * 	   public String call() {
   * 		   User u1 = Ebean.find(User.class, 1);
   * 		   ...
   * 		   return u1.getEmail();
   * 	   }
   *   });
   *
   * }</pre>
   */
  public static <T> T executeCall(TxScope scope, Callable<T> c) {
    return serverMgr.getDefaultServer().executeCall(scope, c);
  }

  /**
   * Execute a Callable in a Transaction with the default scope.
   * <p>
   * The default scope runs with REQUIRED and by default will rollback on any
   * exception (checked or runtime).
   * </p>
   * <p>
   * This is basically the same as TxRunnable except that it returns an Object
   * (and you specify the return type via generics).
   * </p>
   * <pre>{@code
   *
   *   Ebean.executeCall(() -> {
   *
   *       User u1 = Ebean.find(User.class, 1);
   *       User u2 = Ebean.find(User.class, 2);
   *
   *       u1.setName("u1 mod");
   *       u2.setName("u2 mod");
   *
   *       Ebean.save(u1);
   *       Ebean.save(u2);
   *
   *       return u1.getEmail();
   *
   *   });
   *
   * }</pre>
   */
  public static <T> T executeCall(Callable<T> c) {
    return serverMgr.getDefaultServer().executeCall(c);
  }

  /**
   * Inform Ebean that tables have been modified externally. These could be the
   * result of from calling a stored procedure, other JDBC calls or external
   * programs including other frameworks.
   * <p>
   * If you use Ebean.execute(UpdateSql) then the table modification information
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
   * @param tableName the name of the table that was modified
   * @param inserts   true if rows where inserted into the table
   * @param updates   true if rows on the table where updated
   * @param deletes   true if rows on the table where deleted
   */
  public static void externalModification(String tableName, boolean inserts, boolean updates, boolean deletes) {

    serverMgr.getDefaultServer().externalModification(tableName, inserts, updates, deletes);
  }

  /**
   * Return the BeanState for a given entity bean.
   * <p>
   * This will return null if the bean is not an enhanced entity bean.
   * </p>
   */
  public static BeanState getBeanState(Object bean) {
    return serverMgr.getDefaultServer().getBeanState(bean);
  }

  /**
   * Return the manager of the server cache ("L2" cache).
   */
  public static ServerCacheManager getServerCacheManager() {
    return serverMgr.getDefaultServer().getServerCacheManager();
  }

  /**
   * Return the BackgroundExecutor service for asynchronous processing of
   * queries.
   */
  public static BackgroundExecutor getBackgroundExecutor() {
    return serverMgr.getDefaultServer().getBackgroundExecutor();
  }

  /**
   * Return the JsonContext for reading/writing JSON.
   */
  public static JsonContext json() {
    return serverMgr.getDefaultServer().json();
  }

}

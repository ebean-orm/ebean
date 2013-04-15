package com.avaje.ebean;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.OptimisticLockException;

import com.avaje.ebean.annotation.CacheStrategy;
import com.avaje.ebean.cache.ServerCacheManager;
import com.avaje.ebean.config.ServerConfig;
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
 * <pre class="code">
 * // Get access to the Human Resources EbeanServer/Database
 * EbeanServer hrServer = Ebean.getServer(&quot;HR&quot;);
 * 
 * 
 * // fetch contact 3 from the HR database Contact contact =
 * hrServer.find(Contact.class, new Integer(3));
 * 
 * contact.setStatus(&quot;INACTIVE&quot;); ...
 * 
 * // save the contact back to the HR database hrServer.save(contact);
 * </pre>
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
   * Return the BeanState for a given entity bean.
   * <p>
   * This will return null if the bean is not an enhanced (or subclassed) entity
   * bean.
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
   * Create a new instance of T that is an EntityBean (for subclassing).
   * <p>
   * Note that if you are using enhancement (rather than subclassing) then you
   * do not need to use this method and just new up a bean.
   * </p>
   * <p>
   * Potentially useful when using subclassing and you wish to programmatically
   * load a entity bean . Otherwise this method is generally not required.
   * </p>
   */
  public <T> T createEntityBean(Class<T> type);

  /**
   * Create a ObjectInputStream that can be used to deserialise "Proxy" or
   * "SubClassed" entity beans.
   * <p>
   * This is NOT required when entity beans are "Enhanced" (via java agent or
   * ant task etc).
   * </p>
   * <p>
   * The reason this is needed to deserialise "Proxy" beans is because Ebean
   * creates the "Proxy/SubClass" classes in a class loader - and generally the
   * class loader deserialising the inputStream is not aware of these other
   * classes.
   * </p>
   */
  public ObjectInputStream createProxyObjectInputStream(InputStream is);

  /**
   * Create a CsvReader for a given beanType.
   */
  public <T> CsvReader<T> createCsvReader(Class<T> beanType);

  /**
   * Create a named query for an entity bean (refer
   * {@link Ebean#createQuery(Class, String)})
   * <p>
   * The query statement will be defined in a deployment orm xml file.
   * </p>
   * 
   * @see Ebean#createQuery(Class, String)
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
   * <pre class="code">
   *  EbeanServer ebeanServer = ... ;
   *  String q = "find order fetch details where status = :st";
   *  
   *  List&lt;Order&gt; newOrders 
   *        = ebeanServer.createQuery(Order.class, q)
   *             .setParameter("st", Order.Status.NEW)
   *             .findList();
   * </pre>
   * 
   * @param query
   *          the object query
   */
  public <T> Query<T> createQuery(Class<T> beanType, String query);

  /**
   * Create a query for an entity bean (refer {@link Ebean#createQuery(Class)}
   * ).
   * 
   * @see Ebean#createQuery(Class)
   */
  public <T> Query<T> createQuery(Class<T> beanType);

  /**
   * Create a query for a type of entity bean (the same as
   * {@link EbeanServer#createQuery(Class)}).
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
   * Create a filter for filtering lists of entity beans.
   */
  public <T> Filter<T> filter(Class<T> beanType);

  /**
   * Sort the list using the sortByClause.
   * 
   * @see Ebean#sort(List, String)
   * 
   * @param list
   *          the list of entity beans
   * @param sortByClause
   *          the properties to sort the list by
   */
  public <T> void sort(List<T> list, String sortByClause);

  /**
   * Create a named update for an entity bean (refer
   * {@link Ebean#createNamedUpdate(Class, String)}).
   */
  public <T> Update<T> createNamedUpdate(Class<T> beanType, String namedUpdate);

  /**
   * Create a update for an entity bean where you will manually specify the
   * insert update or delete statement.
   */
  public <T> Update<T> createUpdate(Class<T> beanType, String ormUpdate);

  /**
   * Create a sql query for executing native sql query statements (refer
   * {@link Ebean#createSqlQuery(String)}).
   * 
   * @see Ebean#createSqlQuery(String)
   */
  public SqlQuery createSqlQuery(String sql);

  /**
   * Create a named sql query (refer {@link Ebean#createNamedSqlQuery(String)}
   * ).
   * <p>
   * The query statement will be defined in a deployment orm xml file.
   * </p>
   * 
   * @see Ebean#createNamedSqlQuery(String)
   */
  public SqlQuery createNamedSqlQuery(String namedQuery);

  /**
   * Create a sql update for executing native dml statements (refer
   * {@link Ebean#createSqlUpdate(String)}).
   * 
   * @see Ebean#createSqlUpdate(String)
   */
  public SqlUpdate createSqlUpdate(String sql);

  /**
   * Create a CallableSql to execute a given stored procedure.
   */
  public CallableSql createCallableSql(String callableSql);

  /**
   * Create a named sql update (refer {@link Ebean#createNamedSqlUpdate(String)}
   * ).
   * <p>
   * The statement (an Insert Update or Delete statement) will be defined in a
   * deployment orm xml file.
   * </p>
   * 
   * @see Ebean#createNamedSqlUpdate(String)
   */
  public SqlUpdate createNamedSqlUpdate(String namedQuery);

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
   * Start a new transaction putting it into a ThreadLocal.
   * 
   * @see Ebean#beginTransaction()
   */
  public Transaction beginTransaction();

  /**
   * Start a transaction additionally specifying the isolation level.
   */
  public Transaction beginTransaction(TxIsolation isolation);

  /**
   * Returns the current transaction or null if there is no current transaction
   * in scope.
   */
  public Transaction currentTransaction();

  /**
   * Commit the current transaction.
   * 
   * @see Ebean#commitTransaction()
   */
  public void commitTransaction();

  /**
   * Rollback the current transaction.
   * 
   * @see Ebean#rollbackTransaction()
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
   * <pre class="code">
   * Ebean.startTransaction(); try { // do some fetching
   * and or persisting
   * 
   * // commit at the end Ebean.commitTransaction();
   * 
   * } finally { // if commit didn't occur then rollback the transaction
   * Ebean.endTransaction(); }
   * </pre>
   * 
   * </p>
   * 
   * @see Ebean#endTransaction()
   */
  public void endTransaction();

  /**
   * Refresh the values of a bean.
   * <p>
   * Note that this does not refresh any OneToMany or ManyToMany properties.
   * </p>
   * 
   * @see Ebean#refresh(Object)
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
   * @see Ebean#refreshMany(Object, String)
   */
  public void refreshMany(Object bean, String propertyName);

  /**
   * Find a bean using its unique id.
   * 
   * @see Ebean#find(Class, Object)
   */
  public <T> T find(Class<T> beanType, Object uid);

  /**
   * Get a reference Object (see {@link Ebean#getReference(Class, Object)}.
   * <p>
   * This will not perform a query against the database.
   * </p>
   * 
   * @see Ebean#getReference(Class, Object)
   */
  public <T> T getReference(Class<T> beanType, Object uid);

  /**
   * Return the number of 'top level' or 'root' entities this query should
   * return.
   */
  public <T> int findRowCount(Query<T> query, Transaction transaction);

  /**
   * Return the Id values of the query as a List.
   */
  public <T> List<Object> findIds(Query<T> query, Transaction t);

  /**
   * Return a QueryIterator for the query. This is similar to findVisit in that
   * not all the result beans need to be held in memory at the same time and as
   * such is go for processing large queries.
   */
  public <T> QueryIterator<T> findIterate(Query<T> query, Transaction t);

  /**
   * Execute the query visiting the results. This is similar to findIterate in
   * that not all the result beans need to be held in memory at the same time
   * and as such is go for processing large queries.
   */
  public <T> void findVisit(Query<T> query, QueryResultVisitor<T> visitor, Transaction t);

  /**
   * Execute a query returning a list of beans.
   * <p>
   * Generally you are able to use {@link Query#findList()} rather than
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
   * @param t
   *          the transaction (can be null).
   * @return a Future object for the row count query
   */
  public <T> FutureRowCount<T> findFutureRowCount(Query<T> query, Transaction t);

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
   * @param t
   *          the transaction (can be null).
   * @return a Future object for the list of Id's
   */
  public <T> FutureIds<T> findFutureIds(Query<T> query, Transaction t);

  /**
   * Execute find list query in a background thread.
   * <p>
   * This returns a Future object which can be used to cancel, check the
   * execution status (isDone etc) and get the value (with or without a
   * timeout).
   * </p>
   * 
   * @param query
   *          the query to execute in the background
   * @param t
   *          the transaction (can be null).
   * @return a Future object for the list result of the query
   */
  public <T> FutureList<T> findFutureList(Query<T> query, Transaction t);

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
   * @param t
   *          the transaction (can be null).
   * @return a Future object for the list result of the query
   */
  public SqlFutureList findFutureList(SqlQuery query, Transaction t);

  /**
   * Find using a PagingList with explicit transaction and pageSize.
   */
  public <T> PagingList<T> findPagingList(Query<T> query, Transaction t, int pageSize);

  /**
   * Execute the query returning a set of entity beans.
   * <p>
   * Generally you are able to use {@link Query#findSet()} rather than
   * explicitly calling this method. You could use this method if you wish to
   * explicitly control the transaction used for the query.
   * </p>
   * 
   * @param <T>
   *          the type of entity bean to fetch.
   * @param query
   *          the query to execute
   * @param transaction
   *          the transaction to use (can be null).
   * @return the set of fetched beans.
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
   * @see SqlQuery#findUnique()
   */
  public SqlRow findUnique(SqlQuery query, Transaction transaction);

  /**
   * Persist the bean by either performing an insert or update.
   * 
   * @see Ebean#save(Object)
   */
  public void save(Object bean) throws OptimisticLockException;

  /**
   * Save all the beans in the iterator.
   */
  public int save(Iterator<?> it) throws OptimisticLockException;

  /**
   * Save all the beans in the collection.
   */
  public int save(Collection<?> it) throws OptimisticLockException;

  /**
   * Delete the bean.
   * 
   * @see Ebean#delete(Object)
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
  public int delete(Class<?> beanType, Object id, Transaction t);

  /**
   * Delete several beans given their type and id values.
   */
  public void delete(Class<?> beanType, Collection<?> ids);

  /**
   * Delete several beans given their type and id values with an explicit
   * transaction.
   */
  public void delete(Class<?> beanType, Collection<?> ids, Transaction t);

  /**
   * Execute a SQL Update Delete or Insert statement using the current
   * transaction. This returns the number of rows that where updated, deleted or
   * inserted.
   * <p>
   * Refer to Ebean.execute(UpdateSql) for full documentation.
   * </p>
   * 
   * @see Ebean#execute(SqlUpdate)
   */
  public int execute(SqlUpdate updSql);

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
   * Call a stored procedure.
   * <p>
   * Refer to Ebean.execute(CallableSql) for full documentation.
   * </p>
   * 
   * @see Ebean#execute(CallableSql)
   */
  public int execute(CallableSql callableSql);

  /**
   * Process committed changes from another framework.
   * <p>
   * This notifies this instance of the framework that beans have been committed
   * externally to it. Either by another framework or clustered server. It uses
   * this to maintain its cache and text indexes appropriately.
   * </p>
   * 
   * @see Ebean#externalModification(String, boolean, boolean, boolean)
   */
  public void externalModification(String tableName, boolean inserted, boolean updated,
      boolean deleted);

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
  public void save(Object bean, Transaction t) throws OptimisticLockException;

  /**
   * Save all the beans in the iterator with an explicit transaction.
   */
  public int save(Iterator<?> it, Transaction t) throws OptimisticLockException;

  /**
   * Force an update using the bean.
   * <p>
   * You can use this method to FORCE an update to occur (even on a bean that
   * has not been fetched but say built from JSON or XML). When
   * {@link EbeanServer#save(Object)} is used Ebean determines whether to use an
   * insert or an update based on the state of the bean. Using this method will
   * force an update to occur.
   * </p>
   * <p>
   * It is expected that this method is most useful in stateless REST services
   * or web applications where you have the values you wish to update but no
   * existing bean.
   * </p>
   * <p>
   * For updates against beans that have not been fetched (say built from JSON
   * or XML) this will treat deleteMissingChildren=true and will delete any
   * 'missing children'. Refer to
   * {@link EbeanServer#update(Object, Set, Transaction, boolean, boolean)}.
   * </p>
   * 
   * <pre class="code">
   * 
   * Customer c = new Customer();
   * c.setId(7);
   * c.setName(&quot;ModifiedNameNoOCC&quot;);
   * 
   * // generally you should set the version property
   * // so that Optimistic Concurrency Checking is used.
   * // If a version property is not set then no Optimistic
   * // Concurrency Checking occurs for the update
   * // c.setLastUpdate(lastUpdateTime);
   * 
   * // by default the Non-null properties
   * // are included in the update
   * ebeanServer.update(c);
   * 
   * </pre>
   */
  public void update(Object bean);

  /**
   * Force an update of the non-null properties of the bean with an explicit
   * transaction.
   * <p>
   * You can use this method to FORCE an update to occur (even on a bean that
   * has not been fetched but say built from JSON or XML). When
   * {@link EbeanServer#save(Object)} is used Ebean determines whether to use an
   * insert or an update based on the state of the bean. Using this method will
   * force an update to occur.
   * </p>
   * <p>
   * It is expected that this method is most useful in stateless REST services
   * or web applications where you have the values you wish to update but no
   * existing bean.
   * </p>
   * <p>
   * For updates against beans that have not been fetched (say built from JSON
   * or XML) this will treat deleteMissingChildren=true and will delete any
   * 'missing children'. Refer to
   * {@link EbeanServer#update(Object, Set, Transaction, boolean, boolean)}.
   * </p>
   */
  public void update(Object bean, Transaction t);

  /**
   * Force an update using the bean explicitly stating the properties to update.
   * <p>
   * You can use this method to FORCE an update to occur (even on a bean that
   * has not been fetched but say built from JSON or XML). When
   * {@link EbeanServer#save(Object)} is used Ebean determines whether to use an
   * insert or an update based on the state of the bean. Using this method will
   * force an update to occur.
   * </p>
   * <p>
   * It is expected that this method is most useful in stateless REST services
   * or web applications where you have the values you wish to update but no
   * existing bean.
   * </p>
   * <p>
   * For updates against beans that have not been fetched (say built from JSON
   * or XML) this will treat deleteMissingChildren=true and will delete any
   * 'missing children'. Refer to
   * {@link EbeanServer#update(Object, Set, Transaction, boolean, boolean)}.
   * </p>
   * 
   * <pre class="code">
   * 
   * Customer c = new Customer();
   * c.setId(7);
   * c.setName(&quot;ModifiedNameNoOCC&quot;);
   * 
   * // generally you should set the version property
   * // so that Optimistic Concurrency Checking is used.
   * // If a version property is not set then no Optimistic
   * // Concurrency Checking occurs for the update
   * // c.setLastUpdate(lastUpdateTime);
   * 
   * // by default the Non-null properties
   * // are included in the update
   * ebeanServer.update(c);
   * 
   * </pre>
   */
  public void update(Object bean, Set<String> updateProps);

  /**
   * Force an update of the specified properties of the bean with an explicit
   * transaction.
   * <p>
   * You can use this method to FORCE an update to occur (even on a bean that
   * has not been fetched but say built from JSON or XML). When
   * {@link EbeanServer#save(Object)} is used Ebean determines whether to use an
   * insert or an update based on the state of the bean. Using this method will
   * force an update to occur.
   * </p>
   * <p>
   * It is expected that this method is most useful in stateless REST services
   * or web applications where you have the values you wish to update but no
   * existing bean.
   * </p>
   * <p>
   * For updates against beans that have not been fetched (say built from JSON
   * or XML) this will treat deleteMissingChildren=true and will delete any
   * 'missing children'. Refer to
   * {@link EbeanServer#update(Object, Set, Transaction, boolean, boolean)}.
   * </p>
   */
  public void update(Object bean, Set<String> updateProps, Transaction t);

  /**
   * Force an update additionally specifying whether to 'deleteMissingChildren'
   * when the update cascades to a OneToMany or ManyToMany.
   * <p>
   * By default the deleteMissingChildren is true and it is assumed that when
   * cascade saving a O2M or M2M relationship that the relationship is 'fully
   * loaded' and any child beans that are no longer on the relationship will be
   * deleted.
   * </p>
   * <p>
   * You can use this method to FORCE an update to occur (even on a bean that
   * has not been fetched but say built from JSON or XML). When
   * {@link EbeanServer#save(Object)} is used Ebean determines whether to use an
   * insert or an update based on the state of the bean. Using this method will
   * force an update to occur.
   * </p>
   * <p>
   * It is expected that this method is most useful in stateless REST services
   * or web applications where you have the values you wish to update but no
   * existing bean.
   * </p>
   * 
   * @param bean
   *          the bean to update
   * @param updateProps
   *          optionally you can specify the properties to update (can be null).
   * @param t
   *          optionally you can specify the transaction to use (can be null).
   * @param deleteMissingChildren
   *          specify false if you do not want 'missing children' of a OneToMany
   *          or ManyToMany to be automatically deleted.
   * @param updateNullProperties
   *          specify true if by default you want properties with null values to
   *          be included in the update and false if those properties should be
   *          treated as 'unloaded' and excluded from the update. This only
   *          takes effect if the updateProps is null.
   */
  public void update(Object bean, Set<String> updateProps, Transaction t,
      boolean deleteMissingChildren, boolean updateNullProperties);

  /**
   * Force the bean to be saved with an explicit insert.
   * <p>
   * Typically you would use save() and let Ebean determine if the bean should
   * be inserted or updated. This can be useful when you are transferring data
   * between databases and want to explicitly insert a bean into a different
   * database that it came from.
   * </p>
   */
  public void insert(Object bean);

  /**
   * Force the bean to be saved with an explicit insert.
   * <p>
   * Typically you would use save() and let Ebean determine if the bean should
   * be inserted or updated. This can be useful when you are transferring data
   * between databases and want to explicitly insert a bean into a different
   * database that it came from.
   * </p>
   */
  public void insert(Object bean, Transaction t);

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
   */
  public void execute(TxScope scope, TxRunnable r);

  /**
   * Execute a TxRunnable in a Transaction with the default scope.
   * <p>
   * The default scope runs with REQUIRED and by default will rollback on any
   * exception (checked or runtime).
   * </p>
   */
  public void execute(TxRunnable r);

  /**
   * Execute a TxCallable in a Transaction with an explicit scope.
   * <p>
   * The scope can control the transaction type, isolation and rollback
   * semantics.
   * </p>
   */
  public <T> T execute(TxScope scope, TxCallable<T> c);

  /**
   * Execute a TxCallable in a Transaction with the default scope.
   * <p>
   * The default scope runs with REQUIRED and by default will rollback on any
   * exception (checked or runtime).
   * </p>
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
   * Create a JsonContext that will use the default configuration options.
   */
  public JsonContext createJsonContext();

}

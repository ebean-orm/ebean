package com.avaje.ebean;

import com.avaje.ebean.text.PathProperties;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.MappedSuperclass;

/**
 * A MappedSuperclass base class that provides convenience methods for inserting, updating and
 * deleting beans.
 * 
 * <p>
 * By having your entity beans extend this it provides a 'Active Record' style programming model for
 * Ebean users.
 * 
 * <p>
 * Note that there is a avaje-ebeanorm-mocker project that enables you to use Mockito or similar
 * tools to still mock out the underlying 'default EbeanServer' for testing purposes.
 * 
 * <p>
 * You may choose not use this Model mapped superclass if you don't like the 'Active Record' style
 * or if you believe it 'pollutes' your entity beans.
 *
 * <p>
 * You can use Dependency Injection like Guice or Spring to construct and wire a EbeanServer instance
 * and have that same instance used with this Model and Finder. The way that works is that when the
 * DI container creates the EbeanServer instance it can be registered with the Ebean singleton. In this
 * way the EbeanServer instance can be injected as per normal Guice / Spring dependency injection and
 * that same instance also used to support the Model and Finder active record style.
 *
 * <p>
 * If you choose to use the Model mapped superclass you will probably also chose to additionally add
 * a {@link Finder} as a public static field to complete the active record pattern and provide a
 * relatively nice clean way to write queries.
 *
 * <h3>Typical common @MappedSuperclass</h3>
 * <pre>{@code
 *
 *     // Typically there is a common base model that has some
 *     // common properties like the ones below
 *
 *     @MappedSuperclass
 *     public class BaseModel extends Model {
 *
 *       @Id Long id;
 *
 *       @Version Long version;
 *
 *       @CreatedTimestamp Timestamp whenCreated;
 *
 *       @UpdatedTimestamp Timestamp whenUpdated;
 *
 *       ...
 *
 * }</pre>
 *
 * <h3>Extend the Model</h3>
 * <pre>{@code
 *
 *     // Extend the mappedSuperclass
 *
 *     @Entity @Table(name="oto_account")
 *     public class Account extends BaseModel {
 *
 *       // add a static Finder
 *       // ... with Long being the type of our ID property ...
 *
 *       public static final Finder<Long,Account> find =
 *             new Finder<Long,Account>(Long.class, Account.class);
 *
 *       String name;
 *
 *       @OneToOne(mappedBy = "account",optional = true)
 *       User user;
 *
 *       ...
 *     }
 *
 * }</pre>
 *
 * <h3>Modal: save()</h3>
 * <pre>{@code
 *
 *     // Active record style ... save(), delete() etc
 *     Account account = new Account();
 *     account.setName("AC234");
 *
 *     // save() method inherited from Model
 *     account.save();
 *
 * }</pre>
 *
 * <h3>Finder: find byId</h3>
 * <pre>{@code
 *
 *     // find byId
 *     Account account = Account.find.byId(42);
 *
 * }</pre>
 *
 * <h3>Finder: find where</h3>
 * <pre>{@code
 *
 *     // find where ...
 *     List<Account> accounts =
 *         Account.find
 *         .where().gt("startDate", lastMonth)
 *         .findList();
 *
 * }</pre>
 */
@MappedSuperclass
public abstract class Model {

  /**
   * Return the underlying 'default' EbeanServer.
   * 
   * <p>
   * This provides full access to the API such as explicit transaction demarcation etc.
   * 
   * <p>
   * Example:
   * <pre class="code">
   * Transaction transaction = Customer.db().beginTransaction();
   * try {
   * 
   *   // turn off cascade persist for this transaction
   *   transaction.setPersistCascade(false);
   * 
   *   // extra control over jdbc batching for this transaction
   *   transaction.setBatchGetGeneratedKeys(false);
   *   transaction.setBatchMode(true);
   *   transaction.setBatchSize(20);
   * 
   *   Customer customer = new Customer();
   *   customer.setName(&quot;Roberto&quot;);
   *   customer.save();
   * 
   *   Customer otherCustomer = new Customer();
   *   otherCustomer.setName(&quot;Franko&quot;);
   *   otherCustomer.save();
   * 
   *   transaction.commit();
   * 
   * } finally {
   *   transaction.end();
   * }
   * 
   * </pre>
   */
  public static EbeanServer db() {
    return Ebean.getServer(null);
  }

  /**
   * Return a named EbeanServer that is typically different to the default server.
   * 
   * <p>
   * If you are using multiple databases then each database has a name and maps to a single
   * EbeanServer. You can use this method to get an EbeanServer for another database.
   * 
   * @param server
   *          The name of the EbeanServer. If this is null then the default EbeanServer is returned.
   */
  public static EbeanServer db(String server) {
    return Ebean.getServer(server);
  }

  /**
   * Marks the entity bean as dirty.
   * <p>
   * This is used so that when a bean that is otherwise unmodified is updated the version
   * property is updated.
   * <p>
   * An unmodified bean that is saved or updated is normally skipped and this marks the bean as
   * dirty so that it is not skipped.
   * 
   * <pre class="code">
   * 
   * Customer customer = Customer.find.byId(id);
   * 
   * // mark the bean as dirty so that a save() or update() will
   * // increment the version property
   * customer.markAsDirty();
   * customer.save();
   * 
   * </pre>
   *
   * @see EbeanServer#markAsDirty(Object)
   */
  public void markAsDirty() {
    db().markAsDirty(this);
  }
  
  /**
   * Insert or update this entity depending on its state.
   * 
   * <p>
   * Ebean will detect if this is a new bean or a previously fetched bean and perform either an
   * insert or an update based on that.
   *
   * @see EbeanServer#save(Object)
   */
  public void save() {
    db().save(this);
  }

  /**
   * Update this entity.
   *
   * @see EbeanServer#update(Object)
   */
  public void update() {
    db().update(this);
  }

  /**
   * Insert this entity.
   *
   * @see EbeanServer#insert(Object)
   */
  public void insert() {
    db().insert(this);
  }

  /**
   * Delete this entity.
   *
   * @see EbeanServer#delete(Object)
   */
  public void delete() {
    db().delete(this);
  }

  /**
   * Perform an update using this entity against the specified server.
   */
  public void update(String server) {
    db(server).update(this);
  }

  /**
   * Perform an insert using this entity against the specified server.
   */
  public void insert(String server) {
    db(server).insert(this);
  }

  /**
   * Perform a delete using this entity against the specified server.
   */
  public void delete(String server) {
    db(server).delete(this);
  }

  /**
   * Refreshes this entity from the database.
   *
   * @see EbeanServer#refresh(Object)
   */
  public void refresh() {
    db().refresh(this);
  }

  /**
   * Helper object for performing queries.
   * 
   * <p>
   * Typically a Finder is defined as a public static field on an entity bean class to provide a
   * nice way to write queries.
   * 
   * @param <I>
   *          The Id type. This is most often a {@link Long} but is also often a {@link UUID} or
   *          {@link String}.
   *
   * @param <T>
   *          The bean type
   */
  public static class Finder<I, T> {

    private final Class<I> idType;
    private final Class<T> type;
    private final String serverName;

    /**
     * Creates a finder for entity of type <code>T</code> with ID of type <code>I</code>.
     * 
     * <p>
     * Typically you use this constructor to have a static "find" field on each entity bean.
     */
    public Finder(Class<I> idType, Class<T> type) {
      this(null, idType, type);
    }

    /**
     * Creates a finder for entity of type <code>T</code> with ID of type <code>I</code>, using a
     * specific EbeanServer.
     * 
     * <p>
     * Typically you don't need to use this method.
     */
    public Finder(String serverName, Class<I> idType, Class<T> type) {
      this.type = type;
      this.idType = idType;
      this.serverName = serverName;
    }

    /**
     * Return the underlying 'default' EbeanServer.
     * 
     * <p>
     * This provides full access to the API such as explicit transaction demarcation etc.
     * 
     */
    public EbeanServer db() {
      return Ebean.getServer(serverName);
    }

    /**
     * Return typically a different EbeanServer to the default.
     * <p>
     * This is equivilent to {@link Ebean#getServer(String)}
     * 
     * @param server
     *          The name of the EbeanServer. If this is null then the default EbeanServer is
     *          returned.
     */
    public EbeanServer db(String server) {
      return Ebean.getServer(server);
    }

    /**
     * Creates a Finder for the named EbeanServer.
     * 
     * <p>
     * Create and return a new Finder for a different server.
     */
    public Finder<I, T> on(String server) {
      return new Finder<I, T>(server, idType, type);
    }

    /**
     * Delete a bean by Id.
     * <p>
     * Equivalent to {@link EbeanServer#delete(Class, Object)}
     */
    public void deleteById(I id) {
      db().delete(type, id);
    }

    /**
     * Retrieves all entities of the given type.
     * 
     * <p>
     * This is the same as (synonym for) {@link #findList()}
     */
    public List<T> all() {
      return findList();
    }

    /**
     * Retrieves an entity by ID.
     * 
     * <p>
     * Equivalent to {@link EbeanServer#find(Class, Object)}
     */
    public T byId(I id) {
      return db().find(type, id);
    }

    /**
     * Creates an entity reference for this ID.
     * 
     * <p>
     * Equivalent to {@link EbeanServer#getReference(Class, Object)}
     */
    public T ref(I id) {
      return db().getReference(type, id);
    }

    /**
     * Creates a filter for sorting and filtering lists of entities locally without going back to
     * the database.
     * <p>
     * Equivalent to {@link EbeanServer#filter(Class)}
     */
    public Filter<T> filter() {
      return db().filter(type);
    }

    /**
     * Creates a query.
     * <p>
     * Equivalent to {@link EbeanServer#find(Class)}
     */
    public Query<T> query() {
      return db().find(type);
    }

    /**
     * Creates a query applying the path properties to set the select and fetch clauses.
     * <p>
     * Equivalent to {@link Query#apply(com.avaje.ebean.text.PathProperties)}
     */
    public Query<T> apply(PathProperties pathProperties) {
      return db().find(type).apply(pathProperties);
    }

    /**
     * Returns the next identity value.
     * 
     * @see EbeanServer#nextId(Class)
     */
    @SuppressWarnings("unchecked")
    public I nextId() {
      return (I) db().nextId(type);
    }

    /**
     * Executes a query and returns the results as a list of IDs.
     * <p>
     * Equivalent to {@link Query#findIds()}
     */
    public List<Object> findIds() {
      return query().findIds();
    }

    /**
     * Execute the query consuming each bean one at a time.
     * <p>
     * This is generally used to process large queries where unlike findList
     * you do not want to hold all the results in memory at once but instead
     * process them one at a time (requiring far less memory).
     * </p>
     * Equivalent to {@link Query#findEach(QueryEachConsumer)}
     */
    public void findEach(QueryEachConsumer<T> consumer) {
      query().findEach(consumer);
    }

    /**
     * Execute the query consuming each bean one at a time.
     * <p>
     * Equivalent to {@link Query#findEachWhile(QueryEachWhileConsumer)}
     * <p>
     * This is similar to #findEach except that you return boolean
     * true to continue processing beans and return false to stop
     * processing early.
     * </p>
     * <p>
     * This is generally used to process large queries where unlike findList
     * you do not want to hold all the results in memory at once but instead
     * process them one at a time (requiring far less memory).
     * </p>
     * Equivalent to {@link Query#findEachWhile(QueryEachWhileConsumer)}
     */
    public void findEachWhile(QueryEachWhileConsumer<T> consumer) {
      query().findEachWhile(consumer);
    }

    /**
     * Retrieves all entities of the given type.
     * <p>
     * The same as {@link #all()}
     * <p>
     * Equivalent to {@link Query#findList()}
     */
    public List<T> findList() {
      return query().findList();
    }

    /**
     * Returns all the entities of the given type as a set.
     * <p>
     * Equivalent to {@link Query#findSet()}
     */
    public Set<T> findSet() {
      return query().findSet();
    }

    /**
     * Retrieves all entities of the given type as a map of objects.
     * <p>
     * Equivalent to {@link Query#findMap()}
     */
    public Map<?, T> findMap() {
      return query().findMap();
    }

    /**
     * Executes the query and returns the results as a map of the objects specifying the map key
     * property.
     * <p>
     * Equivalent to {@link Query#findMap(String, Class)}
     */
    public <K> Map<K, T> findMap(String keyProperty, Class<K> keyType) {
      return query().findMap(keyProperty, keyType);
    }

    /**
     * Return a PagedList of all entities of the given type (use where() to specify predicates as
     * needed).
     * <p>
     * Equivalent to {@link Query#findPagedList(int, int)}
     */
    public PagedList<T> findPagedList(int pageIndex, int pageSize) {
      return query().findPagedList(pageIndex, pageSize);
    }

    /**
     * Executes a find row count query in a background thread.
     * <p>
     * Equivalent to {@link Query#findFutureRowCount()}
     */
    public FutureRowCount<T> findFutureRowCount() {
      return query().findFutureRowCount();
    }

    /**
     * Returns the total number of entities for this type. *
     * <p>
     * Equivalent to {@link Query#findRowCount()}
     */
    public int findRowCount() {
      return query().findRowCount();
    }

    /**
     * Returns the <code>ExpressionFactory</code> used by this query.
     */
    public ExpressionFactory getExpressionFactory() {
      return query().getExpressionFactory();
    }

    /**
     * Explicitly sets a comma delimited list of the properties to fetch on the 'main' entity bean,
     * to load a partial object.
     * <p>
     * Equivalent to {@link Query#select(String)}
     */
    public Query<T> select(String fetchProperties) {
      return query().select(fetchProperties);
    }

    /**
     * Specifies a path to load including all its properties.
     * <p>
     * Equivalent to {@link Query#fetch(String)}
     */
    public Query<T> fetch(String path) {
      return query().fetch(path);
    }

    /**
     * Additionally specifies a <code>FetchConfig</code> to specify a 'query join' and/or define the
     * lazy loading query.
     * <p>
     * Equivalent to {@link Query#fetch(String, FetchConfig)}
     */
    public Query<T> fetch(String path, FetchConfig joinConfig) {
      return query().fetch(path, joinConfig);
    }

    /**
     * Specifies a path to fetch with a specific list properties to include, to load a partial
     * object.
     * <p>
     * Equivalent to {@link Query#fetch(String, String)}
     */
    public Query<T> fetch(String path, String fetchProperties) {
      return query().fetch(path, fetchProperties);
    }

    /**
     * Additionally specifies a <code>FetchConfig</code> to use a separate query or lazy loading to
     * load this path.
     * <p>
     * Equivalent to {@link Query#fetch(String, String, FetchConfig)}
     */
    public Query<T> fetch(String assocProperty, String fetchProperties, FetchConfig fetchConfig) {
      return query().fetch(assocProperty, fetchProperties, fetchConfig);
    }

    /**
     * Adds expressions to the <code>where</code> clause with the ability to chain on the
     * <code>ExpressionList</code>.
     * <p>
     * Equivalent to {@link Query#where()}
     */
    public ExpressionList<T> where() {
      return query().where();
    }

    /**
     * Returns the <code>order by</code> clause so that you can append an ascending or descending
     * property to the <code>order by</code> clause.
     * <p>
     * This is exactly the same as {@link #orderBy}.
     * <p>
     * Equivalent to {@link Query#order()}
     */
    public OrderBy<T> order() {
      return query().order();
    }

    /**
     * Sets the <code>order by</code> clause, replacing the existing <code>order by</code> clause if
     * there is one.
     * <p>
     * This is exactly the same as {@link #orderBy(String)}.
     */
    public Query<T> order(String orderByClause) {
      return query().order(orderByClause);
    }

    /**
     * Returns the <code>order by</code> clause so that you can append an ascending or descending
     * property to the <code>order by</code> clause.
     * <p>
     * This is exactly the same as {@link #order}.
     * <p>
     * Equivalent to {@link Query#orderBy()}
     */
    public OrderBy<T> orderBy() {
      return query().orderBy();
    }

    /**
     * Set the <code>order by</code> clause replacing the existing <code>order by</code> clause if
     * there is one.
     * <p>
     * This is exactly the same as {@link #order(String)}.
     */
    public Query<T> orderBy(String orderByClause) {
      return query().orderBy(orderByClause);
    }

    /**
     * Sets the first row to return for this query.
     * <p>
     * Equivalent to {@link Query#setFirstRow(int)}
     */
    public Query<T> setFirstRow(int firstRow) {
      return query().setFirstRow(firstRow);
    }

    /**
     * Sets the maximum number of rows to return in the query.
     * <p>
     * Equivalent to {@link Query#setMaxRows(int)}
     */
    public Query<T> setMaxRows(int maxRows) {
      return query().setMaxRows(maxRows);
    }

    /**
     * Sets the ID value to query.
     * 
     * <p>
     * Use this to perform a find byId query but with additional control over the query such as
     * using select and fetch to control what parts of the object graph are returned.
     * <p>
     * Equivalent to {@link Query#setId(Object)}
     */
    public Query<T> setId(Object id) {
      return query().setId(id);
    }

    /**
     * Create and return a new query using the OQL.
     * <p>
     * Equivalent to {@link EbeanServer#createQuery(Class, String)}
     */
    public Query<T> setQuery(String oql) {
      return db().createQuery(type, oql);
    }

    /**
     * Create and return a new query based on the <code>RawSql</code>.
     * <p>
     * Equivalent to {@link Query#setRawSql(RawSql)}
     */
    public Query<T> setRawSql(RawSql rawSql) {
      return query().setRawSql(rawSql);
    }

    /**
     * Create a query with explicit 'Autofetch' use.
     */
    public Query<T> setAutofetch(boolean autofetch) {
      return query().setAutofetch(autofetch);
    }

    /**
     * Create a query with the select with "for update" specified.
     * 
     * <p>
     * This will typically create row level database locks on the selected rows.
     */
    public Query<T> setForUpdate(boolean forUpdate) {
      return query().setForUpdate(forUpdate);
    }

    /**
     * Create a query specifying whether the returned beans will be read-only.
     */
    public Query<T> setReadOnly(boolean readOnly) {
      return query().setReadOnly(readOnly);
    }

    /**
     * Create a query specifying if the beans should be loaded into the L2 cache.
     */
    public Query<T> setLoadBeanCache(boolean loadBeanCache) {
      return query().setLoadBeanCache(loadBeanCache);
    }

    /**
     * Create a query specifying if the L2 bean cache should be used.
     */
    public Query<T> setUseCache(boolean useBeanCache) {
      return query().setUseCache(useBeanCache);
    }

    /**
     * Create a query specifying if the L2 query cache should be used.
     */
    public Query<T> setUseQueryCache(boolean useQueryCache) {
      return query().setUseQueryCache(useQueryCache);
    }

  }
}
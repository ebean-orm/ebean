package com.avaje.ebean;

import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.util.ClassUtil;
import org.jetbrains.annotations.Nullable;

import javax.persistence.MappedSuperclass;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * A MappedSuperclass base class that provides convenience methods for inserting, updating and
 * deleting beans.
 * <p>
 * By having your entity beans extend this it provides a 'Active Record' style programming model for
 * Ebean users.
 * <p>
 * Note that there is a avaje-ebeanorm-mocker project that enables you to use Mockito or similar
 * tools to still mock out the underlying 'default EbeanServer' for testing purposes.
 * <p>
 * You may choose not use this Model mapped superclass if you don't like the 'Active Record' style
 * or if you believe it 'pollutes' your entity beans.
 * <p>
 * You can use Dependency Injection like Guice or Spring to construct and wire a EbeanServer instance
 * and have that same instance used with this Model and Finder. The way that works is that when the
 * DI container creates the EbeanServer instance it can be registered with the Ebean singleton. In this
 * way the EbeanServer instance can be injected as per normal Guice / Spring dependency injection and
 * that same instance also used to support the Model and Finder active record style.
 * <p>
 * If you choose to use the Model mapped superclass you will probably also chose to additionally add
 * a {@link Find} as a public static field to complete the active record pattern and provide a
 * relatively nice clean way to write queries.
 * <p>
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
 * <p>
 * <h3>Extend the Model</h3>
 * <pre>{@code
 *
 *     // Extend the mappedSuperclass
 *
 *     @Entity @Table(name="oto_account")
 *     public class Customer extends BaseModel {
 *
 *       // Add a static Find
 *       // ... with Long being the type of our @Id property.
 *       // ... Note the {} at the end as Find is an abstract class.
 *
 *       public static final Find<Long,Account> find = new Find<Long,Account>(){};
 *
 *       String name;
 *       ...
 *     }
 *
 * }</pre>
 * <p>
 * <h3>Modal: save()</h3>
 * <pre>{@code
 *
 *     // Active record style ... save(), delete() etc
 *     Customer customer = new Customer();
 *     customer.setName("AC234");
 *
 *     // save() method inherited from Model
 *     customer.save();
 *
 * }</pre>
 * <p>
 * <h3>Find byId</h3>
 * <pre>{@code
 *
 *     // find byId
 *     Customer customer = Customer.find.byId(42);
 *
 * }</pre>
 * <p>
 * <h3>Find where</h3>
 * <pre>{@code
 *
 *     // find where ...
 *     List<Customer> customers =
 *         Customer.find
 *         .where().gt("startDate", lastMonth)
 *         .findList();
 *
 * }</pre>
 */
@MappedSuperclass
public abstract class Model {

  /**
   * Return the underlying 'default' EbeanServer.
   * <p>
   * This provides full access to the API such as explicit transaction demarcation etc.
   * <p>
   * Example:
   * <pre>{@code
   *
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
   *   otherCustomer.setName("Franko");
   *   otherCustomer.save();
   *
   *   transaction.commit();
   *
   * } finally {
   *   transaction.end();
   * }
   *
   * }</pre>
   */
  public static EbeanServer db() {
    return Ebean.getDefaultServer();
  }

  /**
   * Return a named EbeanServer that is typically different to the default server.
   * <p>
   * If you are using multiple databases then each database has a name and maps to a single
   * EbeanServer. You can use this method to get an EbeanServer for another database.
   *
   * @param server The name of the EbeanServer. If this is null then the default EbeanServer is returned.
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
   * <p>
   * <pre>{@code
   *
   * Customer customer = Customer.find.byId(id);
   *
   * // mark the bean as dirty so that a save() or update() will
   * // increment the version property
   * customer.markAsDirty();
   * customer.save();
   *
   * }</pre>
   *
   * @see EbeanServer#markAsDirty(Object)
   */
  public void markAsDirty() {
    db().markAsDirty(this);
  }

  /**
   * Mark the property as unset or 'not loaded'.
   * <p>
   * This would be used to specify a property that we did not wish to include in a stateless update.
   * </p>
   * <pre>{@code
   *
   *   // populate an entity bean from JSON or whatever
   *   User user = ...;
   *
   *   // mark the email property as 'unset' so that it is not
   *   // included in a 'stateless update'
   *   user.markPropertyUnset("email");
   *
   *   user.update();
   *
   * }</pre>
   *
   * @param propertyName the name of the property on the bean to be marked as 'unset'
   */
  public void markPropertyUnset(String propertyName) {
    ((EntityBean) this)._ebean_getIntercept().setPropertyLoaded(propertyName, false);
  }

  /**
   * Insert or update this entity depending on its state.
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
   * Delete this bean.
   * <p>
   * This will return true if the bean was deleted successfully or JDBC batch is being used.
   * </p>
   * <p>
   * If there is no current transaction one will be created and committed for
   * you automatically.
   * </p>
   * <p>
   * If the Bean does not have a version property (or loaded version property) and
   * the bean does not exist then this returns false indicating that nothing was
   * deleted. Note that, if JDBC batch mode is used then this always returns true.
   * </p>
   *
   * @see EbeanServer#delete(Object)
   */
  public boolean delete() {
    return db().delete(this);
  }

  /**
   * Delete a bean permanently without soft delete.
   * <p>
   * This is used when the bean contains a <code>@SoftDelete</code> property and we
   * want to perform a hard/permanent delete.
   * </p>
   *
   * @see EbeanServer#deletePermanent(Object)
   */
  public boolean deletePermanent() {
    return db().deletePermanent(this);
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
  public boolean delete(String server) {
    return db(server).delete(this);
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
   * A concrete implementation of Find.
   * <p>
   * It should be preferred to use {@link Find} instead of Finder as that can use reflection to determine the class
   * literal type of the entity bean.
   * </p>
   *
   * @param <I> type of the Id property
   * @param <T> type of the entity bean
   */
  public static class Finder<I, T> extends Find<I, T> {

    /**
     * Create with the type of the entity bean.
     * <p>
     * <pre>{@code
     *
     * @Entity
     * public class Customer extends BaseModel {
     *
     *   public static final Finder<Long,Customer> find = new Finder<Long,Customer>(Customer.class);
     *   ...
     *
     * }</pre>
     * <p>
     * <p/>
     * The preferred approach is to instead use <code>Find</code> as below. This approach is more DRY in that it does
     * not require the class literal Customer.class to be passed into the constructor.
     * <p>
     * <pre>{@code
     *
     * @Entity
     * public class Customer extends BaseModel {
     *
     *   public static final Find<Long,Customer> find = new Find<Long,Customer>(){};
     *   ...
     *
     * }</pre>
     */
    public Finder(Class<T> type) {
      super(null, type);
    }

    /**
     * Create with the type of the entity bean and specific server name.
     */
    public Finder(String serverName, Class<T> type) {
      super(serverName, type);
    }

  }

  /**
   * Helper object for performing queries.
   * <p>
   * <p>
   * Typically a Find instance is defined as a public static field on an entity bean class to provide a
   * nice way to write queries.
   * <p>
   * <h3>Example use:</h3>
   * <p>
   * <pre>{@code
   *
   * @Entity
   * public class Customer extends BaseModel {
   *
   *   public static final Find<Long,Customer> find = new Find<Long,Customer>(){};
   *
   *   ...
   *
   * }</pre>
   * <p/>
   * This enables you to write code like:
   * <pre>{@code
   *
   * Customer customer = Customer.find.byId(42L);
   *
   * List<Customer> customers =
   *     Customer.find
   *         .select("name, dateOfBirth")
   *         .findList();
   *
   * }</pre>
   * <p>
   * <h3>Kotlin</h3>
   * In Kotlin you would typically create Find as a companion object.
   * <pre>{@code
   *
   *   // kotlin
   *   companion object : Model.Find<Long, Product>() {}
   *
   * }</pre>
   *
   * @param <I> The Id type. This is most often a {@link Long} but is also often a {@link UUID} or
   *            {@link String}.
   * @param <T> The entity bean type
   */
  public static abstract class Find<I, T> {

    /**
     * The entity bean type.
     */
    private final Class<T> type;

    /**
     * The name of the EbeanServer, null for the default server.
     */
    private final String serverName;

    /**
     * Creates a finder for entity of type <code>T</code> with ID of type <code>I</code>.
     * <p/>
     * Typically you create Find as a public static field on each entity bean as the example below.
     * <p>
     * <p/>
     * Note that Find is an abstract class and hence <code>{}</code> is required. This is done so
     * that the type (class literal) of the entity bean can be derived from the generics parameter.
     * <p>
     * <pre>{@code
     *
     * @Entity
     * public class Customer extends BaseModel {
     *
     *   // Note the trailing {} as Find is an abstract class.
     *   // We do this so that we can derive the type literal Customer.class
     *   // via reflection
     *   public static final Find<Long,Customer> find = new Find<Long,Customer>(){};
     *   ...
     *
     * }</pre>
     * <p/>
     * This enables you to write code like:
     * <pre>{@code
     *
     * Customer customer = Customer.find.byId(42L);
     *
     * List<Customer> customers =
     *     Customer.find
     *        .select("name, email, dateOfBirth")
     *        .findList();
     *
     * }</pre>
     * <p>
     * <h3>Kotlin</h3>
     * In Kotlin you would typically create it as a companion object.
     * <p>
     * <pre>{@code
     *
     *   // kotlin
     *   companion object : Model.Find<Long, Product>() {}
     *
     * }</pre>
     */
    @SuppressWarnings("unchecked")
    public Find() {
      this.serverName = null;
      this.type = (Class<T>) ClassUtil.getSecondArgumentType(getClass());
    }

    /**
     * Construct passing the class literal type of the entity type.
     */
    protected Find(String serverName, Class<T> type) {
      this.serverName = serverName;
      this.type = type;
    }

    /**
     * Return the underlying 'default' EbeanServer.
     * <p>
     * <p>
     * This provides full access to the API such as explicit transaction demarcation etc.
     */
    public EbeanServer db() {
      return Ebean.getServer(serverName);
    }

    /**
     * Return typically a different EbeanServer to the default.
     * <p>
     * This is equivalent to {@link Ebean#getServer(String)}
     *
     * @param server The name of the EbeanServer. If this is null then the default EbeanServer is
     *               returned.
     */
    public EbeanServer db(String server) {
      return Ebean.getServer(server);
    }

    /**
     * Creates a Finder for the named EbeanServer.
     * <p>
     * <p>
     * Create and return a new Finder for a different server.
     */
    public Finder<I, T> on(String server) {
      return new Finder<>(server, type);
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
     * <p>
     * <p>
     * This is the same as (synonym for) {@link #findList()}
     */
    public List<T> all() {
      return findList();
    }

    /**
     * Retrieves an entity by ID.
     * <p>
     * <p>
     * Equivalent to {@link EbeanServer#find(Class, Object)}
     */
    @Nullable
    public T byId(I id) {
      return db().find(type, id);
    }

    /**
     * Creates an entity reference for this ID.
     * <p>
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
     * Equivalent to {@link Query#apply(FetchPath)}
     */
    public Query<T> apply(FetchPath fetchPath) {
      return db().find(type).apply(fetchPath);
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
    public <A> List<A> findIds() {
      return query().findIds();
    }

    /**
     * Execute the query consuming each bean one at a time.
     * <p>
     * This is generally used to process large queries where unlike findList
     * you do not want to hold all the results in memory at once but instead
     * process them one at a time (requiring far less memory).
     * </p>
     * Equivalent to {@link Query#findEach(Consumer)}
     */
    public void findEach(Consumer<T> consumer) {
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
    public <K> Map<K, T> findMap() {
      return query().findMap();
    }

    /**
     * Executes a find row count query in a background thread.
     * <p>
     * Equivalent to {@link Query#findFutureCount()}
     */
    public FutureRowCount<T> findFutureCount() {
      return query().findFutureCount();
    }

    /**
     * Deprecated in favor of findFutureCount().
     * <p>
     * Equivalent to {@link Query#findFutureCount()}
     */
    public FutureRowCount<T> findFutureRowCount() {
      return query().findFutureCount();
    }

    /**
     * Returns the total number of entities for this type. *
     * <p>
     * Equivalent to {@link Query#findCount()}
     */
    public int findCount() {
      return query().findCount();
    }

    /**
     * Deprecated in favor of findCount().
     *
     * @deprecated
     */
    public int findRowCount() {
      return query().findCount();
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
     * <p>
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
     * Create and return a new query based on the <code>RawSql</code>.
     * <p>
     * Equivalent to {@link Query#setRawSql(RawSql)}
     */
    public Query<T> setRawSql(RawSql rawSql) {
      return query().setRawSql(rawSql);
    }

    /**
     * Create a query with explicit 'AutoTune' use.
     */
    public Query<T> setAutoTune(boolean autoTune) {
      return query().setAutoTune(autoTune);
    }

    /**
     * Create a query with the select with "for update" specified.
     * <p>
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

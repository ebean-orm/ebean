package com.avaje.ebean;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
 */
@MappedSuperclass
public class Model {

  private Object _getId() {
    try {
      return db().getBeanId(this);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Return the underlying 'default' EbeanServer.
   * 
   * <p>
   * This provides full access to the API such as explicit transaction demarcation etc.
   * 
   * <p>
   * TODO: Transaction example
   */
  public EbeanServer db() {
    return Ebean.getServer(null);
  }

  /**
   * Return typically a different EbeanServer to the default.
   * 
   * @param server
   *          The name of the EbeanServer. If this is null then the default EbeanServer is returned.
   */
  public EbeanServer db(String server) {
    return Ebean.getServer(server);
  }

  /**
   * Insert or update this entity depending on its state.
   * 
   * <p>
   * Ebean will detect if this is a new bean or a previously fetched bean and perform either an
   * insert or an update based on that.
   */
  public void save() {
    db().save(this);
  }

  /**
   * Update this entity.
   */
  public void update() {
    db().update(this);
  }

  /**
   * Insert this entity.
   */
  public void insert() {
    db().insert(this);
  }

  /**
   * Deletes this entity.
   */
  public void delete() {
    db().delete(this);
  }

  /**
   * Update this entity.
   */
  public void update(String server) {
    db(server).update(this);
  }

  /**
   * Insert this entity.
   */
  public void insert(String server) {
    db(server).insert(this);
  }

  /**
   * Deletes this entity.
   */
  public void delete(String server) {
    db(server).delete(this);
  }

  /**
   * Refreshes this entity from the database.
   */
  public void refresh() {
    db().refresh(this);
  }

  @Override
  public boolean equals(Object other) {
    // RB: This is inconsistent with the default Ebean implementation so
    // in that sense I don't believe this can be the default implementation 
    if (this == other)
      return true;
    if (other == null || other.getClass() != this.getClass())
      return false;
    Object id = _getId();
    Object otherId = ((Model) other)._getId();
    if (id == null)
      return false;
    if (otherId == null)
      return false;
    return id.equals(otherId);
  }

  @Override
  public int hashCode() {
    // RB: this hashCode changes ... so I don't like this as the
    // baked in default behaviour - but need to support this for any Play users
    // that convert over to this. Best option is to support this in enhancement and let the
    // users choose their poison. Alternatively provide 2 versions of Model with the
    // 2 different behaviours for hashCode / equals
    Object id = _getId();
    return id == null ? super.hashCode() : id.hashCode();
  }

  /**
   * Helper for queries.
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
     * specific Ebean server.
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
     */
    public EbeanServer db() {
      return Ebean.getServer(serverName);
    }

    /**
     * Return typically a different EbeanServer to the default.
     * 
     * @param server
     *          The name of the EbeanServer. If this is null then the default EbeanServer is
     *          returned.
     */
    public EbeanServer db(String server) {
      return Ebean.getServer(server);
    }

    /**
     * Changes the Ebean server.
     * 
     * <p>
     * Create and return a new Finder for a different server.
     */
    public Finder<I, T> on(String server) {
      return new Finder<I, T>(server, idType, type);
    }

    // Does not exist yet but I think should
    // public void deleteById(I id) {
    // return db().deleteById(type, id);
    // }

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
     */
    public T byId(I id) {
      return db().find(type, id);
    }

    /**
     * Creates an entity reference for this ID.
     */
    public T ref(I id) {
      return db().getReference(type, id);
    }

    /**
     * Creates a filter for sorting and filtering lists of entities locally without going back to
     * the database.
     */
    public Filter<T> filter() {
      return db().filter(type);
    }

    /**
     * Creates a query.
     */
    public Query<T> query() {
      return db().find(type);
    }

    /**
     * Returns the next identity value.
     */
    @SuppressWarnings("unchecked")
    public I nextId() {
      return (I) db().nextId(type);
    }

    /**
     * Executes a find IDs query in a background thread.
     * 
     * @deprecated RB: Hmm, this is a "find all" - less is more, prefer to hide this - just get from
     *             query().
     */
    public FutureIds<T> findFutureIds() {
      return query().findFutureIds();
    }

    /**
     * Executes a query and returns the results as a list of IDs.
     */
    public List<Object> findIds() {
      return query().findIds();
    }

    /**
     * Retrieves all entities of the given type.
     * 
     * <p>
     * The same as {@link #all()}
     */
    public List<T> findList() {
      return query().findList();
    }

    /**
     * Returns all the entities of the given type as a set.
     */
    public Set<T> findSet() {
      return query().findSet();
    }

    /**
     * Retrieves all entities of the given type as a map of objects.
     */
    public Map<?, T> findMap() {
      return query().findMap();
    }

    /**
     * Executes the query and returns the results as a map of the objects specifying the map key
     * property.
     */
    public <K> Map<K, T> findMap(String keyProperty, Class<K> keyType) {
      return query().findMap(keyProperty, keyType);
    }

    /**
     * Return a PagedList of all entities of the given type (use where() to specify predicates as
     * needed).
     */
    public PagedList<T> findPagedList(int pageIndex, int pageSize) {
      return query().findPagedList(pageIndex, pageSize);
    }

    /**
     * Returns a <code>PagingList</code> for this query.
     * 
     * @deprecated RB: PagingList deprecated - migrate to findPagedList().
     */
    public PagingList<T> findPagingList(int pageSize) {
      return query().findPagingList(pageSize);
    }

    /**
     * Executes a find row count query in a background thread.
     */
    public FutureRowCount<T> findFutureRowCount() {
      return query().findFutureRowCount();
    }

    /**
     * Returns the total number of entities for this type.
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
     */
    public Query<T> select(String fetchProperties) {
      return query().select(fetchProperties);
    }

    /**
     * Specifies a path to load including all its properties.
     */
    public Query<T> fetch(String path) {
      return query().fetch(path);
    }

    /**
     * Additionally specifies a <code>FetchConfig</code> to specify a 'query join' and/or define the
     * lazy loading query.
     */
    public Query<T> fetch(String path, FetchConfig joinConfig) {
      return query().fetch(path, joinConfig);
    }

    /**
     * Specifies a path to fetch with a specific list properties to include, to load a partial
     * object.
     */
    public Query<T> fetch(String path, String fetchProperties) {
      return query().fetch(path, fetchProperties);
    }

    /**
     * Additionally specifies a <code>FetchConfig</code> to use a separate query or lazy loading to
     * load this path.
     */
    public Query<T> fetch(String assocProperty, String fetchProperties, FetchConfig fetchConfig) {
      return query().fetch(assocProperty, fetchProperties, fetchConfig);
    }

    /**
     * Adds expressions to the <code>where</code> clause with the ability to chain on the
     * <code>ExpressionList</code>.
     */
    public ExpressionList<T> where() {
      return query().where();
    }

    /**
     * Adds a single <code>Expression</code> to the <code>where</code> clause and returns the query.
     */
    public Query<T> where(com.avaje.ebean.Expression expression) {
      return query().where(expression);
    }

    /**
     * Adds additional clauses to the <code>where</code> clause.
     */
    public Query<T> where(String addToWhereClause) {
      return query().where(addToWhereClause);
    }

    /**
     * Returns the <code>order by</code> clause so that you can append an ascending or descending
     * property to the <code>order by</code> clause.
     * <p>
     * This is exactly the same as {@link #orderBy}.
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
     */
    public Query<T> setFirstRow(int firstRow) {
      return query().setFirstRow(firstRow);
    }

    /**
     * Sets the maximum number of rows to return in the query.
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
     */
    public Query<T> setId(Object id) {
      return query().setId(id);
    }

    /**
     * Create and return a new query using the OQL.
     */
    public Query<T> setQuery(String oql) {
      return db().createQuery(type, oql);
    }

    /**
     * Create and return a new query based on the <code>RawSql</code>.
     */
    public Query<T> setRawSql(RawSql rawSql) {
      return query().setRawSql(rawSql);
    }

    /**
     * Sets the property to use as keys for a {@link Query#findMap()} query.
     */
    public Query<T> setMapKey(String mapKey) {
      return query().setMapKey(mapKey);
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
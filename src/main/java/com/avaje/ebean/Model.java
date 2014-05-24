package com.avaje.ebean;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.MappedSuperclass;

/**
 * A MappedSuperclass base class that provides convenience methods for inserting, updating and deleting beans.
 * 
 * <p>By having your entity beans extend this it provides a 'Active Record' style programming model for Ebean users.
 * 
 * <p>Note that there is a avaje-ebeanorm-mocker project that enables you to use Mockito or similar tools to still
 * mock out the underlying 'default EbeanServer' for testing purposes.
 */
@MappedSuperclass
public class Model {
 

  private Object _getId() {
    try {
      return Ebean.getServer(null).getBeanId(this);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Return the underlying 'default' EbeanServer.
   * 
   * <p>This provides full access to the API such as explicit transaction demarcation etc.
   */
  public EbeanServer db() {
    return Ebean.getServer(null);
  }
  
  /**
   * Return typically a different EbeanServer to the default.
   * 
   * @param server The name of the EbeanServer. If this is null then the default EbeanServer is returned.
   */
  public EbeanServer db(String server) {
    return Ebean.getServer(server);
  }
  
  /**
   * Inserts or update this entity depending on its state.
   */
  public void save() {
    Ebean.save(this);
  }

  /**
   * Updates this entity.
   */
  public void update() {
    Ebean.update(this);
  }

  /**
   * Deletes this entity.
   */
  public void delete() {
    Ebean.delete(this);
  }

  /**
   * Refreshes this entity from the database.
   */
  public void refresh() {
    Ebean.refresh(this);
  }


  @Override
  public boolean equals(Object other) {
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
     * <p>Typically you use this constructor to have a static "find" field on each entity bean.
     */
    public Finder(Class<I> idType, Class<T> type) {
      this(null, idType, type);
    }

    /**
     * Creates a finder for entity of type <code>T</code> with ID of type <code>I</code>, using a
     * specific Ebean server.
     * 
     * <p>Typically you don't need to use this method.
     */
    public Finder(String serverName, Class<I> idType, Class<T> type) {
      this.type = type;
      this.idType = idType;
      this.serverName = serverName;
    }

    private EbeanServer server() {
      return Ebean.getServer(serverName);
    }

    /**
     * Return the underlying 'default' EbeanServer.
     * 
     * <p>This provides full access to the API such as explicit transaction demarcation etc.
     */
    public EbeanServer db() {
      return Ebean.getServer(null);
    }
    
    /**
     * Return typically a different EbeanServer to the default.
     * 
     * @param server The name of the EbeanServer. If this is null then the default EbeanServer is returned.
     */
    public EbeanServer db(String server) {
      return Ebean.getServer(server);
    }
    
    /**
     * Changes the Ebean server.
     */
    public Finder<I, T> on(String server) {
      return new Finder<I,T>(server, idType, type);
    }

    /**
     * Retrieves all entities of the given type.
     */
    public List<T> all() {
      return server().find(type).findList();
    }

    /**
     * Retrieves an entity by ID.
     */
    public T byId(I id) {
      return server().find(type, id);
    }

    /**
     * Creates an entity reference for this ID.
     */
    public T ref(I id) {
      return server().getReference(type, id);
    }

    /**
     * Creates a filter for sorting and filtering lists of entities locally without going back to
     * the database.
     */
    public Filter<T> filter() {
      return server().filter(type);
    }

    /**
     * Creates a query.
     */
    public Query<T> query() {
      return server().find(type);
    }

    /**
     * Returns the next identity value.
     */
    @SuppressWarnings("unchecked")
    public I nextId() {
      return (I) server().nextId(type);
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
     * Executes a find IDs query in a background thread.
     * 
     * @deprecated RB: Hmm, this is a "find all" - less is more, prefer to hide this - just get from query().
     */
    public FutureIds<T> findFutureIds() {
      return query().findFutureIds();
    }

    /**
     * Executes a find row count query in a background thread.
     */
    public FutureRowCount<T> findFutureRowCount() {
      return query().findFutureRowCount();
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
     * <p>The same as {@link #all()}
     */
    public List<T> findList() {
      return query().findList();
    }

    /**
     * Retrieves all entities of the given type as a map of objects.
     */
    public Map<?, T> findMap() {
      return query().findMap();
    }

    /**
     * Executes the query and returns the results as a map of the objects specifying the map key property.
     */
    public <K> Map<K, T> findMap(String keyProperty, Class<K> keyType) {
      return query().findMap(keyProperty, keyType);
    }

    /**
     * Return a PagedList of all entities of the given type (use where() to specify predicates as needed).
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
     * Returns the total number of entities for this type.
     */
    public int findRowCount() {
      return query().findRowCount();
    }

    /**
     * Returns all the entities of the given type as a set.
     */
    public Set<T> findSet() {
      return query().findSet();
    }

    /**
     * Returns the <code>ExpressionFactory</code> used by this query.
     */
    public ExpressionFactory getExpressionFactory() {
      return query().getExpressionFactory();
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
     * Explicitly sets a comma delimited list of the properties to fetch on the 'main' entity bean,
     * to load a partial object.
     */
    public Query<T> select(String fetchProperties) {
      return query().select(fetchProperties);
    }

    /**
     * Explicitly specifies whether to use 'Autofetch' for this query.
     */
    public Query<T> setAutofetch(boolean autofetch) {
      return query().setAutofetch(autofetch);
    }

    /**
     * Sets the first row to return for this query.
     */
    public Query<T> setFirstRow(int firstRow) {
      return query().setFirstRow(firstRow);
    }

    /**
     * Sets the ID value to query.
     */
    public Query<T> setId(Object id) {
      return query().setId(id);
    }

    /**
     * When set to <code>true</code>, all the beans from this query are loaded into the bean cache.
     */
    public Query<T> setLoadBeanCache(boolean loadBeanCache) {
      return query().setLoadBeanCache(loadBeanCache);
    }

    /**
     * Sets the property to use as keys for a map.
     */
    public Query<T> setMapKey(String mapKey) {
      return query().setMapKey(mapKey);
    }

    /**
     * Sets the maximum number of rows to return in the query.
     */
    public Query<T> setMaxRows(int maxRows) {
      return query().setMaxRows(maxRows);
    }

    /**
     * Sets the OQL query to run
     */
    public Query<T> setQuery(String oql) {
      return server().createQuery(type, oql);
    }

    /**
     * Sets <code>RawSql</code> to use for this query.
     */
    public Query<T> setRawSql(RawSql rawSql) {
      return query().setRawSql(rawSql);
    }

    /**
     * Sets whether the returned beans will be read-only.
     */
    public Query<T> setReadOnly(boolean readOnly) {
      return query().setReadOnly(readOnly);
    }

    /**
     * Sets whether to use the bean cache.
     */
    public Query<T> setUseCache(boolean useBeanCache) {
      return query().setUseCache(useBeanCache);
    }

    /**
     * Sets whether to use the query cache.
     */
    public Query<T> setUseQueryCache(boolean useQueryCache) {
      return query().setUseQueryCache(useQueryCache);
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
     * Execute the select with "for update" which should lock the record "on read"
     */
    public Query<T> setForUpdate(boolean forUpdate) {
      return query().setForUpdate(forUpdate);
    }

  }
}
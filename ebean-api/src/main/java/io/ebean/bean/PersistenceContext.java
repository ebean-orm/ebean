package io.ebean.bean;

/**
 * Holds entity beans by there type and id.
 * <p>
 * This is used to ensure only one instance for a given entity type and id is
 * used to build object graphs from queries and lazy loading.
 */
public interface PersistenceContext {

  /**
   * Put the entity bean into the PersistenceContext.
   */
  void put(Class<?> rootType, Object id, Object bean);

  /**
   * Put the entity bean into the PersistenceContext if one is not already
   * present (for this id).
   * <p>
   * Returns an existing entity bean (if one is already there) and otherwise
   * returns null.
   */
  Object putIfAbsent(Class<?> rootType, Object id, Object bean);

  /**
   * Return an object given its type and unique id.
   */
  Object get(Class<?> rootType, Object uid);

  /**
   * Get the bean from the persistence context also checked to see if it had
   * been previously deleted (if so then you also can't hit the L2 cache to
   * fetch the bean for this particular persistence context).
   */
  WithOption getWithOption(Class<?> rootType, Object uid);

  /**
   * Clear all the references.
   */
  void clear();

  /**
   * Clear all the references for a given type of entity bean.
   */
  void clear(Class<?> rootType);

  /**
   * Clear the reference to a specific entity bean.
   */
  void clear(Class<?> rootType, Object uid);

  /**
   * Clear the reference as a result of an entity being deleted.
   */
  void deleted(Class<?> rootType, Object id);

  /**
   * Return the number of beans of the given type in the persistence context.
   */
  int size(Class<?> rootType);

  /**
   * Signalizes the PersistenceContext, the begin for large query iteration.
   */
  void beginIterate();

  /**
   * Signalizes the PersistenceContext, the end for large query iteration.
   */
  void endIterate();
  
  /**
   * Wrapper on a bean to also indicate if a bean has been deleted.
   * <p>
   * If a bean has been deleted then for the same persistence context is should
   * not be able to be fetched from persistence context or L2 cache.
   */
  class WithOption {

    /**
     * The bean was previously deleted from this persistence context (can't hit
     * L2 cache).
     */
    public static final WithOption DELETED = new WithOption();

    private final boolean deleted;
    private final Object bean;

    private WithOption() {
      this.deleted = true;
      this.bean = null;
    }

    /**
     * The bean exists in the persistence context (and not been previously deleted).
     */
    public WithOption(Object bean) {
      this.deleted = false;
      this.bean = bean;
    }

    /**
     * Return true if the bean was deleted. This means you can't hit the L2
     * cache.
     */
    public boolean isDeleted() {
      return deleted;
    }

    /**
     * Return the bean (from the persistence context).
     */
    public Object getBean() {
      return bean;
    }
  }
}

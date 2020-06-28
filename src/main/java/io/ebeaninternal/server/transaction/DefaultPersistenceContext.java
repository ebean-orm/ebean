package io.ebeaninternal.server.transaction;

import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.api.Monitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of PersistenceContext.
 * <p>
 * Ensures only one instance of a bean is used according to its type and unique
 * id.
 * </p>
 * <p>
 * PersistenceContext lives on a Transaction and as such is expected to only
 * have a single thread accessing it at a time. This is not expected to be used
 * concurrently.
 * </p>
 * <p>
 * Duplicate beans are ones having the same type and unique id value. These are
 * considered duplicates and replaced by the bean instance that was already
 * loaded into the PersistenceContext.
 * </p>
 */
public final class DefaultPersistenceContext implements PersistenceContext {

  /**
   * Map used hold caches. One cache per bean type.
   */
  private final HashMap<Class<?>, ClassContext> typeCache = new HashMap<>();

  private final Monitor monitor = new Monitor();

  private int putCount;

  /**
   * Create a new PersistenceContext.
   */
  public DefaultPersistenceContext() {
  }

  /**
   * Create as a shallow copy with initial or types that have not been added to.
   */
  private DefaultPersistenceContext(DefaultPersistenceContext parent, boolean initial) {
    for (Map.Entry<Class<?>, ClassContext> entry : parent.typeCache.entrySet()) {
      typeCache.put(entry.getKey(), entry.getValue().copy(initial));
    }
  }

  /**
   * Return the initial shallow copy with each ClassContext noting it's initialSize (to detect additions).
   */
  @Override
  public PersistenceContext forIterate() {
    return new DefaultPersistenceContext(this, true);
  }

  /**
   * Return a shallow copy including each ClassContext that has had no additions (still at initialSize).
   */
  @Override
  public PersistenceContext forIterateReset() {
    return new DefaultPersistenceContext(this, false);
  }

  public boolean resetLimit() {
    synchronized (monitor) {
      if (putCount < 100) {
        return false;
      }
      putCount = 0;
      for (ClassContext value : typeCache.values()) {
        if (value.resetLimit()) {
          return true;
        }
      }
      // checking after another 100 puts
      return false;
    }
  }

  /**
   * Set an object into the PersistenceContext.
   */
  @Override
  public void put(Class<?> rootType, Object id, Object bean) {
    synchronized (monitor) {
      putCount++;
      getClassContext(rootType).put(id, bean);
    }
  }

  @Override
  public Object putIfAbsent(Class<?> rootType, Object id, Object bean) {
    synchronized (monitor) {
      putCount++;
      return getClassContext(rootType).putIfAbsent(id, bean);
    }
  }

  /**
   * Return an object given its type and unique id.
   */
  @Override
  public Object get(Class<?> rootType, Object id) {
    synchronized (monitor) {
      return getClassContext(rootType).get(id);
    }
  }

  @Override
  public WithOption getWithOption(Class<?> rootType, Object id) {
    synchronized (monitor) {
      return getClassContext(rootType).getWithOption(id);
    }
  }

  /**
   * Return the number of beans of the given type in the persistence context.
   */
  @Override
  public int size(Class<?> rootType) {
    synchronized (monitor) {
      ClassContext classMap = typeCache.get(rootType);
      return classMap == null ? 0 : classMap.size();
    }
  }

  /**
   * Clear the PersistenceContext.
   */
  @Override
  public void clear() {
    synchronized (monitor) {
      typeCache.clear();
    }
  }

  @Override
  public void clear(Class<?> rootType) {
    synchronized (monitor) {
      ClassContext classMap = typeCache.get(rootType);
      if (classMap != null) {
        classMap.clear();
      }
    }
  }

  @Override
  public void deleted(Class<?> rootType, Object id) {
    synchronized (monitor) {
      ClassContext classMap = typeCache.get(rootType);
      if (classMap != null && id != null) {
        classMap.deleted(id);
      }
    }
  }

  @Override
  public void clear(Class<?> rootType, Object id) {
    synchronized (monitor) {
      ClassContext classMap = typeCache.get(rootType);
      if (classMap != null && id != null) {
        classMap.remove(id);
      }
    }
  }

  @Override
  public String toString() {
    synchronized (monitor) {
      return typeCache.toString();
    }
  }

  private ClassContext getClassContext(Class<?> rootType) {
    return typeCache.computeIfAbsent(rootType, k -> new ClassContext());
  }

  private static class ClassContext {

    private final Map<Object, Object> map = new HashMap<>();

    private Set<Object> deleteSet;

    private int initialSize;

    private ClassContext parent;

    private ClassContext() {
    }

    /**
     * Create as a shallow copy.
     */
    private ClassContext(ClassContext source, boolean initial) {
      if (initial || source.isTransfer()) {
        parent = source.transferParent();
        initialSize = parent.size();
        if (source.deleteSet != null) {
          deleteSet = new HashSet<>(source.deleteSet);
        }
      }
    }

    /**
     * True if this should be transferred to a new iterator persistence context.
     */
    private boolean isTransfer() {
      // map not added to and has some original/parent beans
      return map.isEmpty() && initialSize > 0;
    }

    private ClassContext transferParent() {
      return (parent != null) ? parent : this;
    }

    /**
     * Return a shallow copy if initial copy or it has not grown (still at initialSize).
     */
    private ClassContext copy(boolean initial) {
      return new ClassContext(this, initial);
    }

    /**
     * Return true if grown above the reset limit size of 1000.
     */
    private boolean resetLimit() {
      return map.size() > 1000;
    }

    @Override
    public String toString() {
      return "size:" + map.size();
    }

    private Object get(Object id) {
      Object bean = (parent == null) ? null : parent.get(id);
      return bean != null ? bean : map.get(id);
    }

    private WithOption getWithOption(Object id) {
      if (deleteSet != null && deleteSet.contains(id)) {
        return WithOption.DELETED;
      }
      Object bean = get(id);
      return (bean == null) ? null : new WithOption(bean);
    }

    private Object putIfAbsent(Object id, Object bean) {
      Object existingValue = get(id);
      if (existingValue != null) {
        // it is not absent
        return existingValue;
      }
      // put the new value and return null indicating the put was successful
      map.put(id, bean);
      return null;
    }

    private void put(Object id, Object b) {
      map.put(id, b);
    }

    private int size() {
      return map.size() + initialSize;
    }

    private void clear() {
      map.clear();
    }

    private void remove(Object id) {
      map.remove(id);
    }

    private void deleted(Object id) {
      if (deleteSet == null) {
        deleteSet = new HashSet<>();
      }
      deleteSet.add(id);
      map.remove(id);
    }
  }

}

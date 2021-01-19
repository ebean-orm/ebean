package io.ebeaninternal.server.transaction;

import io.ebean.bean.PersistenceContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

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

  private final ReentrantLock lock = new ReentrantLock();

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
    lock.lock();
    try {
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
    } finally {
      lock.unlock();
    }
  }

  /**
   * Set an object into the PersistenceContext.
   */
  @Override
  public void put(Class<?> rootType, Object id, Object bean) {
    lock.lock();
    try {
      putCount++;
      getClassContext(rootType).put(id, bean);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Object putIfAbsent(Class<?> rootType, Object id, Object bean) {
    lock.lock();
    try {
      putCount++;
      return getClassContext(rootType).putIfAbsent(id, bean);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Return an object given its type and unique id.
   */
  @Override
  public Object get(Class<?> rootType, Object id) {
    lock.lock();
    try {
      return getClassContext(rootType).get(id);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public WithOption getWithOption(Class<?> rootType, Object id) {
    lock.lock();
    try {
      return getClassContext(rootType).getWithOption(id);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Return the number of beans of the given type in the persistence context.
   */
  @Override
  public int size(Class<?> rootType) {
    lock.lock();
    try {
      ClassContext classMap = typeCache.get(rootType);
      return classMap == null ? 0 : classMap.size();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Clear the PersistenceContext.
   */
  @Override
  public void clear() {
    lock.lock();
    try {
      typeCache.clear();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void clear(Class<?> rootType) {
    lock.lock();
    try {
      ClassContext classMap = typeCache.get(rootType);
      if (classMap != null) {
        classMap.clear();
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void deleted(Class<?> rootType, Object id) {
    lock.lock();
    try {
      ClassContext classMap = typeCache.get(rootType);
      if (classMap != null && id != null) {
        classMap.deleted(id);
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void clear(Class<?> rootType, Object id) {
    lock.lock();
    try {
      ClassContext classMap = typeCache.get(rootType);
      if (classMap != null && id != null) {
        classMap.remove(id);
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public String toString() {
    lock.lock();
    try {
      return typeCache.toString();
    } finally {
      lock.unlock();
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

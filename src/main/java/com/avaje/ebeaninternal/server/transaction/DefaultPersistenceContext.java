package com.avaje.ebeaninternal.server.transaction;

import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebean.bean.PersistenceContextUtil;
import com.avaje.ebeaninternal.api.Monitor;

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
  private final HashMap<Class<?>, ClassContext> typeCache = new HashMap<Class<?>, ClassContext>();

  private final Monitor monitor = new Monitor();

  /**
   * Create a new PersistenceContext.
   */
  public DefaultPersistenceContext() {
  }

  /**
   * Set an object into the PersistenceContext.
   */
  public void put(Class<?> rootType, Object id, Object bean) {
    synchronized (monitor) {
      getClassContext(rootType).put(id, bean);
    }
  }

  public Object putIfAbsent(Class<?> rootType, Object id, Object bean) {
    synchronized (monitor) {
      return getClassContext(rootType).putIfAbsent(id, bean);
    }
  }

  /**
   * Return an object given its type and unique id.
   */
  public Object get(Class<?> rootType, Object id) {
    synchronized (monitor) {
      return getClassContext(rootType).get(id);
    }
  }

  public WithOption getWithOption(Class<?> rootType, Object id) {
    synchronized (monitor) {
      return getClassContext(rootType).getWithOption(id);
    }
  }

  /**
   * Return the number of beans of the given type in the persistence context.
   */
  public int size(Class<?> rootType) {
    synchronized (monitor) {
      ClassContext classMap = typeCache.get(rootType);
      return classMap == null ? 0 : classMap.size();
    }
  }

  /**
   * Clear the PersistenceContext.
   */
  public void clear() {
    synchronized (monitor) {
      typeCache.clear();
    }
  }

  public void clear(Class<?> rootType) {
    synchronized (monitor) {
      ClassContext classMap = typeCache.get(rootType);
      if (classMap != null) {
        classMap.clear();
      }
    }
  }

  public void deleted(Class<?> rootType, Object id) {
    synchronized (monitor) {
      ClassContext classMap = typeCache.get(rootType);
      if (classMap != null && id != null) {
        classMap.deleted(id);
      }
    }
  }

  public void clear(Class<?> rootType, Object id) {
    synchronized (monitor) {
      ClassContext classMap = typeCache.get(rootType);
      if (classMap != null && id != null) {
        classMap.remove(id);
      }
    }
  }

  public String toString() {
    synchronized (monitor) {
      return typeCache.toString();
    }
  }

  private ClassContext getClassContext(Class<?> rootType) {

    ClassContext classMap = typeCache.get(rootType);
    if (classMap == null) {
      classMap = new ClassContext();
      typeCache.put(rootType, classMap);
    }
    return classMap;
  }

  private static class ClassContext {

    private final Map<Object, Object> map = new HashMap<Object, Object>();

    private Set<Object> deleteSet;

    private ClassContext() {
    }

    public String toString() {
      return "size:" + map.size();
    }

    private WithOption getWithOption(Object id) {
      if (deleteSet != null && deleteSet.contains(id)) {
        return WithOption.DELETED;
      }
      Object bean = map.get(id);
      return (bean == null) ? null : new WithOption(bean);
    }

    private Object get(Object id) {
      return map.get(id);
    }

    private Object putIfAbsent(Object id, Object bean) {

      Object existingValue = map.get(id);
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
      return map.size();
    }

    private void clear() {
      map.clear();
    }

    private void remove(Object id) {
      map.remove(id);
    }

    private void deleted(Object id) {
      if (deleteSet == null) {
        deleteSet = new HashSet<Object>();
      }
      deleteSet.add(id);
      map.remove(id);
    }
  }

}

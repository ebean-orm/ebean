package io.ebeaninternal.server.transaction;

import io.ebean.bean.EntityBean;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.api.SpiBeanType;
import io.ebeaninternal.api.SpiBeanTypeManager;
import io.ebeaninternal.api.SpiPersistenceContext;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Weak reference based PersistenceContext for "streaming queries".
 */
final class WeakPersistenceContext implements SpiPersistenceContext {

  private final HashMap<Class<?>, WeakClassContext> typeCache = new HashMap<>();
  private final ReentrantLock lock = new ReentrantLock();

  WeakPersistenceContext() {
  }

  /**
   * Load from the initiating persistence context.
   */
  void add(Class<?> rootType, Set<Object> deleteSet, Map<Object, Object> map) {
    typeCache.put(rootType, new WeakClassContext( rootType, deleteSet, map));
  }

  @Override
  public PersistenceContext forIterate() {
    return this;
  }

  @Override
  public void put(Class<?> rootType, Object id, Object bean) {
    lock.lock();
    try {
      classContext(rootType).put(id, bean);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Object putIfAbsent(Class<?> rootType, Object id, Object bean) {
    lock.lock();
    try {
      return classContext(rootType).putIfAbsent(id, bean);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Object get(Class<?> rootType, Object id) {
    lock.lock();
    try {
      return classContext(rootType).get(id);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public WithOption getWithOption(Class<?> rootType, Object id) {
    lock.lock();
    try {
      return classContext(rootType).getWithOption(id);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public int size(Class<?> rootType) {
    lock.lock();
    try {
      WeakClassContext classMap = typeCache.get(rootType);
      return classMap == null ? 0 : classMap.size();
    } finally {
      lock.unlock();
    }
  }

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
      WeakClassContext classMap = typeCache.get(rootType);
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
      WeakClassContext classMap = typeCache.get(rootType);
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
      WeakClassContext classMap = typeCache.get(rootType);
      if (classMap != null && id != null) {
        classMap.remove(id);
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public List<Object> dirtyBeans(SpiBeanTypeManager manager) {
    lock.lock();
    try {
      List<Object> list = new ArrayList<>();
      for (WeakClassContext classContext : typeCache.values()) {
        classContext.dirtyBeans(manager, list);
      }
      return list;
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

  private WeakClassContext classContext(Class<?> rootType) {
    return typeCache.computeIfAbsent(rootType, k -> new WeakClassContext(rootType));
  }

  private static class WeakClassContext {

    private final ReferenceQueue<Object> queue = new ReferenceQueue<>();
    private final Map<Object, BeanRef> map = new HashMap<>();
    private final Class<?> rootType;
    private Set<Object> deleteSet;

    private WeakClassContext(Class<?> rootType) {
      this.rootType = rootType;
    }

    private WeakClassContext(Class<?> rootType, Set<Object> initialDeleteSet, Map<Object, Object> initialMap) {
      this.rootType = rootType;
      this.deleteSet = initialDeleteSet == null ? null : new HashSet<>(initialDeleteSet);
      for (Map.Entry<Object, Object> entry : initialMap.entrySet()) {
        Object id = entry.getKey();
        map.put(id, new BeanRef(id, entry.getValue(), queue));
      }
    }

    private void expungeStaleEntries() {
      Reference<?> ref;
      while ((ref = queue.poll()) != null) {
        map.remove(((BeanRef)ref).key());
      }
    }

    @Override
    public String toString() {
      return "size:" + map.size();
    }

    private Object get(Object id) {
      expungeStaleEntries();
      Reference<Object> ref = map.get(id);
      return ref == null ? null : ref.get();
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
      put(id, bean);
      return null;
    }

    private void put(Object id, Object b) {
      expungeStaleEntries();
      map.put(id, new BeanRef(id, b, queue));
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
        deleteSet = new HashSet<>();
      }
      deleteSet.add(id);
      map.remove(id);
    }

    /**
     * Add the dirty beans to the list.
     */
    void dirtyBeans(SpiBeanTypeManager manager, List<Object> list) {
      final SpiBeanType beanType = manager.beanType(rootType);
      for (BeanRef value : map.values()) {
        EntityBean bean = (EntityBean) value.get();
        if (bean != null && (bean._ebean_getIntercept().isDirty() || beanType.isToManyDirty(bean))) {
          list.add(bean);
        }
      }
    }
  }

  private static class BeanRef extends WeakReference<Object> {
    private final Object key;
    private BeanRef(Object key, Object referent, ReferenceQueue<? super Object> q) {
      super(referent, q);
      this.key = key;
    }
    Object key() {
      return key;
    }
  }
}

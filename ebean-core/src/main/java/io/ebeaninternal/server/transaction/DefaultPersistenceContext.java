package io.ebeaninternal.server.transaction;

import io.ebean.bean.ClassContextTracker;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiBeanType;
import io.ebeaninternal.api.SpiBeanTypeManager;
import io.ebeaninternal.api.SpiPersistenceContext;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Default implementation of PersistenceContext.
 * <p>
 * Ensures only one instance of a bean is used according to its type and unique
 * id.
 * <p>
 * PersistenceContext lives on a Transaction and as such is expected to only
 * have a single thread accessing it at a time. This is not expected to be used
 * concurrently.
 * <p>
 * Duplicate beans are ones having the same type and unique id value. These are
 * considered duplicates and replaced by the bean instance that was already
 * loaded into the PersistenceContext.
 */
public final class DefaultPersistenceContext implements SpiPersistenceContext {

  private final HashMap<Class<?>, ClassContext> typeCache = new HashMap<>();
  private final ReentrantLock lock = new ReentrantLock();
  private final ReferenceQueue<Object> queue = new ReferenceQueue<>();

  /**
   * When we are inside an iterate loop, we will add only WeakReferences. This
   * allows the JVM GC to collect beans, which are not referenced elsewhere. In
   * normal operation, we will use hard references, to avoid performance impact
   */
  private int iterateDepth;

  /**
   * Create a new PersistenceContext.
   */
  public DefaultPersistenceContext() {
  }

  @Override
  public void beginIterate() {
    lock.lock();
    try {
      iterateDepth++;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void endIterate() {
    lock.lock();
    try {
      iterateDepth--;
      expungeStaleEntries(); // when leaving the iterator, cleanup.
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void put(Class<?> rootType, Object id, Object bean) {
    lock.lock();
    try {
      expungeStaleEntries();
      classContext(rootType).useReferences(iterateDepth > 0).put(id, bean);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Object putIfAbsent(Class<?> rootType, Object id, Object bean) {
    lock.lock();
    try {
      expungeStaleEntries();
      return classContext(rootType).useReferences(iterateDepth > 0).putIfAbsent(id, bean);
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
      expungeStaleEntries();
      return classContext(rootType).get(id);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public WithOption getWithOption(Class<?> rootType, Object id) {
    lock.lock();
    try {
      expungeStaleEntries();
      return classContext(rootType).getWithOption(id);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public int size(Class<?> rootType) {
    lock.lock();
    try {
      expungeStaleEntries();
      ClassContext classMap = typeCache.get(rootType);
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
      expungeStaleEntries();
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
      expungeStaleEntries();
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
      expungeStaleEntries();
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
      expungeStaleEntries();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public List<Object> dirtyBeans(SpiBeanTypeManager manager) {
    lock.lock();
    try {
      expungeStaleEntries();
      List<Object> list = new ArrayList<>();
      for (ClassContext classContext : typeCache.values()) {
        classContext.dirtyBeans(manager, list);
      }
      return list;
    } finally {
      lock.unlock();
    }
  }

  /**
   * When there is a queue, poll it to remove stale entries from the map. Note:
   * This is always done AFTER <code>useReferences</code> was called with
   * <code>true</code>. Polling an empty queue has no performance impact.
   */
  private void expungeStaleEntries() {
    Reference<?> ref;
    while ((ref = queue.poll()) != null) {
      ((BeanRef) ref).expunge();
    }
  }

  @Override
  public String toString() {
    lock.lock();
    try {
      expungeStaleEntries();
      return typeCache.toString();
    } finally {
      lock.unlock();
    }
  }

  private ClassContext classContext(Class<?> rootType) {
    return typeCache.computeIfAbsent(rootType, k -> new ClassContext(k, queue));
  }


  private static class ClassContext {

    private final Map<Object, Object> map = new HashMap<>();
    private final Class<?> rootType;
    private final ReferenceQueue<Object> queue;
    private Set<Object> deleteSet;
    private boolean useReferences;
    private int weakCount;
    private int threshold = 0;

    private ClassContext(Class<?> rootType, ReferenceQueue<Object> queue) {
      this.rootType = rootType;
      this.queue = queue;
      this.threshold = ClassContextTracker.INSTANCE.getThreshold(rootType);
    }

    /**
     * When called with "true", initialize referenceQueue and store BeanRefs instead
     * of real object references.
     */
    private ClassContext useReferences(boolean useReferences) {
      this.useReferences = useReferences;
      return this;
    }

    @Override
    public String toString() {
      return "size:" + map.size() + " (" + weakCount + " weak)";
    }

    private Object get(Object id) {
      Object ret = map.get(id);
      if (ret instanceof BeanRef) {
        return ((BeanRef) ret).get();
      } else {
        return ret;
      }
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

    private void put(Object id, Object bean) {
      Object existing;
      if (useReferences) {
        weakCount++;
        existing = map.put(id, new BeanRef(this, id, bean, queue));
      } else {
        existing = map.put(id, bean);
      }
      if (existing instanceof BeanRef) {
        // when a BeanRef is replaced, its expunge() must NOT remove an entry
        ((BeanRef) existing).setReplaced();
        weakCount--;
      }
      if (threshold != -1 && (map.size() - weakCount) > threshold) {
        threshold = ClassContextTracker.INSTANCE.log(rootType, (map.size() - weakCount), threshold);
      }
    }

    private int size() {
      return map.size();
    }

    private void clear() {
      map.clear();
      weakCount = 0;
    }

    private void remove(Object id) {
      Object ret = map.remove(id);
      if (ret instanceof BeanRef) {
        weakCount--;
      }
    }

    private void deleted(Object id) {
      if (deleteSet == null) {
        deleteSet = new HashSet<>();
      }
      deleteSet.add(id);
      remove(id);
    }

    /**
     * Add the dirty beans to the list.
     */
    void dirtyBeans(SpiBeanTypeManager manager, List<Object> list) {
      final SpiBeanType beanType = manager.beanType(rootType);
      for (Object value : map.values()) {
        if (value instanceof BeanRef) {
          value = ((BeanRef) value).get();
          if (value == null) continue;
        }
        EntityBean bean = (EntityBean) value;
        if (bean._ebean_getIntercept().isDirty() || beanType.isToManyDirty(bean)) {
          list.add(value);
        }
      }
    }
  }

  private static class BeanRef extends WeakReference<Object> {

    private final ClassContext classContext;
    private final Object key;
    private boolean replaced;

    private BeanRef(ClassContext classContext, Object key, Object referent, ReferenceQueue<? super Object> q) {
      super(referent, q);
      this.classContext = classContext;
      this.key = key;
    }

    private void setReplaced() {
      replaced = true;
    }

    private void expunge() {
      if (!replaced) {
        classContext.remove(key);
      }
    }

  }
}

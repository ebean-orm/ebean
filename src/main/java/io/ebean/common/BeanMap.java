package io.ebean.common;

import io.ebean.bean.BeanCollection;
import io.ebean.bean.BeanCollectionLoader;
import io.ebean.bean.EntityBean;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Map capable of lazy loading.
 */
public final class BeanMap<K, E> extends AbstractBeanCollection<E> implements Map<K, E> {

  private static final long serialVersionUID = 1L;

  /**
   * The underlying map implementation.
   */
  private Map<K, E> map;

  /**
   * Create with a given Map.
   */
  public BeanMap(Map<K, E> map) {
    this.map = map;
  }

  /**
   * Create using a underlying LinkedHashMap.
   */
  public BeanMap() {
    this(new LinkedHashMap<>());
  }

  public BeanMap(BeanCollectionLoader ebeanServer, EntityBean ownerBean, String propertyName) {
    super(ebeanServer, ownerBean, propertyName);
  }

  @Override
  public void reset(EntityBean ownerBean, String propertyName) {
    this.ownerBean = ownerBean;
    this.propertyName = propertyName;
    this.map = null;
  }

  @Override
  public boolean isSkipSave() {
    return map == null || (map.isEmpty() && !holdsModifications());
  }

  @Override
  @SuppressWarnings("unchecked")
  public void loadFrom(BeanCollection<?> other) {
    BeanMap<K, E> otherMap = (BeanMap<K, E>) other;
    internalPutNull();
    map.putAll(otherMap.getActualMap());
  }

  public void internalPutNull() {
    if (map == null) {
      map = new LinkedHashMap<>();
    }
  }

  @SuppressWarnings("unchecked")
  public void internalPut(Object key, Object bean) {
    if (map == null) {
      map = new LinkedHashMap<>();
    }
    if (key != null) {
      map.put((K) key, (E) bean);
    }
  }

  public void internalPutWithCheck(Object key, Object bean) {
    if (map == null || !map.containsKey(key)) {
      internalPut(key, bean);
    }
  }

  @Override
  public void internalAddWithCheck(Object bean) {
    throw new RuntimeException("Not allowed for map");
  }

  @Override
  public void internalAdd(Object bean) {
    throw new RuntimeException("Not allowed for map");
  }

  /**
   * Return true if the underlying map has been populated. Returns false if it
   * has a deferred fetch pending.
   */
  @Override
  public boolean isPopulated() {
    return map != null;
  }

  /**
   * Return true if this is a reference (lazy loading) bean collection. This is
   * the same as !isPopulated();
   */
  @Override
  public boolean isReference() {
    return map == null;
  }

  @Override
  public boolean checkEmptyLazyLoad() {
    if (map == null) {
      map = new LinkedHashMap<>();
      return true;
    } else {
      return false;
    }
  }

  private void initClear() {
    synchronized (this) {
      if (map == null) {
        if (modifyListening) {
          lazyLoadCollection(true);
        } else {
          map = new LinkedHashMap<>();
        }
      }
    }
  }

  private void init() {
    synchronized (this) {
      if (map == null) {
        lazyLoadCollection(false);
      }
    }
  }

  /**
   * Set the actual underlying map. Used for performing lazy fetch.
   */
  @SuppressWarnings("unchecked")
  public void setActualMap(Map<?, ?> map) {
    this.map = (Map<K, E>) map;
  }

  /**
   * Return the actual underlying map.
   */
  public Map<K, E> getActualMap() {
    return map;
  }

  /**
   * Returns the collection of beans (map values).
   */
  @Override
  public Collection<E> getActualDetails() {
    return map.values();
  }

  /**
   * Returns the map entrySet.
   * <p>
   * This is because the key values may need to be set against the details (so
   * they don't need to be set twice).
   * </p>
   */
  @Override
  public Collection<?> getActualEntries() {
    return map.entrySet();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(50);
    sb.append("BeanMap ");
    if (isReadOnly()) {
      sb.append("readOnly ");
    }
    if (map == null) {
      sb.append("deferred ");

    } else {
      sb.append("size[").append(map.size()).append("]");
      sb.append(" map").append(map);
    }
    return sb.toString();
  }

  /**
   * Equal if obj is a Map and equal in a Map sense.
   */
  @Override
  public boolean equals(Object obj) {
    init();
    return map.equals(obj);
  }

  @Override
  public int hashCode() {
    init();
    return map.hashCode();
  }

  @Override
  public void clear() {
    checkReadOnly();
    initClear();
    if (modifyListening) {
      // add all beans to the removal list
      for (E bean : map.values()) {
        modifyRemoval(bean);
      }
    }
    map.clear();
  }

  @Override
  public boolean containsKey(Object key) {
    init();
    return map.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    init();
    return map.containsValue(value);
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Set<Entry<K, E>> entrySet() {
    init();
    if (isReadOnly()) {
      return Collections.unmodifiableSet(map.entrySet());
    }
    if (modifyListening) {
      Set<Entry<K, E>> s = map.entrySet();
      return new ModifySet(this, s);
    }
    return map.entrySet();
  }

  @Override
  public E get(Object key) {
    init();
    return map.get(key);
  }

  @Override
  public boolean isEmpty() {
    init();
    return map.isEmpty();
  }

  @Override
  public Set<K> keySet() {
    init();
    if (isReadOnly()) {
      return Collections.unmodifiableSet(map.keySet());
    }
    // we don't really care about modifications to the ketSet?
    return map.keySet();
  }

  @Override
  public E put(K key, E value) {
    checkReadOnly();
    init();
    if (modifyListening) {
      E oldBean = map.put(key, value);
      if (value != oldBean) {
        // register the add of the new and the removal of the old
        modifyAddition(value);
        modifyRemoval(oldBean);
      }
      return oldBean;
    } else {
      return map.put(key, value);
    }
  }

  @Override
  public void putAll(Map<? extends K, ? extends E> puts) {
    checkReadOnly();
    init();
    if (modifyListening) {
      for (Entry<? extends K, ? extends E> entry : puts.entrySet()) {
        Object oldBean = map.put(entry.getKey(), entry.getValue());
        if (entry.getValue() != oldBean) {
          modifyAddition(entry.getValue());
          modifyRemoval(oldBean);
        }
      }
    } else {
      map.putAll(puts);
    }
  }

  @Override
  public void addBean(E bean) {
    throw new IllegalStateException("Method not allowed on Map. Please use List instead.");
  }

  @Override
  public void removeBean(E bean) {
    throw new IllegalStateException("Method not allowed on Map. Please use List instead.");
  }

  @Override
  public E remove(Object key) {
    checkReadOnly();
    init();
    if (modifyListening) {
      E o = map.remove(key);
      modifyRemoval(o);
      return o;
    }
    return map.remove(key);
  }

  @Override
  public int size() {
    init();
    return map.size();
  }

  @Override
  public Collection<E> values() {
    init();
    if (isReadOnly()) {
      return Collections.unmodifiableCollection(map.values());
    }
    if (modifyListening) {
      Collection<E> c = map.values();
      return new ModifyCollection<>(this, c);
    }
    return map.values();
  }

  @Override
  public BeanCollection<E> getShallowCopy() {
    BeanMap<K, E> copy = new BeanMap<>(new LinkedHashMap<>(map));
    copy.setFromOriginal(this);
    return copy;
  }
}

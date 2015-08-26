package com.avaje.ebean.common;

import com.avaje.ebean.bean.BeanCollectionLoader;
import com.avaje.ebean.bean.EntityBean;

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
    this(new LinkedHashMap<K, E>());
  }

  public BeanMap(BeanCollectionLoader ebeanServer, EntityBean ownerBean, String propertyName) {
    super(ebeanServer, ownerBean, propertyName);
  }

  @Override
  public void reset(EntityBean ownerBean, String propertyName) {
    this.ownerBean = ownerBean;
    this.propertyName = propertyName;
    this.map = null;
    this.touched = false;
  }

  public boolean isEmptyAndUntouched() {
    return !touched && (map == null || map.isEmpty());
  }

  public void internalPutNull() {
    if (map == null) {
      map = new LinkedHashMap<K, E>();
    }
  }

    @SuppressWarnings("unchecked")
  public void internalPut(Object key, Object bean) {
    if (map == null) {
      map = new LinkedHashMap<K, E>();
    }
    if (key != null) {
      map.put((K) key, (E) bean);
    }
  }
  
  public void internalAdd(Object bean) {
    throw new RuntimeException("Not allowed for map");
  }

  /**
   * Return true if the underlying map has been populated. Returns false if it
   * has a deferred fetch pending.
   */
  public boolean isPopulated() {
    return map != null;
  }

  /**
   * Return true if this is a reference (lazy loading) bean collection. This is
   * the same as !isPopulated();
   */
  public boolean isReference() {
    return map == null;
  }

  public boolean checkEmptyLazyLoad() {
    if (map == null) {
      map = new LinkedHashMap<K, E>();
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
          map = new LinkedHashMap<K, E>();
        }
      }
      touched(true);
    }
  }

  private void initAsUntouched() {
    init(false);
  }
  
  private void init() {
    init(true);
  }
  
  private void init(boolean setTouched) {
    synchronized (this) {
      if (map == null) {
        lazyLoadCollection(false);
      }
      touched(setTouched);
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
  public Collection<?> getActualEntries() {
    return map.entrySet();
  }

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
  public boolean equals(Object obj) {
    init();
    return map.equals(obj);
  }

  public int hashCode() {
    init();
    return map.hashCode();
  }

  public void clear() {
    checkReadOnly();
    initClear();
    if (modifyRemoveListening) {
      // add all beans to the removal list
      for (E bean : map.values()) {
        modifyRemoval(bean);
      }
    }
    map.clear();
  }

  public boolean containsKey(Object key) {
    init();
    return map.containsKey(key);
  }

  public boolean containsValue(Object value) {
    init();
    return map.containsValue(value);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
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

  public E get(Object key) {
    init();
    return map.get(key);
  }

  public boolean isEmpty() {
    initAsUntouched();
    return map.isEmpty();
  }

  public Set<K> keySet() {
    init();
    if (isReadOnly()) {
      return Collections.unmodifiableSet(map.keySet());
    }
    // we don't really care about modifications to the ketSet?
    return map.keySet();
  }

  public E put(K key, E value) {
    checkReadOnly();
    init();
    if (modifyListening) {
      Object oldBean = map.put(key, value);
      if (value != oldBean) {
        // register the add of the new and the removal of the old
        modifyAddition(value);
        modifyRemoval(oldBean);
      }
    }
    return map.put(key, value);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
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
    }
    map.putAll(puts);
  }

  public E remove(Object key) {
    checkReadOnly();
    init();
    if (modifyRemoveListening) {
      E o = map.remove(key);
      modifyRemoval(o);
      return o;
    }
    return map.remove(key);
  }

  public int size() {
    init();
    return map.size();
  }

  public Collection<E> values() {
    init();
    if (isReadOnly()) {
      return Collections.unmodifiableCollection(map.values());
    }
    if (modifyListening) {
      Collection<E> c = map.values();
      return new ModifyCollection<E>(this, c);
    }
    return map.values();
  }

}

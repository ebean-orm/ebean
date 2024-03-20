package io.ebean.common;

import io.ebean.bean.BeanCollection;
import io.ebean.bean.BeanCollectionLoader;
import io.ebean.bean.EntityBean;
import io.ebean.bean.ToStringBuilder;

import java.util.*;

/**
 * Map capable of lazy loading and modification aware.
 */
public final class BeanMap<K, E> extends AbstractBeanCollection<E> implements SequencedMap<K, E> {

  private static final long serialVersionUID = 1L;

  /**
   * The underlying map implementation.
   */
  private LinkedHashMap<K, E> map;

  /**
   * Create with a given Map.
   */
  public BeanMap(LinkedHashMap<K, E> map) {
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
  public void toString(ToStringBuilder builder) {
    if (map == null || map.isEmpty()) {
      builder.addRaw("{}");
    } else {
      builder.addRaw("{");
      for (Entry<K, E> entry : map.entrySet()) {
        builder.add(String.valueOf(entry.getKey()), entry.getValue());
      }
      builder.addRaw("}");
    }
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
    map.putAll(otherMap.actualMap());
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
    if (map == null || key == null || !map.containsKey(key)) {
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
    lock.lock();
    try {
      if (map == null) {
        if (!disableLazyLoad && modifyListening) {
          lazyLoadCollection(true);
        } else {
          map = new LinkedHashMap<>();
        }
      }
    } finally {
      lock.unlock();
    }
  }

  private void init() {
    lock.lock();
    try {
      if (map == null) {
        if (disableLazyLoad) {
          map = new LinkedHashMap<>();
        } else {
          lazyLoadCollection(false);
        }
      }
    } finally {
      lock.unlock();
    }
  }

  public LinkedHashMap<K, E> collectionAdd() {
    if (map == null) {
      map = new LinkedHashMap<>();
    }
    return map;
  }

  @SuppressWarnings("unchecked")
  public void refresh(ModifyListenMode modifyListenMode, BeanMap<?, ?> newMap) {
    setModifyListening(modifyListenMode);
    this.map = (LinkedHashMap<K, E>) newMap.actualMap();
  }

  /**
   * Return the actual underlying map.
   */
  public LinkedHashMap<K, E> actualMap() {
    return map;
  }

  /**
   * Returns the collection of beans (map values).
   */
  @Override
  public Collection<E> actualDetails() {
    return map.values();
  }

  /**
   * Returns the map entrySet.
   */
  @Override
  public Collection<?> actualEntries() {
    return map.entrySet();
  }

  @Override
  public String toString() {
    if (map == null) {
      return "BeanMap<deferred>";
    } else {
      return map.toString();
    }
  }

  /**
   * Equal if object is a Map and equal in a Map sense.
   */
  @Override
  public boolean equals(Object object) {
    init();
    return map.equals(object);
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
  public Set<Entry<K, E>> entrySet() {
    init();
    if (readOnly) {
      return Collections.unmodifiableSet(map.entrySet());
    }
    return modifyListening ? new ModifyEntrySet<>(this, map.entrySet()) : map.entrySet();
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
    if (readOnly) {
      return Collections.unmodifiableSet(map.keySet());
    }
    return modifyListening ? new ModifyKeySet<>(this, map.keySet()) : map.keySet();
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
    if (readOnly) {
      return Collections.unmodifiableCollection(map.values());
    }
    return modifyListening ? new ModifyCollection<>(this, map.values()) : map.values();
  }

  @Override
  public BeanCollection<E> shallowCopy() {
    BeanMap<K, E> copy = new BeanMap<>(new LinkedHashMap<>(map));
    copy.setFromOriginal(this);
    return copy;
  }

  @Override
  public SequencedMap<K, E> reversed() {
    init();
    if (modifyListening) {
      throw new UnsupportedOperationException("Not supported on modify listening map");
    }
    return map.reversed();
  }

  @Override
  public Entry<K, E> firstEntry() {
    init();
    return map.firstEntry();
  }

  @Override
  public Entry<K, E> lastEntry() {
    init();
    return map.lastEntry();
  }

  @Override
  public Entry<K, E> pollFirstEntry() {
    checkReadOnly();
    init();
    var entry = map.pollFirstEntry();
    if (modifyListening && entry != null) {
        modifyRemoval(entry.getValue());
    }
    return entry;
  }

  @Override
  public Entry<K, E> pollLastEntry() {
    checkReadOnly();
    init();
    var entry = map.pollLastEntry();
    if (modifyListening && entry != null) {
      modifyRemoval(entry.getValue());
    }
    return entry;
  }

  @Override
  public E putFirst(K key, E value) {
    checkReadOnly();
    init();
    if (modifyListening) {
      E oldBean = map.putFirst(key, value);
      if (value != oldBean) {
        // register the add of the new and the removal of the old
        modifyAddition(value);
        modifyRemoval(oldBean);
      }
      return oldBean;
    } else {
      return map.putFirst(key, value);
    }
  }

  @Override
  public E putLast(K key, E value) {
    checkReadOnly();
    init();
    if (modifyListening) {
      E oldBean = map.putLast(key, value);
      if (value != oldBean) {
        // register the add of the new and the removal of the old
        modifyAddition(value);
        modifyRemoval(oldBean);
      }
      return oldBean;
    } else {
      return map.putLast(key, value);
    }
  }

  @Override
  public SequencedSet<K> sequencedKeySet() {
    init();
    return map.sequencedKeySet();
  }

  @Override
  public SequencedCollection<E> sequencedValues() {
    init();
    return map.sequencedValues();
  }

  @Override
  public SequencedSet<Entry<K, E>> sequencedEntrySet() {
    init();
    return map.sequencedEntrySet();
  }
}

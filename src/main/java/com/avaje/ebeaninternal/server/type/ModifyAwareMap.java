package com.avaje.ebeaninternal.server.type;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Map that is wraps an underlying map for the purpose of detecting changes.
 */
public class ModifyAwareMap<K,V> implements Map<K,V>, ModifyAwareOwner {

  /**
   * Dirty flag set when the map has been modified.
   */
  private boolean dirty;
  
  /**
   * The underlying map.
   */
  private Map<K,V> map;
  
  public ModifyAwareMap(Map<K,V> underyling) {
    this.map = underyling;
  }

  public String toString() {
    return map.toString();
  }
  
  @Override
  public boolean isMarkedDirty() {
    return dirty;
  }

  @Override
  public void markAsModified() {
    dirty = true;
  }
  
  @Override
  public int size() {
    return map.size();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  @Override
  public V get(Object key) {
    return map.get(key);
  }

  @Override
  public V put(K key, V value) {
    markAsModified();
    return map.put(key, value);
  }

  @Override
  public V remove(Object key) {
    V value = map.remove(key);
    if (value != null) {
      markAsModified();      
    }
    return value;
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    markAsModified();
    map.putAll(m);
  }


  @Override
  public void clear() {
    if (!map.isEmpty()) {
      markAsModified();
    }
    map.clear();
  }

  @Override
  public Set<K> keySet() {
    return new ModifyAwareSet<K>(this, map.keySet());
  }

  @Override
  public Collection<V> values() {
    return new ModifyAwareCollection<V>(this, map.values());
  }

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    return new ModifyAwareSet<Map.Entry<K, V>>(this, map.entrySet());
  }

  
}

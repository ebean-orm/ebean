package io.ebean.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Handle the Key Set for BeanMap.
 */
class ModifyKeySet<E> implements Set<E> {

  private final Set<E> keySet;
  private final BeanMap<E, ?> owner;

  ModifyKeySet(BeanMap<E, ?> owner, Set<E> keySet) {
    this.owner = owner;
    this.keySet = keySet;
  }

  @Override
  public int size() {
    return keySet.size();
  }

  @Override
  public boolean isEmpty() {
    return keySet.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return keySet.contains(o);
  }

  @Override
  public Object[] toArray() {
    return keySet.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return keySet.toArray(a);
  }

  @Override
  public boolean add(E key) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(Collection<? extends E> keys) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove(Object o) {
    return owner.remove(o) != null;
  }

  @Override
  public boolean containsAll(Collection<?> keys) {
    return keySet.containsAll(keys);
  }

  @Override
  public void clear() {
    owner.clear();
  }

  @Override
  public Iterator<E> iterator() {
    return new KeySetIterator<>(new ArrayList<>(keySet).iterator());
  }

  @Override
  public boolean retainAll(Collection<?> keys) {
    return keysMatch(keys, false);
  }

  @Override
  public boolean removeAll(Collection<?> keys) {
    return keysMatch(keys, true);
  }

  private boolean keysMatch(Collection<?> keys, boolean containsMatch) {
    boolean changed = false;
    final Iterator<E> iterator = iterator();
    while (iterator.hasNext()) {
      final E key = iterator.next();
      if (keys.contains(key) == containsMatch) {
        iterator.remove();
        changed = true;
      }
    }
    return changed;
  }


  class KeySetIterator<K> implements Iterator<K> {

    private final Iterator<K> iterator;
    private K key;

    KeySetIterator(Iterator<K> iterator) {
      this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public K next() {
      key = iterator.next();
      return key;
    }

    @Override
    public void remove() {
      owner.remove(key);
      iterator.remove();
    }
  }
}

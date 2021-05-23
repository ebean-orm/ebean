package io.ebean.common;

import java.util.*;

/**
 * Handles the Entry Set for BeanMap.
 */
class ModifyEntrySet<K, E> implements Set<Map.Entry<K, E>> {

  private final BeanMap<K, E> owner;
  private final Set<Map.Entry<K, E>> entrySet;

  ModifyEntrySet(BeanMap<K, E> owner, Set<Map.Entry<K, E>> entrySet) {
    this.owner = owner;
    this.entrySet = entrySet;
  }

  @Override
  public int size() {
    return entrySet.size();
  }

  @Override
  public boolean isEmpty() {
    return entrySet.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return entrySet.contains(o);
  }

  @Override
  public Object[] toArray() {
    return entrySet.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return entrySet.toArray(a);
  }

  @Override
  public boolean containsAll(Collection<?> entries) {
    return entrySet.containsAll(entries);
  }

  @Override
  public void clear() {
    owner.clear();
  }

  @Override
  public boolean add(Map.Entry<K, E> entry) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(Collection<? extends Map.Entry<K, E>> c) {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("rawtypes")
  @Override
  public boolean remove(Object o) {
    if (o instanceof Map.Entry) {
      Map.Entry entry = (Map.Entry) o;
      final E val = owner.get(entry.getKey());
      if (Objects.equals(val, entry.getValue())) {
        owner.remove(entry.getKey());
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean retainAll(Collection<?> entries) {
    boolean modified = false;
    final Iterator<Map.Entry<K, E>> it = iterator();
    while (it.hasNext()) {
      if (!entries.contains(it.next())) {
        it.remove();
        modified = true;
      }
    }
    return modified;
  }

  @Override
  public boolean removeAll(Collection<?> entries) {
    boolean modified = false;
    for (Object entry : entries) {
      modified |= remove(entry);
    }
    return modified;
  }

  @Override
  public Iterator<Map.Entry<K, E>> iterator() {
    return new EntrySetIterator(new ArrayList<>(entrySet).iterator());
  }

  class EntrySetIterator implements Iterator<Map.Entry<K, E>> {

    private final Iterator<Map.Entry<K, E>> iterator;
    private Map.Entry<K, E> entry;

    EntrySetIterator(Iterator<Map.Entry<K, E>> iterator) {
      this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public Map.Entry<K, E> next() {
      entry = iterator.next();
      return entry;
    }

    @Override
    public void remove() {
      owner.remove(entry.getKey());
      iterator.remove();
    }
  }

}

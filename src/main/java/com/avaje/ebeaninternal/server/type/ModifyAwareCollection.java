package com.avaje.ebeaninternal.server.type;

import java.util.Collection;
import java.util.Iterator;

/**
 * Wraps a collection for the purposes of detecting modifications.
 */
public class ModifyAwareCollection<E> implements Collection<E> {

  protected final ModifyAwareOwner owner;

  protected final Collection<E> c;

  /**
   * Create with an Owner and the underlying collection this wraps.
   * <p>
   * The owner is notified of the additions and removals.
   * </p>
   */
  public ModifyAwareCollection(ModifyAwareOwner owner, Collection<E> c) {
    this.owner = owner;
    this.c = c;
  }
  
  public String toString() {
    return c.toString();
  }

  public boolean add(E o) {
    if (c.add(o)) {
      owner.markAsModified();
      return true;
    }
    return false;
  }

  public boolean addAll(Collection<? extends E> collection) {
    boolean changed = false;
    Iterator<? extends E> it = collection.iterator();
    while (it.hasNext()) {
      E o = it.next();
      if (c.add(o)) {
        owner.markAsModified();
        changed = true;
      }
    }
    return changed;
  }

  public void clear() {
    if (!c.isEmpty()) {
      owner.markAsModified();
    }
    c.clear();
  }

  public boolean contains(Object o) {
    return c.contains(o);
  }

  public boolean containsAll(Collection<?> collection) {
    return c.containsAll(collection);
  }

  public boolean isEmpty() {
    return c.isEmpty();
  }

  public Iterator<E> iterator() {
    return new ModifyAwareIterator<E>(owner,  c.iterator());
  }

  public boolean remove(Object o) {
    if (c.remove(o)) {
      owner.markAsModified();
      return true;
    }
    return false;
  }

  public boolean removeAll(Collection<?> collection) {
    boolean changed = false;
    Iterator<?> it = collection.iterator();
    while (it.hasNext()) {
      Object o = (Object) it.next();
      if (c.remove(o)) {
        owner.markAsModified();
        changed = true;
      }
    }
    return changed;
  }

  public boolean retainAll(Collection<?> collection) {
    boolean changed = false;
    Iterator<?> it = c.iterator();
    while (it.hasNext()) {
      Object o = (Object) it.next();
      if (!collection.contains(o)) {
        it.remove();
        owner.markAsModified();
        changed = true;
      }
    }
    return changed;
  }

  public int size() {
    return c.size();
  }

  public Object[] toArray() {
    return c.toArray();
  }

  public <T> T[] toArray(T[] a) {
    return c.toArray(a);
  }

}

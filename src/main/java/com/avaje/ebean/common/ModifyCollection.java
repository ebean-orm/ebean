package com.avaje.ebean.common;

import java.util.Collection;
import java.util.Iterator;

import com.avaje.ebean.bean.BeanCollection;

/**
 * Wraps a collection for the purposes of notifying removals and additions to
 * the BeanCollection owner.
 * <p>
 * This is required for persisting ManyToMany objects. Additions and removals
 * become inserts and deletes to the intersection table.
 * </p>
 */
class ModifyCollection<E> implements Collection<E> {

  protected final BeanCollection<E> owner;

  protected final Collection<E> c;

  /**
   * Create with an Owner and the underlying collection this wraps.
   * <p>
   * The owner is notified of the additions and removals.
   * </p>
   */
  public ModifyCollection(BeanCollection<E> owner, Collection<E> c) {
    this.owner = owner;
    this.c = c;
  }

  public boolean add(E o) {
    if (c.add(o)) {
      owner.modifyAddition(o);
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
        owner.modifyAddition(o);
        changed = true;
      }
    }
    return changed;
  }

  public void clear() {
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
    Iterator<E> it = c.iterator();
    return new ModifyIterator<E>(owner, it);
  }

  public boolean remove(Object o) {
    if (c.remove(o)) {
      owner.modifyRemoval(o);
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
        owner.modifyRemoval(o);
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
        owner.modifyRemoval(o);
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

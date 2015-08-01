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
    for (E o : collection) {
      if (c.add(o)) {
        owner.modifyAddition(o);
        changed = true;
      }
    }
    return changed;
  }

  @Override
  public void clear() {
    c.clear();
  }

  @Override
  public boolean contains(Object o) {
    return c.contains(o);
  }

  @Override
  public boolean containsAll(Collection<?> collection) {
    return c.containsAll(collection);
  }

  @Override
  public boolean isEmpty() {
    return c.isEmpty();
  }

  @Override
  public Iterator<E> iterator() {
    Iterator<E> it = c.iterator();
    return new ModifyIterator<E>(owner, it);
  }

  @Override
  public boolean remove(Object o) {
    if (c.remove(o)) {
      owner.modifyRemoval(o);
      return true;
    }
    return false;
  }

  @Override
  public boolean removeAll(Collection<?> collection) {
    boolean changed = false;
    for (Object bean : collection) {
      if (c.remove(bean)) {
        owner.modifyRemoval(bean);
        changed = true;
      }
    }
    return changed;
  }

  @Override
  public boolean retainAll(Collection<?> collection) {
    boolean changed = false;
    Iterator<?> it = c.iterator();
    while (it.hasNext()) {
      Object bean = it.next();
      if (!collection.contains(bean)) {
        // not retaining this bean so add to removals
        it.remove();
        owner.modifyRemoval(bean);
        changed = true;
      }
    }
    return changed;
  }

  @Override
  public int size() {
    return c.size();
  }

  @Override
  public Object[] toArray() {
    return c.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    //noinspection SuspiciousToArrayCall
    return c.toArray(a);
  }

}

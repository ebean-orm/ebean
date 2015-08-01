package com.avaje.ebean.common;

import java.util.ListIterator;

import com.avaje.ebean.bean.BeanCollection;

/**
 * Wraps a ListIterator for the purposes of notifying removals and additions to
 * the BeanCollection owner.
 * <p>
 * This is required for persisting ManyToMany objects. Additions and removals
 * become inserts and deletes to the intersection table.
 * </p>
 */
class ModifyListIterator<E> implements ListIterator<E> {

  private final BeanCollection<E> owner;

  private final ListIterator<E> it;

  private E last;

  /**
   * Create with an Owner that is notified of any additions or deletions.
   */
  ModifyListIterator(BeanCollection<E> owner, ListIterator<E> it) {
    this.owner = owner;
    this.it = it;
  }

  public void add(E bean) {
    owner.modifyAddition(bean);
    last = null;
    it.add(bean);
  }

  public boolean hasNext() {
    return it.hasNext();
  }

  public boolean hasPrevious() {
    return it.hasPrevious();
  }

  public E next() {
    last = it.next();
    return last;
  }

  public int nextIndex() {
    return it.nextIndex();
  }

  public E previous() {
    last = it.previous();
    return last;
  }

  public int previousIndex() {
    return it.previousIndex();
  }

  public void remove() {
    owner.modifyRemoval(last);
    last = null;
    it.remove();
  }

  public void set(E o) {
    if (last != null) {
      owner.modifyRemoval(last);
      owner.modifyAddition(o);
    }
    it.set(o);
  }

}

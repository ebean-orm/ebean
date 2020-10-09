package io.ebean.common;

import io.ebean.bean.BeanCollection;

import java.util.ListIterator;

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

  @Override
  public void add(E bean) {
    owner.modifyAddition(bean);
    last = null;
    it.add(bean);
  }

  @Override
  public boolean hasNext() {
    return it.hasNext();
  }

  @Override
  public boolean hasPrevious() {
    return it.hasPrevious();
  }

  @Override
  public E next() {
    last = it.next();
    return last;
  }

  @Override
  public int nextIndex() {
    return it.nextIndex();
  }

  @Override
  public E previous() {
    last = it.previous();
    return last;
  }

  @Override
  public int previousIndex() {
    return it.previousIndex();
  }

  @Override
  public void remove() {
    owner.modifyRemoval(last);
    last = null;
    it.remove();
  }

  @Override
  public void set(E o) {
    if (last != null) {
      owner.modifyRemoval(last);
      owner.modifyAddition(o);
    }
    it.set(o);
  }

}

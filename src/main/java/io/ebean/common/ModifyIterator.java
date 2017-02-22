package io.ebean.common;

import io.ebean.bean.BeanCollection;

import java.util.Iterator;

/**
 * Wraps an iterator for the purposes of notifying removals and additions to the
 * BeanCollection owner.
 * <p>
 * This is required for persisting ManyToMany objects. Additions and removals
 * become inserts and deletes to the intersection table.
 * </p>
 */
class ModifyIterator<E> implements Iterator<E> {

  private final BeanCollection<E> owner;

  private final Iterator<E> it;

  private E last;

  /**
   * Create with an Owner and the underlying Iterator this wraps.
   * <p>
   * The owner is notified of the removals.
   * </p>
   */
  ModifyIterator(BeanCollection<E> owner, Iterator<E> it) {
    this.owner = owner;
    this.it = it;
  }

  @Override
  public boolean hasNext() {
    return it.hasNext();
  }

  @Override
  public E next() {
    last = it.next();
    return last;
  }

  @Override
  public void remove() {
    owner.modifyRemoval(last);
    it.remove();
  }

}

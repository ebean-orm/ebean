package com.avaje.ebeaninternal.server.type;

import java.util.Iterator;

/**
 * Wraps an iterator for the purposes of detecting modifications.
 */
public class ModifyAwareIterator<E> implements Iterator<E> {

  private final ModifyAwareOwner owner;

  private final Iterator<E> it;

  /**
   * Create with an Owner and the underlying Iterator this wraps.
   * <p>
   * The owner is notified of the removals.
   * </p>
   */
  public ModifyAwareIterator(ModifyAwareOwner owner, Iterator<E> it) {
    this.owner = owner;
    this.it = it;
  }

  public boolean hasNext() {
    return it.hasNext();
  }

  public E next() {
    return it.next();
  }

  public void remove() {
    owner.markAsModified();
    it.remove();
  }

}

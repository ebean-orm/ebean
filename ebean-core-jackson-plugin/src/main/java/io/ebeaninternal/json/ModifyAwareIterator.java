package io.ebeaninternal.json;

import java.util.Iterator;

import io.ebean.ModifyAwareType;

/** Wraps an iterator for the purposes of detecting modifications. */
public final class ModifyAwareIterator<E> implements Iterator<E> {

  private final ModifyAwareType owner;
  private final Iterator<E> it;

  /**
   * Create with an Owner and the underlying Iterator this wraps.
   *
   * <p>The owner is notified of the removals.
   */
  public ModifyAwareIterator(ModifyAwareType owner, Iterator<E> it) {
    this.owner = owner;
    this.it = it;
  }

  @Override
  public boolean hasNext() {
    return it.hasNext();
  }

  @Override
  public E next() {
    return it.next();
  }

  @Override
  public void remove() {
    owner.setMarkedDirty(true);
    it.remove();
  }
}

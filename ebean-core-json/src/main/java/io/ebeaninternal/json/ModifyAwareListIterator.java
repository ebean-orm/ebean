package io.ebeaninternal.json;

import io.ebean.ModifyAwareType;

import java.util.ListIterator;

/**
 * Modify aware wrapper of a ListIterator.
 */
public final class ModifyAwareListIterator<E> implements ListIterator<E> {

  final ModifyAwareType owner;
  final ListIterator<E> iterator;

  public ModifyAwareListIterator(ModifyAwareType owner, ListIterator<E> iterator) {
    this.owner = owner;
    this.iterator = iterator;
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public E next() {
    return iterator.next();
  }

  @Override
  public boolean hasPrevious() {
    return iterator.hasPrevious();
  }

  @Override
  public E previous() {
    return iterator.previous();
  }

  @Override
  public int nextIndex() {
    return iterator.nextIndex();
  }

  @Override
  public int previousIndex() {
    return iterator.previousIndex();
  }

  @Override
  public void remove() {
    owner.setMarkedDirty(true);
    iterator.remove();
  }

  @Override
  public void set(E e) {
    owner.setMarkedDirty(true);
    iterator.set(e);
  }

  @Override
  public void add(E e) {
    owner.setMarkedDirty(true);
    iterator.add(e);
  }
}

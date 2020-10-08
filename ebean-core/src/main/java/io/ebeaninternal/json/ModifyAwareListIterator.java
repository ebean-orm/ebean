package io.ebeaninternal.json;

import java.util.ListIterator;

/**
 * Modify aware wrapper of a ListIterator.
 */
public class ModifyAwareListIterator<E> implements ListIterator<E> {

  final ModifyAwareOwner owner;

  final ListIterator<E> iterator;

  public ModifyAwareListIterator(ModifyAwareOwner owner, ListIterator<E> iterator) {
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
    owner.markAsModified();
    iterator.remove();
  }

  @Override
  public void set(E e) {
    owner.markAsModified();
    iterator.set(e);
  }

  @Override
  public void add(E e) {
    owner.markAsModified();
    iterator.add(e);
  }
}

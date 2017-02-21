package io.ebean.common;

import io.ebean.bean.BeanCollection;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * Wraps a List for the purposes of notifying removals and additions to the
 * BeanCollection owner.
 * <p>
 * This is required for persisting ManyToMany objects. Additions and removals
 * become inserts and deletes to the intersection table.
 * </p>
 * <p>
 * Note that this is created by a call to subList() on a BeanList. Thats its
 * only purpose really. BeanList holds the actual List.
 * </p>
 */
class ModifyList<E> extends ModifyCollection<E> implements List<E> {

  /**
   * The underlying list.
   */
  private final List<E> list;

  /**
   * Create with an Owner that is notified of any additions or deletions.
   */
  ModifyList(BeanCollection<E> owner, List<E> list) {
    super(owner, list);
    this.list = list;
  }

  @Override
  public void add(int index, E element) {
    list.add(index, element);
    owner.modifyAddition(element);
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> addCollection) {
    if (list.addAll(index, addCollection)) {
      for (E bean : addCollection) {
        owner.modifyAddition(bean);
      }
      return true;
    }
    return false;
  }

  @Override
  public E get(int index) {
    return list.get(index);
  }

  @Override
  public int indexOf(Object o) {
    return list.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return list.lastIndexOf(o);
  }

  @Override
  public ListIterator<E> listIterator() {
    return new ModifyListIterator<>(owner, list.listIterator());
  }

  @Override
  public ListIterator<E> listIterator(int index) {
    return new ModifyListIterator<>(owner, list.listIterator(index));
  }

  @Override
  public E remove(int index) {
    E o = list.remove(index);
    owner.modifyRemoval(o);
    return o;
  }

  @Override
  public E set(int index, E element) {
    E o = list.set(index, element);
    owner.modifyAddition(element);
    owner.modifyRemoval(o);
    return o;
  }

  @Override
  public List<E> subList(int fromIndex, int toIndex) {
    return new ModifyList<>(owner, list.subList(fromIndex, toIndex));
  }

}

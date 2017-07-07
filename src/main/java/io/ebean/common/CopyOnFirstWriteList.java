package io.ebean.common;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * List that copies itself on first write access. Needed to keep memory footprint low and the ability
 * to modify lists from cache.
 * 
 * @author Roland Praml, FOCONIS AG
 */
public final class CopyOnFirstWriteList<E> extends AbstractList<E> implements List<E>, Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * The underlying List implementation.
   */
  private List<E> list;
  
  
  public CopyOnFirstWriteList(List<E> list) {
    super();
    this.list = list;
  }

  private volatile boolean copied = false;

  @Override
  public int size() {
    return list.size();
  }

  @Override
  public boolean isEmpty() {
    return list.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return list.contains(o);
  }

  @Override
  public Object[] toArray() {
    return list.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return list.toArray(a);
  }

  @Override
  public boolean add(E e) {
    checkCopyOnWrite();
    return list.add(e);
  }

  @Override
  public boolean remove(Object o) {
    checkCopyOnWrite();
    return list.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return list.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    checkCopyOnWrite();
    return list.addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    checkCopyOnWrite();
    return list.addAll(index, c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    checkCopyOnWrite();
    return list.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    checkCopyOnWrite();
    return list.retainAll(c);
  }

  @Override
  public void replaceAll(UnaryOperator<E> operator) {
    checkCopyOnWrite();
    list.replaceAll(operator);
  }

  @Override
  public boolean removeIf(Predicate<? super E> filter) {
    checkCopyOnWrite();
    return list.removeIf(filter);
  }

  @Override
  public void sort(Comparator<? super E> c) {
    checkCopyOnWrite();
    list.sort(c);
  }

  @Override
  public void clear() {
    if (!copied) {
      list = new ArrayList<>();
      copied = true;
    }
  }

  @Override
  public boolean equals(Object o) {
    return list.equals(o);
  }

  @Override
  public int hashCode() {
    return list.hashCode();
  }

  @Override
  public E get(int index) {
    return list.get(index);
  }

  @Override
  public E set(int index, E element) {
    checkCopyOnWrite();
    return list.set(index, element);
  }

  @Override
  public void add(int index, E element) {
    checkCopyOnWrite();
    list.add(index, element);
  }

  @Override
  public E remove(int index) {
    checkCopyOnWrite();
    return list.remove(index);
  }

  @Override
  public int indexOf(Object o) {
    return list.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return list.lastIndexOf(o);
  }
  
  private void checkCopyOnWrite() {
    if (!copied) {
      synchronized (this) {
        if (!copied) {
          list = new ArrayList<>(list);
          copied = true;
        }
      }
    }
  }
}

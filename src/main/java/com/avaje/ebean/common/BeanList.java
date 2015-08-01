package com.avaje.ebean.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.avaje.ebean.bean.BeanCollectionAdd;
import com.avaje.ebean.bean.BeanCollectionLoader;
import com.avaje.ebean.bean.EntityBean;

/**
 * List capable of lazy loading.
 */
public final class BeanList<E> extends AbstractBeanCollection<E> implements List<E>, BeanCollectionAdd {

  private static final long serialVersionUID = 1L;
  
  /**
   * The underlying List implementation.
   */
  private List<E> list;

  /**
   * Specify the underlying List implementation.
   */
  public BeanList(List<E> list) {
    super();
    this.list = list;
  }

  /**
   * Uses an ArrayList as the underlying List implementation.
   */
  public BeanList() {
    this(new ArrayList<E>());
  }

  /**
   * Used to create deferred fetch proxy.
   */
  public BeanList(BeanCollectionLoader loader, EntityBean ownerBean, String propertyName) {
    super(loader, ownerBean, propertyName);
  }

  @Override
  public void reset(EntityBean ownerBean, String propertyName) {
    this.ownerBean = ownerBean;
    this.propertyName = propertyName;
    this.list = null;
    this.touched = false;
  }

  @Override
  public boolean isEmptyAndUntouched() {
    return !touched && (list == null || list.isEmpty());
  }

  @SuppressWarnings("unchecked")
  public void addBean(EntityBean bean) {
    list.add((E) bean);
  }

  @SuppressWarnings("unchecked")
  public void internalAdd(Object bean) {
    if (list == null) {
      list = new ArrayList<E>();
    }
    if (bean != null) {
      list.add((E) bean);
    }
  }

  public boolean checkEmptyLazyLoad() {
    if (list == null) {
      list = new ArrayList<E>();
      return true;
    } else {
      return false;
    }
  }

  private void initClear() {
    synchronized (this) {
      if (list == null) {
        if (modifyListening) {
          lazyLoadCollection(true);
        } else {
          list = new ArrayList<E>();
        }
      }
      touched(true);
    }
  }

  private void initAsUntouched() {
    init(false);
  }
  
  private void init() {
    init(true);
  }
  
  private void init(boolean setTouched) {
    synchronized (this) {
      if (list == null) {
        lazyLoadCollection(false);
      }
      touched(setTouched);
    }
  }

  /**
   * Set the actual underlying list.
   * <p>
   * This is primarily for the deferred fetching function.
   * </p>
   */
  @SuppressWarnings("unchecked")
  public void setActualList(List<?> list) {
    this.list = (List<E>) list;
  }

  /**
   * Return the actual underlying list.
   */
  public List<E> getActualList() {
    return list;
  }

  public Collection<E> getActualDetails() {
    return list;
  }
  
  @Override
  public Collection<?> getActualEntries() {
    return list;
  }

  /**
   * Return true if the underlying list is populated.
   */
  public boolean isPopulated() {
    return list != null;
  }

  /**
   * Return true if this is a reference (lazy loading) bean collection. This is
   * the same as !isPopulated();
   */
  public boolean isReference() {
    return list == null;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder(50);
    sb.append("BeanList ");
    if (isReadOnly()) {
      sb.append("readOnly ");
    }
    if (list == null) {
      sb.append("deferred ");

    } else {
      sb.append("size[").append(list.size()).append("] ");
      sb.append("list").append(list).append("");
    }
    return sb.toString();
  }

  /**
   * Equal if obj is a List and equal in a list sense.
   * <p>
   * Specifically obj does not need to be a BeanList but any list. This does not
   * use the FindMany, fetchedMaxRows or finishedFetch properties in the equals
   * test.
   * </p>
   */
  public boolean equals(Object obj) {
    init();
    return list.equals(obj);
  }

  public int hashCode() {
    init();
    return list.hashCode();
  }

  // -----------------------------------------------------//
  // The additional methods are here
  // -----------------------------------------------------//

  // -----------------------------------------------------//
  // proxy method for List
  // -----------------------------------------------------//

  public void add(int index, E element) {
    checkReadOnly();
    init();
    if (modifyAddListening) {
      modifyAddition(element);
    }
    list.add(index, element);
  }

  public boolean add(E o) {
    checkReadOnly();
    init();
    if (modifyAddListening) {
      if (list.add(o)) {
        modifyAddition(o);
        return true;
      } else {
        return false;
      }
    }
    return list.add(o);
  }

  public boolean addAll(Collection<? extends E> c) {
    checkReadOnly();
    init();
    if (modifyAddListening) {
      // all elements in c are added (no contains checking)
      getModifyHolder().modifyAdditionAll(c);
    }
    return list.addAll(c);
  }

  public boolean addAll(int index, Collection<? extends E> c) {
    checkReadOnly();
    init();
    if (modifyAddListening) {
      // all elements in c are added (no contains checking)
      getModifyHolder().modifyAdditionAll(c);
    }
    return list.addAll(index, c);
  }

  public void clear() {
    checkReadOnly();
    // TODO: when clear() and not initialised could be more clever
    // and fetch just the Id's
    initClear();
    if (modifyRemoveListening) {
      for (int i = 0; i < list.size(); i++) {
        getModifyHolder().modifyRemoval(list.get(i));
      }
    }
    list.clear();
  }

  public boolean contains(Object o) {
    init();
    return list.contains(o);
  }

  public boolean containsAll(Collection<?> c) {
    init();
    return list.containsAll(c);
  }

  public E get(int index) {
    init();
    return list.get(index);
  }

  public int indexOf(Object o) {
    init();
    return list.indexOf(o);
  }

  public boolean isEmpty() {
    initAsUntouched();
    return list.isEmpty();
  }

  public Iterator<E> iterator() {
    init();
    if (isReadOnly()) {
      return new ReadOnlyListIterator<E>(list.listIterator());
    }
    if (modifyListening) {
      Iterator<E> it = list.iterator();
      return new ModifyIterator<E>(this, it);
    }
    return list.iterator();
  }

  public int lastIndexOf(Object o) {
    init();
    return list.lastIndexOf(o);
  }

  public ListIterator<E> listIterator() {
    init();
    if (isReadOnly()) {
      return new ReadOnlyListIterator<E>(list.listIterator());
    }
    if (modifyListening) {
      ListIterator<E> it = list.listIterator();
      return new ModifyListIterator<E>(this, it);
    }
    return list.listIterator();
  }

  public ListIterator<E> listIterator(int index) {
    init();
    if (isReadOnly()) {
      return new ReadOnlyListIterator<E>(list.listIterator(index));
    }
    if (modifyListening) {
      ListIterator<E> it = list.listIterator(index);
      return new ModifyListIterator<E>(this, it);
    }
    return list.listIterator(index);
  }

  public E remove(int index) {
    checkReadOnly();
    init();
    if (modifyRemoveListening) {
      E o = list.remove(index);
      modifyRemoval(o);
      return o;
    }
    return list.remove(index);
  }

  public boolean remove(Object o) {
    checkReadOnly();
    init();
    if (modifyRemoveListening) {
      boolean isRemove = list.remove(o);
      if (isRemove) {
        modifyRemoval(o);
      }
      return isRemove;
    }
    return list.remove(o);
  }

  public boolean removeAll(Collection<?> beans) {
    checkReadOnly();
    init();
    if (modifyRemoveListening) {
      boolean changed = false;
      for (Object bean : beans) {
        if (list.remove(bean)) {
          // register this bean as having been removed
          modifyRemoval(bean);
          changed = true;
        }
      }
      return changed;
    }
    return list.removeAll(beans);
  }

  public boolean retainAll(Collection<?> retainBeans) {
    checkReadOnly();
    init();
    if (modifyRemoveListening) {
      boolean changed = false;
      Iterator<E> it = list.iterator();
      while (it.hasNext()) {
        Object bean = it.next();
        if (!retainBeans.contains(bean)) {
          // removing this bean
          it.remove();
          modifyRemoval(bean);
          changed = true;
        }
      }
      return changed;
    }
    return list.retainAll(retainBeans);
  }

  public E set(int index, E element) {
    checkReadOnly();
    init();
    if (modifyListening) {
      E o = list.set(index, element);
      modifyAddition(element);
      modifyRemoval(o);
      return o;
    }
    return list.set(index, element);
  }

  public int size() {
    init();
    return list.size();
  }

  public List<E> subList(int fromIndex, int toIndex) {
    init();
    if (isReadOnly()) {
      return Collections.unmodifiableList(list.subList(fromIndex, toIndex));
    }
    if (modifyListening) {
      return new ModifyList<E>(this, list.subList(fromIndex, toIndex));
    }
    return list.subList(fromIndex, toIndex);
  }

  public Object[] toArray() {
    init();
    return list.toArray();
  }

  public <T> T[] toArray(T[] a) {
    init();
    //noinspection SuspiciousToArrayCall
    return list.toArray(a);
  }

  private static class ReadOnlyListIterator<E> implements ListIterator<E>, Serializable {

    private static final long serialVersionUID = 3097271091406323699L;

    private final ListIterator<E> i;

    ReadOnlyListIterator(ListIterator<E> i) {
      this.i = i;
    }

    public void add(E o) {
      throw new IllegalStateException("This collection is in ReadOnly mode");
    }

    public void remove() {
      throw new IllegalStateException("This collection is in ReadOnly mode");
    }

    public void set(E o) {
      throw new IllegalStateException("This collection is in ReadOnly mode");
    }

    public boolean hasNext() {
      return i.hasNext();
    }

    public boolean hasPrevious() {
      return i.hasPrevious();
    }

    public E next() {
      return i.next();
    }

    public int nextIndex() {
      return i.nextIndex();
    }

    public E previous() {
      return i.previous();
    }

    public int previousIndex() {
      return i.previousIndex();
    }

  }
}

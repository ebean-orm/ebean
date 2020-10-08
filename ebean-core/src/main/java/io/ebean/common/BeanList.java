package io.ebean.common;

import io.ebean.bean.BeanCollection;
import io.ebean.bean.BeanCollectionAdd;
import io.ebean.bean.BeanCollectionLoader;
import io.ebean.bean.EntityBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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
    this(new ArrayList<>());
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
  }

  @Override
  public boolean isSkipSave() {
    return list == null || (list.isEmpty() && !holdsModifications());
  }

  @Override
  @SuppressWarnings("unchecked")
  public void addEntityBean(EntityBean bean) {
    list.add((E) bean);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void loadFrom(BeanCollection<?> other) {
    if (list == null) {
      list = new ArrayList<>();
    }
    list.addAll((Collection<? extends E>) other.getActualDetails());
  }

  @Override
  @SuppressWarnings("unchecked")
  public void internalAdd(Object bean) {
    if (list == null) {
      list = new ArrayList<>();
    }
    if (bean != null) {
      list.add((E) bean);
    }
  }

  @Override
  public void internalAddWithCheck(Object bean) {
    if (list == null || !containsInstance(bean)) {
      internalAdd(bean);
    }
  }

  /**
   * Contains using instance equality for List (specifically not .equals() based).
   */
  private boolean containsInstance(Object bean) {
    for (Object element : list) {
      if (element == bean) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean checkEmptyLazyLoad() {
    if (list == null) {
      list = new ArrayList<>();
      return true;
    } else {
      return false;
    }
  }

  private void initClear() {
    synchronized (this) {
      if (list == null) {
        if (!disableLazyLoad && modifyListening) {
          lazyLoadCollection(true);
        } else {
          list = new ArrayList<>();
        }
      }
    }
  }

  private void init() {
    synchronized (this) {
      if (list == null) {
        if (disableLazyLoad) {
          list = new ArrayList<>();
        } else {
          lazyLoadCollection(false);
        }
      }
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

  @Override
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
  @Override
  public boolean isPopulated() {
    return list != null;
  }

  /**
   * Return true if this is a reference (lazy loading) bean collection. This is
   * the same as !isPopulated();
   */
  @Override
  public boolean isReference() {
    return list == null;
  }

  @Override
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
  @Override
  public boolean equals(Object obj) {
    init();
    return list.equals(obj);
  }

  @Override
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

  @Override
  public void add(int index, E element) {
    checkReadOnly();
    init();
    if (modifyListening) {
      modifyAddition(element);
    }
    list.add(index, element);
  }

  @Override
  public void addBean(E bean) {
    add(bean);
  }

  @Override
  public boolean add(E o) {
    checkReadOnly();
    init();
    if (modifyListening) {
      if (list.add(o)) {
        modifyAddition(o);
        return true;
      } else {
        return false;
      }
    }
    return list.add(o);
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    checkReadOnly();
    init();
    if (modifyListening) {
      // all elements in c are added (no contains checking)
      getModifyHolder().modifyAdditionAll(c);
    }
    return list.addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    checkReadOnly();
    init();
    if (modifyListening) {
      // all elements in c are added (no contains checking)
      getModifyHolder().modifyAdditionAll(c);
    }
    return list.addAll(index, c);
  }

  @Override
  public void clear() {
    checkReadOnly();
    // TODO: when clear() and not initialised could be more clever
    // and fetch just the Id's
    initClear();
    if (modifyListening) {
      for (E aList : list) {
        getModifyHolder().modifyRemoval(aList);
      }
    }
    list.clear();
  }

  @Override
  public boolean contains(Object o) {
    init();
    return list.contains(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    init();
    return list.containsAll(c);
  }

  @Override
  public E get(int index) {
    init();
    return list.get(index);
  }

  @Override
  public int indexOf(Object o) {
    init();
    return list.indexOf(o);
  }

  @Override
  public boolean isEmpty() {
    init();
    return list.isEmpty();
  }

  @Override
  public Iterator<E> iterator() {
    init();
    if (isReadOnly()) {
      return new ReadOnlyListIterator<>(list.listIterator());
    }
    if (modifyListening) {
      Iterator<E> it = list.iterator();
      return new ModifyIterator<>(this, it);
    }
    return list.iterator();
  }

  @Override
  public int lastIndexOf(Object o) {
    init();
    return list.lastIndexOf(o);
  }

  @Override
  public ListIterator<E> listIterator() {
    init();
    if (isReadOnly()) {
      return new ReadOnlyListIterator<>(list.listIterator());
    }
    if (modifyListening) {
      ListIterator<E> it = list.listIterator();
      return new ModifyListIterator<>(this, it);
    }
    return list.listIterator();
  }

  @Override
  public ListIterator<E> listIterator(int index) {
    init();
    if (isReadOnly()) {
      return new ReadOnlyListIterator<>(list.listIterator(index));
    }
    if (modifyListening) {
      ListIterator<E> it = list.listIterator(index);
      return new ModifyListIterator<>(this, it);
    }
    return list.listIterator(index);
  }

  @Override
  public void removeBean(E bean) {
    if (list.remove(bean)) {
      getModifyHolder().modifyRemoval(bean);
    }
  }

  @Override
  public E remove(int index) {
    checkReadOnly();
    init();
    if (modifyListening) {
      E o = list.remove(index);
      modifyRemoval(o);
      return o;
    }
    return list.remove(index);
  }

  @Override
  public boolean remove(Object o) {
    checkReadOnly();
    init();
    if (modifyListening) {
      boolean isRemove = list.remove(o);
      if (isRemove) {
        modifyRemoval(o);
      }
      return isRemove;
    }
    return list.remove(o);
  }

  @Override
  public boolean removeAll(Collection<?> beans) {
    checkReadOnly();
    init();
    if (modifyListening) {
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

  @Override
  public boolean retainAll(Collection<?> retainBeans) {
    checkReadOnly();
    init();
    if (modifyListening) {
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

  @Override
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

  @Override
  public int size() {
    init();
    return list.size();
  }

  @Override
  public List<E> subList(int fromIndex, int toIndex) {
    init();
    if (isReadOnly()) {
      return Collections.unmodifiableList(list.subList(fromIndex, toIndex));
    }
    if (modifyListening) {
      return new ModifyList<>(this, list.subList(fromIndex, toIndex));
    }
    return list.subList(fromIndex, toIndex);
  }

  @Override
  public Object[] toArray() {
    init();
    return list.toArray();
  }

  @Override
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

    @Override
    public void add(E o) {
      throw new IllegalStateException("This collection is in ReadOnly mode");
    }

    @Override
    public void remove() {
      throw new IllegalStateException("This collection is in ReadOnly mode");
    }

    @Override
    public void set(E o) {
      throw new IllegalStateException("This collection is in ReadOnly mode");
    }

    @Override
    public boolean hasNext() {
      return i.hasNext();
    }

    @Override
    public boolean hasPrevious() {
      return i.hasPrevious();
    }

    @Override
    public E next() {
      return i.next();
    }

    @Override
    public int nextIndex() {
      return i.nextIndex();
    }

    @Override
    public E previous() {
      return i.previous();
    }

    @Override
    public int previousIndex() {
      return i.previousIndex();
    }

  }

  @Override
  public BeanCollection<E> getShallowCopy() {
    BeanList<E> copy = new BeanList<>(new CopyOnFirstWriteList<>(list));
    copy.setFromOriginal(this);
    return copy;
  }
}

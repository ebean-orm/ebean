package io.ebean.common;

import io.ebean.bean.*;

import java.util.*;

/**
 * List capable of lazy loading and modification awareness.
 */
public class BeanList<E> extends AbstractBeanCollection<E> implements List<E>, BeanCollectionAdd {

  private static final long serialVersionUID = 1L;

  /**
   * The underlying List implementation.
   */
  List<E> list;

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
  public List<E> freeze() {
    if (list == null) {
      return null;
    } else if (list.isEmpty()) {
      return List.of();
    } else {
      return Collections.unmodifiableList(list);
    }
  }

  @Override
  public void toString(ToStringBuilder builder) {
    builder.addCollection(list);
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
    list.addAll((Collection<? extends E>) other.actualDetails());
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
    if (list == null || bean == null || !containsInstance(bean)) {
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

  protected void initList(boolean skipLoad, boolean onlyIds) {
    if (skipLoad) {
      list = new ArrayList<>();
    } else {
      lazyLoadCollection(onlyIds);
    }
  }

  private void initClear() {
    lock.lock();
    try {
      if (list == null) {
        initList(disableLazyLoad || !modifyListening, true);
      }
    } finally {
      lock.unlock();
    }
  }

  void init() {
    lock.lock();
    try {
      if (list == null) {
        initList(disableLazyLoad, false);
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Set the actual underlying list.
   * <p>
   * This is primarily for the deferred fetching function.
   */
  @SuppressWarnings("unchecked")
  public void setActualList(List<?> list) {
    this.list = (List<E>) list;
  }

  /**
   * Return the actual underlying list.
   */
  public List<E> actualList() {
    return list;
  }

  @Override
  public Collection<E> actualDetails() {
    return list;
  }

  @Override
  public Collection<?> actualEntries() {
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
    if (list == null) {
      return "BeanList<deferred>";
    } else {
      return list.toString();
    }
  }

  /**
   * Equal if obj is a List and equal in a list sense.
   * <p>
   * Specifically obj does not need to be a BeanList but any list. This does not
   * use the FindMany, fetchedMaxRows or finishedFetch properties in the equals
   * test.
   */
  @Override
  public boolean equals(Object other) {
    init();
    return list.equals(other);
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
  public boolean add(E bean) {
    init();
    if (modifyListening) {
      if (list.add(bean)) {
        modifyAddition(bean);
        return true;
      } else {
        return false;
      }
    }
    return list.add(bean);
  }

  @Override
  public boolean addAll(Collection<? extends E> beans) {
    init();
    if (modifyListening) {
      // all elements in c are added (no contains checking)
      getModifyHolder().modifyAdditionAll(beans);
    }
    return list.addAll(beans);
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> beans) {
    init();
    if (modifyListening) {
      // all elements in c are added (no contains checking)
      getModifyHolder().modifyAdditionAll(beans);
    }
    return list.addAll(index, beans);
  }

  @Override
  public void clear() {
    // TODO: when clear() and not initialised could be more clever
    // and fetch just the Id's
    initClear();
    if (modifyListening) {
      for (E element : list) {
        getModifyHolder().modifyRemoval(element);
      }
    }
    list.clear();
  }

  @Override
  public boolean contains(Object bean) {
    init();
    return list.contains(bean);
  }

  @Override
  public boolean containsAll(Collection<?> beans) {
    init();
    return list.containsAll(beans);
  }

  @Override
  public E get(int index) {
    init();
    return list.get(index);
  }

  @Override
  public int indexOf(Object bean) {
    init();
    return list.indexOf(bean);
  }

  @Override
  public boolean isEmpty() {
    init();
    return list.isEmpty();
  }

  @Override
  public Iterator<E> iterator() {
    init();
    if (modifyListening) {
      return new ModifyIterator<>(this, list.iterator());
    }
    return list.iterator();
  }

  @Override
  public int lastIndexOf(Object bean) {
    init();
    return list.lastIndexOf(bean);
  }

  @Override
  public ListIterator<E> listIterator() {
    init();
    if (modifyListening) {
      return new ModifyListIterator<>(this, list.listIterator());
    }
    return list.listIterator();
  }

  @Override
  public ListIterator<E> listIterator(int index) {
    init();
    if (modifyListening) {
      return new ModifyListIterator<>(this, list.listIterator(index));
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
    init();
    if (modifyListening) {
      E o = list.remove(index);
      modifyRemoval(o);
      return o;
    }
    return list.remove(index);
  }

  @Override
  public boolean remove(Object bean) {
    init();
    if (modifyListening) {
      boolean isRemove = list.remove(bean);
      if (isRemove) {
        modifyRemoval(bean);
      }
      return isRemove;
    }
    return list.remove(bean);
  }

  @Override
  public boolean removeAll(Collection<?> beans) {
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
  public <T> T[] toArray(T[] array) {
    init();
    //noinspection SuspiciousToArrayCall
    return list.toArray(array);
  }

}

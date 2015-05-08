package com.avaje.ebean.common;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.avaje.ebean.bean.BeanCollectionAdd;
import com.avaje.ebean.bean.BeanCollectionLoader;
import com.avaje.ebean.bean.EntityBean;

/**
 * Set capable of lazy loading.
 */
public final class BeanSet<E> extends AbstractBeanCollection<E> implements Set<E>, BeanCollectionAdd {

  private static final long serialVersionUID = 1L;

  /**
   * The underlying Set implementation.
   */
  private Set<E> set;

  /**
   * Create with a specific Set implementation.
   */
  public BeanSet(Set<E> set) {
    this.set = set;
  }

  /**
   * Create using an underlying LinkedHashSet.
   */
  public BeanSet() {
    this(new LinkedHashSet<E>());
  }

  public BeanSet(BeanCollectionLoader loader, EntityBean ownerBean, String propertyName) {
    super(loader, ownerBean, propertyName);
  }

  @Override
  public void reset(EntityBean ownerBean, String propertyName) {
    this.ownerBean = ownerBean;
    this.propertyName = propertyName;
    this.set = null;
    this.touched = false;
  }

  public boolean isEmptyAndUntouched() {
    return !touched && (set == null || set.isEmpty());
  }

  @SuppressWarnings("unchecked")
  public void addBean(EntityBean bean) {
    set.add((E) bean);
  }

  @SuppressWarnings("unchecked")
  public void internalAdd(Object bean) {
    if (set == null) {
      set = new LinkedHashSet<E>();
    }
    set.add((E) bean);
  }

  /**
   * Returns true if the underlying set has its data.
   */
  public boolean isPopulated() {
    return set != null;
  }

  /**
   * Return true if this is a reference (lazy loading) bean collection. This is
   * the same as !isPopulated();
   */
  public boolean isReference() {
    return set == null;
  }

  public boolean checkEmptyLazyLoad() {
    if (set == null) {
      set = new LinkedHashSet<E>();
      return true;
    } else {
      return false;
    }
  }

  private void initClear() {
    synchronized (this) {
      if (set == null) {
        if (modifyListening) {
          lazyLoadCollection(true);
        } else {
          set = new LinkedHashSet<E>();
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
      if (set == null) {
        lazyLoadCollection(true);
      }
      touched(setTouched);
    }
  }

  /**
   * Set the underlying set (used for lazy fetch).
   */
  @SuppressWarnings("unchecked")
  public void setActualSet(Set<?> set) {
    this.set = (Set<E>) set;
  }

  /**
   * Return the actual underlying set.
   */
  public Set<E> getActualSet() {
    return set;
  }

  public Collection<E> getActualDetails() {
    return set;
  }

  @Override
  public Collection<?> getActualEntries() {
    return set;
  }

  /**
   * Returns the underlying set.
   */
  public Object getActualCollection() {
    return set;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer(50);
    sb.append("BeanSet ");
    if (isReadOnly()) {
      sb.append("readOnly ");
    }
    if (set == null) {
      sb.append("deferred ");

    } else {
      sb.append("size[").append(set.size()).append("]");
      sb.append(" set").append(set);
    }
    return sb.toString();
  }

  /**
   * Equal if obj is a Set and equal in a Set sense.
   */
  public boolean equals(Object obj) {
    init();
    return set.equals(obj);
  }

  public int hashCode() {
    init();
    return set.hashCode();
  }

  // -----------------------------------------------------//
  // proxy method for map
  // -----------------------------------------------------//

  public boolean add(E o) {
    checkReadOnly();
    init();
    if (modifyAddListening) {
      if (set.add(o)) {
        modifyAddition(o);
        return true;
      } else {
        return false;
      }
    }
    return set.add(o);
  }

  public boolean addAll(Collection<? extends E> c) {
    checkReadOnly();
    init();
    if (modifyAddListening) {
      boolean changed = false;
      Iterator<? extends E> it = c.iterator();
      while (it.hasNext()) {
        E o = it.next();
        if (set.add(o)) {
          modifyAddition(o);
          changed = true;
        }
      }
      return changed;
    }
    return set.addAll(c);
  }

  public void clear() {
    checkReadOnly();
    initClear();
    if (modifyRemoveListening) {
      Iterator<E> it = set.iterator();
      while (it.hasNext()) {
        E e = it.next();
        modifyRemoval(e);
      }
    }
    set.clear();
  }

  public boolean contains(Object o) {
    init();
    return set.contains(o);
  }

  public boolean containsAll(Collection<?> c) {
    init();
    return set.containsAll(c);
  }

  public boolean isEmpty() {
    initAsUntouched();
    return set.isEmpty();
  }

  public Iterator<E> iterator() {
    init();
    if (isReadOnly()) {
      return new ReadOnlyIterator<E>(set.iterator());
    }
    if (modifyListening) {
      return new ModifyIterator<E>(this, set.iterator());
    }
    return set.iterator();
  }

  public boolean remove(Object o) {
    checkReadOnly();
    init();
    if (modifyRemoveListening) {
      if (set.remove(o)) {
        modifyRemoval(o);
        return true;
      }
      return false;
    }
    return set.remove(o);
  }

  public boolean removeAll(Collection<?> c) {
    checkReadOnly();
    init();
    if (modifyRemoveListening) {
      boolean changed = false;
      Iterator<?> it = c.iterator();
      while (it.hasNext()) {
        Object o = (Object) it.next();
        if (set.remove(o)) {
          modifyRemoval(o);
          changed = true;
        }
      }
      return changed;
    }
    return set.removeAll(c);
  }

  public boolean retainAll(Collection<?> c) {
    checkReadOnly();
    init();
    if (modifyRemoveListening) {
      boolean changed = false;
      Iterator<?> it = set.iterator();
      while (it.hasNext()) {
        Object o = it.next();
        if (!c.contains(o)) {
          it.remove();
          modifyRemoval(o);
          changed = true;
        }
      }
      return changed;
    }
    return set.retainAll(c);
  }

  public int size() {
    init();
    return set.size();
  }

  public Object[] toArray() {
    init();
    return set.toArray();
  }

  public <T> T[] toArray(T[] a) {
    init();
    return set.toArray(a);
  }

  private static class ReadOnlyIterator<E> implements Iterator<E>, Serializable {

    private static final long serialVersionUID = 2577697326745352605L;

    private final Iterator<E> it;

    ReadOnlyIterator(Iterator<E> it) {
      this.it = it;
    }

    public boolean hasNext() {
      return it.hasNext();
    }

    public E next() {
      return it.next();
    }

    public void remove() {
      throw new IllegalStateException("This collection is in ReadOnly mode");
    }
  }

}

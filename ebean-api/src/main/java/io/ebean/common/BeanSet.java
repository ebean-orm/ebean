package io.ebean.common;

import io.ebean.bean.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Set capable of lazy loading and modification aware.
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
    this(new LinkedHashSet<>());
  }

  public BeanSet(BeanCollectionLoader loader, EntityBean ownerBean, String propertyName) {
    super(loader, ownerBean, propertyName);
  }

  @Override
  public void toString(ToStringBuilder builder) {
    builder.addCollection(set);
  }

  @Override
  public void reset(EntityBean ownerBean, String propertyName) {
    this.ownerBean = ownerBean;
    this.propertyName = propertyName;
    this.set = null;
  }

  @Override
  public boolean isSkipSave() {
    return set == null || (set.isEmpty() && !holdsModifications());
  }

  @Override
  @SuppressWarnings("unchecked")
  public void addEntityBean(EntityBean bean) {
    set.add((E) bean);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void loadFrom(BeanCollection<?> other) {
    if (set == null) {
      set = new LinkedHashSet<>();
    }
    set.addAll((Collection<? extends E>) other.actualDetails());
  }

  @Override
  public void internalAddWithCheck(Object bean) {
    // set add() already de-dups so just add it
    internalAdd(bean);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void internalAdd(Object bean) {
    if (set == null) {
      set = new LinkedHashSet<>();
    }
    if (bean != null) {
      set.add((E) bean);
    }
  }

  /**
   * Returns true if the underlying set has its data.
   */
  @Override
  public boolean isPopulated() {
    return set != null;
  }

  /**
   * Return true if this is a reference (lazy loading) bean collection. This is
   * the same as !isPopulated();
   */
  @Override
  public boolean isReference() {
    return set == null;
  }

  @Override
  public boolean checkEmptyLazyLoad() {
    if (set == null) {
      set = new LinkedHashSet<>();
      return true;
    } else {
      return false;
    }
  }

  private void initClear() {
    lock.lock();
    try {
      if (set == null) {
        if (!disableLazyLoad && modifyListening) {
          lazyLoadCollection(false);
        } else {
          set = new LinkedHashSet<>();
        }
      }
    } finally {
      lock.unlock();
    }
  }

  private void init() {
    lock.lock();
    try {
      if (set == null) {
        if (disableLazyLoad) {
          set = new LinkedHashSet<>();
        } else {
          lazyLoadCollection(false);
        }
      }
    } finally {
      lock.unlock();
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
  public Set<E> actualSet() {
    return set;
  }

  @Override
  public Collection<E> actualDetails() {
    return set;
  }

  @Override
  public Collection<?> actualEntries(boolean load) {
    if (load) {
      init();
    }
    return set;
  }

  @Override
  public String toString() {
    if (set == null) {
      return "BeanSet<deferred>";
    } else {
      return set.toString();
    }
  }

  /**
   * Equal if obj is a Set and equal in a Set sense.
   */
  @Override
  public boolean equals(Object obj) {
    init();
    return set.equals(obj);
  }

  @Override
  public int hashCode() {
    init();
    return set.hashCode();
  }

  @Override
  public void addBean(E bean) {
    add(bean);
  }

  @Override
  public void removeBean(E bean) {
    if (set.remove(bean)) {
      getModifyHolder().modifyRemoval(bean);
    }
  }

  // -----------------------------------------------------//
  // proxy method for map
  // -----------------------------------------------------//

  @Override
  public boolean add(E bean) {
    checkReadOnly();
    init();
    if (modifyListening) {
      if (set.add(bean)) {
        modifyAddition(bean);
        return true;
      } else {
        return false;
      }
    }
    return set.add(bean);
  }

  @Override
  public boolean addAll(Collection<? extends E> beans) {
    checkReadOnly();
    init();
    if (modifyListening) {
      boolean changed = false;
      for (E bean : beans) {
        if (set.add(bean)) {
          // register the addition of the bean
          modifyAddition(bean);
          changed = true;
        }
      }
      return changed;
    }
    return set.addAll(beans);
  }

  @Override
  public void clear() {
    checkReadOnly();
    initClear();
    if (modifyListening) {
      for (E bean : set) {
        modifyRemoval(bean);
      }
    }
    set.clear();
  }

  @Override
  public boolean contains(Object bean) {
    init();
    return set.contains(bean);
  }

  @Override
  public boolean containsAll(Collection<?> beans) {
    init();
    return set.containsAll(beans);
  }

  @Override
  public boolean isEmpty() {
    init();
    return set.isEmpty();
  }

  @Override
  public Iterator<E> iterator() {
    init();
    if (readOnly) {
      return new ReadOnlyIterator<>(set.iterator());
    }
    if (modifyListening) {
      return new ModifyIterator<>(this, set.iterator());
    }
    return set.iterator();
  }

  @Override
  public boolean remove(Object bean) {
    checkReadOnly();
    init();
    if (modifyListening) {
      if (set.remove(bean)) {
        modifyRemoval(bean);
        return true;
      }
      return false;
    }
    return set.remove(bean);
  }

  @Override
  public boolean removeAll(Collection<?> beans) {
    checkReadOnly();
    init();
    if (modifyListening) {
      boolean changed = false;
      for (Object bean : beans) {
        if (set.remove(bean)) {
          modifyRemoval(bean);
          changed = true;
        }
      }
      return changed;
    }
    return set.removeAll(beans);
  }

  @Override
  public boolean retainAll(Collection<?> beans) {
    checkReadOnly();
    init();
    if (modifyListening) {
      boolean changed = false;
      Iterator<?> it = set.iterator();
      while (it.hasNext()) {
        Object bean = it.next();
        if (!beans.contains(bean)) {
          // not retaining this bean so add it to the removal list
          it.remove();
          modifyRemoval(bean);
          changed = true;
        }
      }
      return changed;
    }
    return set.retainAll(beans);
  }

  @Override
  public int size() {
    init();
    return set.size();
  }

  @Override
  public Object[] toArray() {
    init();
    return set.toArray();
  }

  @Override
  public <T> T[] toArray(T[] array) {
    init();
    //noinspection SuspiciousToArrayCall
    return set.toArray(array);
  }

  private static final class ReadOnlyIterator<E> implements Iterator<E>, Serializable {

    private static final long serialVersionUID = 2577697326745352605L;

    private final Iterator<E> it;

    ReadOnlyIterator(Iterator<E> it) {
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
      throw new IllegalStateException("This collection is in ReadOnly mode");
    }
  }

  @Override
  public BeanCollection<E> shallowCopy() {
    BeanSet<E> copy = new BeanSet<>(new LinkedHashSet<>(set));
    copy.setFromOriginal(this);
    return copy;
  }
}

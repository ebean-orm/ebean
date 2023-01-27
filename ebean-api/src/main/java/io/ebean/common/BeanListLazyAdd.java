package io.ebean.common;

import io.ebean.bean.BeanCollectionLoader;
import io.ebean.bean.EntityBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This bean list can perform additions without populating the list.
 * This might be useful, if you just want to add entries to an existing collection.
 * Works only for lists and only if there is no order column
 */
public class BeanListLazyAdd<E> extends BeanList<E> {

  public BeanListLazyAdd(BeanCollectionLoader loader, EntityBean ownerBean, String propertyName) {
    super(loader, ownerBean, propertyName);
  }

  private List<E> lazyAddedEntries;

  @Override
  public boolean add(E bean) {
    checkReadOnly();

    lock.lock();
    try {
      if (list == null) {
        // list is not yet initialized, so we may add elements to a spare list
        if (lazyAddedEntries == null) {
          lazyAddedEntries = new ArrayList<>();
        }
        lazyAddedEntries.add(bean);
      } else {
        list.add(bean);
      }
    } finally {
      lock.unlock();
    }

    if (modifyListening) {
      modifyAddition(bean);
    }
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends E> beans) {
    checkReadOnly();

    lock.lock();
    try {
      if (list == null) {
        // list is not yet initialized, so we may add elements to a spare list
        if (lazyAddedEntries == null) {
          lazyAddedEntries = new ArrayList<>();
        }
        lazyAddedEntries.addAll(beans);
      } else {
        list.addAll(beans);
      }
    } finally {
      lock.unlock();
    }

    if (modifyListening) {
      getModifyHolder().modifyAdditionAll(beans);
    }

    return true;
  }

  /**
   * on init, this happens on all accessor methods except on 'add' and addAll,
   * we add the lazy added entries at the end of the list
   */
  @Override
  void init() {
    lock.lock();
    try {
      if (list == null) {
        if (disableLazyLoad) {
          list = lazyAddedEntries == null ? new ArrayList<>() : lazyAddedEntries;
        } else {
          lazyLoadCollection(false);
          if (lazyAddedEntries != null) {
            list.addAll(lazyAddedEntries);
          }
        }
      }
      lazyAddedEntries = null;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public List<E> getLazyAddedEntries() {
    return lazyAddedEntries;
  }

  @Override
  public boolean isSkipSave() {
    return lazyAddedEntries == null && super.isSkipSave();
  }
}

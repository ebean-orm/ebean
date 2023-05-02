package io.ebean.common;

import io.ebean.bean.BeanCollection;
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


  @Override
  public void loadFrom(BeanCollection<?> other) {
    super.loadFrom(other);
    if (lazyAddedEntries != null) {
      list.addAll(lazyAddedEntries);
      lazyAddedEntries = null;
    }
  }

  /**
   * on init, this happens on all accessor methods except on 'add' and addAll,
   * we add the lazy added entries at the end of the list
   */
  @Override
  protected void initList(boolean skipLoad, boolean onlyIds) {
    if (skipLoad) {
      if (lazyAddedEntries != null) {
        list = lazyAddedEntries;
        lazyAddedEntries = null;
      } else {
        list = new ArrayList<>();
      }
    } else {
      lazyLoadCollection(onlyIds);
      if (lazyAddedEntries != null) {
        list.addAll(lazyAddedEntries);
        lazyAddedEntries = null;
      }
    }
  }

  @Override
  public List<E> getLazyAddedEntries(boolean reset) {
    List<E> ret = lazyAddedEntries;
    if (reset) {
      lazyAddedEntries = null;
    }
    return ret;
  }

  @Override
  public boolean isSkipSave() {
    return lazyAddedEntries == null && super.isSkipSave();
  }

  public boolean checkEmptyLazyLoad() {
    if (list != null) {
      return false;
    } else if (lazyAddedEntries == null) {
      list = new ArrayList<>();
      return true;
    } else {
      list = lazyAddedEntries;
      lazyAddedEntries = null;
      return false;
    }
  }
}

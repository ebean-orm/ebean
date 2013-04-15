package com.avaje.ebean.common;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.persistence.PersistenceException;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.BeanCollectionLoader;
import com.avaje.ebean.bean.BeanCollectionTouched;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;

/**
 * Base class for List Set and Map implementations of BeanCollection.
 * 
 * @author rbygrave
 */
public abstract class AbstractBeanCollection<E> implements BeanCollection<E> {

  private static final long serialVersionUID = 3365725236140187588L;

  protected boolean readOnly;

  /**
   * The EbeanServer this is associated with. (used for lazy fetch).
   */
  protected transient BeanCollectionLoader loader;

  protected transient ExpressionList<?> filterMany;

  protected int loaderIndex;

  protected String ebeanServerName;

  protected transient BeanCollectionTouched beanCollectionTouched;

  protected transient Future<Integer> fetchFuture;

  /**
   * The owning bean (used for lazy fetch).
   */
  protected final Object ownerBean;

  /**
   * The name of this property in the owning bean (used for lazy fetch).
   */
  protected final String propertyName;

  /**
   * Can be false when a background thread is used to continue the fetch the
   * rows. It will set this to true when it is finished. If no background thread
   * is used then this should already be true.
   */
  protected boolean finishedFetch = true;

  /**
   * Flag set to true if rows are limited by firstRow maxRows and more rows
   * exist. For use by client to enable 'next' for paging.
   */
  protected boolean hasMoreRows;

  protected ModifyHolder<E> modifyHolder;

  protected ModifyListenMode modifyListenMode;
  protected boolean modifyAddListening;
  protected boolean modifyRemoveListening;
  protected boolean modifyListening;

  /**
   * Constructor not non-lazy loading collection.
   */
  public AbstractBeanCollection() {
    this.ownerBean = null;
    this.propertyName = null;
  }

  /**
   * Used to create deferred fetch proxy.
   */
  public AbstractBeanCollection(BeanCollectionLoader loader, Object ownerBean, String propertyName) {
    this.loader = loader;
    this.ebeanServerName = loader.getName();
    this.ownerBean = ownerBean;
    this.propertyName = propertyName;

    if (ownerBean instanceof EntityBean) {
      EntityBeanIntercept ebi = ((EntityBean) ownerBean)._ebean_getIntercept();
      this.readOnly = ebi.isReadOnly();
    }
  }

  public Object getOwnerBean() {
    return ownerBean;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public int getLoaderIndex() {
    return loaderIndex;
  }

  public ExpressionList<?> getFilterMany() {
    return filterMany;
  }

  public void setFilterMany(ExpressionList<?> filterMany) {
    this.filterMany = filterMany;
  }

  protected void lazyLoadCollection(boolean onlyIds) {
    if (loader == null) {
      loader = (BeanCollectionLoader) Ebean.getServer(ebeanServerName);
    }
    if (loader == null) {
      String msg = "Lazy loading but LazyLoadEbeanServer is null?"
          + " The LazyLoadEbeanServer needs to be set after deserialization"
          + " to support lazy loading.";
      throw new PersistenceException(msg);
    }

    loader.loadMany(this, onlyIds);
    checkEmptyLazyLoad();
  }

  protected void touched() {
    if (beanCollectionTouched != null) {
      // only call this once
      beanCollectionTouched.notifyTouched(this);
      beanCollectionTouched = null;
    }
  }

  public void setBeanCollectionTouched(BeanCollectionTouched notify) {
    this.beanCollectionTouched = notify;
  }

  public void setLoader(int beanLoaderIndex, BeanCollectionLoader loader) {
    this.loaderIndex = beanLoaderIndex;
    this.loader = loader;
    this.ebeanServerName = loader.getName();
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  /**
   * Set to true if maxRows was hit and there are actually more rows available.
   * <p>
   * Can be used by client code that is paging through results using
   * setFirstRow() setMaxRows(). If this returns true then the client can
   * display a 'next' button etc.
   * </p>
   */
  public boolean hasMoreRows() {
    return hasMoreRows;
  }

  /**
   * Set to true when maxRows is hit but there are actually more rows available.
   * This is set so that client code knows that there is more data available.
   */
  public void setHasMoreRows(boolean hasMoreRows) {
    this.hasMoreRows = hasMoreRows;
  }

  /**
   * Returns true if the fetch has finished. False if the fetch is continuing in
   * a background thread.
   */
  public boolean isFinishedFetch() {
    return finishedFetch;
  }

  /**
   * Set to true when a fetch has finished. Used when a fetch continues in the
   * background.
   */
  public void setFinishedFetch(boolean finishedFetch) {
    this.finishedFetch = finishedFetch;
  }

  public void setBackgroundFetch(Future<Integer> fetchFuture) {
    this.fetchFuture = fetchFuture;
  }

  public void backgroundFetchWait(long wait, TimeUnit timeUnit) {
    if (fetchFuture != null) {
      try {
        fetchFuture.get(wait, timeUnit);
      } catch (Exception e) {
        throw new PersistenceException(e);
      }
    }
  }

  public void backgroundFetchWait() {
    if (fetchFuture != null) {
      try {
        fetchFuture.get();
      } catch (Exception e) {
        throw new PersistenceException(e);
      }
    }
  }

  protected void checkReadOnly() {
    if (readOnly) {
      String msg = "This collection is in ReadOnly mode";
      throw new IllegalStateException(msg);
    }
  }

  // ---------------------------------------------------------
  // Support for modify additions deletions etc - ManyToMany
  // ---------------------------------------------------------

  /**
   * set modifyListening to be on or off.
   */
  public void setModifyListening(ModifyListenMode mode) {

    this.modifyListenMode = mode;
    this.modifyAddListening = ModifyListenMode.ALL.equals(mode);
    this.modifyRemoveListening = modifyAddListening || ModifyListenMode.REMOVALS.equals(mode);
    this.modifyListening = modifyRemoveListening || modifyAddListening;
    if (modifyListening) {
      // lose any existing modifications
      modifyHolder = null;
    }
  }

  /**
   * Return the modify listening mode this collection is using.
   */
  public ModifyListenMode getModifyListenMode() {
    return modifyListenMode;
  }

  protected ModifyHolder<E> getModifyHolder() {
    if (modifyHolder == null) {
      modifyHolder = new ModifyHolder<E>();
    }
    return modifyHolder;
  }

  public void modifyAddition(E bean) {
    if (modifyAddListening) {
      getModifyHolder().modifyAddition(bean);
    }
  }

  public void modifyRemoval(Object bean) {
    if (modifyRemoveListening) {
      getModifyHolder().modifyRemoval(bean);
    }
  }

  public void modifyReset() {
    if (modifyHolder != null) {
      modifyHolder.reset();
    }
  }

  public Set<E> getModifyAdditions() {
    if (modifyHolder == null) {
      return null;
    } else {
      return modifyHolder.getModifyAdditions();
    }
  }

  public Set<E> getModifyRemovals() {
    if (modifyHolder == null) {
      return null;
    } else {
      return modifyHolder.getModifyRemovals();
    }
  }
}

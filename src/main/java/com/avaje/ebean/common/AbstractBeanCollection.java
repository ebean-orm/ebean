package com.avaje.ebean.common;

import java.util.Set;

import javax.persistence.PersistenceException;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.bean.BeanCollection;
import com.avaje.ebean.bean.BeanCollectionLoader;
import com.avaje.ebean.bean.BeanCollectionTouched;
import com.avaje.ebean.bean.EntityBean;

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

  /**
   * Flag set when registered with the batch loading context.
   */
  protected boolean registeredWithLoadContext;

  protected String ebeanServerName;

  protected transient BeanCollectionTouched beanCollectionTouched;

  /**
   * The owning bean (used for lazy fetch).
   */
  protected EntityBean ownerBean;

  /**
   * The name of this property in the owning bean (used for lazy fetch).
   */
  protected String propertyName;

  protected ModifyHolder<E> modifyHolder;

  protected ModifyListenMode modifyListenMode;
  protected boolean modifyAddListening;
  protected boolean modifyRemoveListening;
  protected boolean modifyListening;

  /**
   * Flag used to tell if empty collections have been cleared etc or just
   * uninitialised.
   */
  protected boolean touched;

  /**
   * Constructor not non-lazy loading collection.
   */
  public AbstractBeanCollection() {
  }

  /**
   * Used to create deferred fetch proxy.
   */
  public AbstractBeanCollection(BeanCollectionLoader loader, EntityBean ownerBean, String propertyName) {
    this.loader = loader;
    this.ebeanServerName = loader.getName();
    this.ownerBean = ownerBean;
    this.propertyName = propertyName;
    this.readOnly = ownerBean._ebean_getIntercept().isReadOnly();
  }

  public EntityBean getOwnerBean() {
    return ownerBean;
  }

  public String getPropertyName() {
    return propertyName;
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

  /**
   * Set touched. If setFlag is false then typically an isEmpty() call and still
   * considering that to be untouched.
   */
  protected void touched(boolean setFlag) {
    if (setFlag) {
      touched = true;
    }
    if (beanCollectionTouched != null) {
      // only call this once
      beanCollectionTouched.notifyTouched(this);
      beanCollectionTouched = null;
    }
  }

  public void setBeanCollectionTouched(BeanCollectionTouched notify) {
    this.beanCollectionTouched = notify;
  }

  public boolean isRegisteredWithLoadContext() {
    return registeredWithLoadContext;
  }

  public void setLoader(BeanCollectionLoader loader) {
    this.registeredWithLoadContext = true;
    this.loader = loader;
    this.ebeanServerName = loader.getName();
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
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

package io.ebean.common;

import io.ebean.Ebean;
import io.ebean.ExpressionList;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.BeanCollectionLoader;
import io.ebean.bean.EntityBean;

import java.util.Set;

/**
 * Base class for List Set and Map implementations of BeanCollection.
 */
abstract class AbstractBeanCollection<E> implements BeanCollection<E> {

  private static final long serialVersionUID = 3365725236140187588L;

  protected boolean readOnly;

  protected boolean disableLazyLoad;

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

  protected boolean modifyListening;

  /**
   * Constructor not non-lazy loading collection.
   */
  AbstractBeanCollection() {
  }

  /**
   * Used to create deferred fetch proxy.
   */
  AbstractBeanCollection(BeanCollectionLoader loader, EntityBean ownerBean, String propertyName) {
    this.loader = loader;
    this.ebeanServerName = loader.getName();
    this.ownerBean = ownerBean;
    this.propertyName = propertyName;
    this.readOnly = ownerBean != null && ownerBean._ebean_getIntercept().isReadOnly();
  }

  @Override
  public EntityBean getOwnerBean() {
    return ownerBean;
  }

  @Override
  public String getPropertyName() {
    return propertyName;
  }

  @Override
  public ExpressionList<?> getFilterMany() {
    return filterMany;
  }

  @Override
  public void setFilterMany(ExpressionList<?> filterMany) {
    this.filterMany = filterMany;
  }

  @Override
  public void setDisableLazyLoad(boolean disableLazyLoad) {
    this.disableLazyLoad = disableLazyLoad;
  }

  void lazyLoadCollection(boolean onlyIds) {
    if (loader == null) {
      loader = (BeanCollectionLoader) Ebean.getServer(ebeanServerName);
    }
    loader.loadMany(this, onlyIds);
    checkEmptyLazyLoad();
  }

  @Override
  public boolean isRegisteredWithLoadContext() {
    return registeredWithLoadContext;
  }

  @Override
  public void setLoader(BeanCollectionLoader loader) {
    this.registeredWithLoadContext = true;
    this.loader = loader;
    this.ebeanServerName = loader.getName();
  }

  @Override
  public boolean isReadOnly() {
    return readOnly;
  }

  @Override
  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  void checkReadOnly() {
    if (readOnly) {
      String msg = "This collection is in ReadOnly mode";
      throw new IllegalStateException(msg);
    }
  }

  // ---------------------------------------------------------
  // Support for modify additions deletions etc - ManyToMany
  // ---------------------------------------------------------

  @Override
  public ModifyListenMode getModifyListening() {
    return modifyListenMode;
  }

  /**
   * set modifyListening to be on or off.
   */
  @Override
  public void setModifyListening(ModifyListenMode mode) {

    this.modifyListenMode = mode;
    this.modifyListening = mode != null && ModifyListenMode.NONE != mode;
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

  ModifyHolder<E> getModifyHolder() {
    if (modifyHolder == null) {
      modifyHolder = new ModifyHolder<>();
    }
    return modifyHolder;
  }

  @Override
  public void modifyAddition(E bean) {
    if (modifyListening) {
      getModifyHolder().modifyAddition(bean);
    }
  }

  @Override
  public void modifyRemoval(Object bean) {
    if (modifyListening) {
      getModifyHolder().modifyRemoval(bean);
    }
  }

  @Override
  public void modifyReset() {
    if (modifyHolder != null) {
      modifyHolder.reset();
    }
  }

  @Override
  public Set<E> getModifyAdditions() {
    if (modifyHolder == null) {
      return null;
    } else {
      return modifyHolder.getModifyAdditions();
    }
  }

  @Override
  public Set<E> getModifyRemovals() {
    if (modifyHolder == null) {
      return null;
    } else {
      return modifyHolder.getModifyRemovals();
    }
  }

  /**
   * Return true if there are underlying additions or removals.
   */
  @Override
  public boolean holdsModifications() {
    return modifyHolder != null && modifyHolder.hasModifications();
  }

  @Override
  public boolean wasTouched() {
    return modifyHolder != null && modifyHolder.wasTouched();
  }

  /**
   * Copies all relevant properties for a clone. See {@link #getShallowCopy()}
   * @param other
   */
  protected void setFromOriginal(AbstractBeanCollection<E> other) {
    this.disableLazyLoad = other.disableLazyLoad;
    this.ebeanServerName = other.ebeanServerName;
    this.loader = other.loader;
    this.ownerBean = other.ownerBean;
    this.propertyName = other.propertyName;
  }
}

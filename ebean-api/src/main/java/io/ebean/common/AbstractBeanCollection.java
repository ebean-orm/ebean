package io.ebean.common;

import io.ebean.DB;
import io.ebean.ExpressionList;
import io.ebean.bean.BeanCollection;
import io.ebean.bean.BeanCollectionLoader;
import io.ebean.bean.EntityBean;

import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Base class for List Set and Map implementations of BeanCollection.
 */
abstract class AbstractBeanCollection<E> implements BeanCollection<E> {

  private static final long serialVersionUID = 3365725236140187588L;

  protected final ReentrantLock lock = new ReentrantLock();
  protected boolean readOnly;
  protected boolean disableLazyLoad;
  /**
   * The Database this is associated with. (used for lazy fetch).
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
    this.ebeanServerName = loader.name();
    this.ownerBean = ownerBean;
    this.propertyName = propertyName;
    this.readOnly = ownerBean != null && ownerBean._ebean_getIntercept().isReadOnly();
  }

  @Override
  public EntityBean owner() {
    return ownerBean;
  }

  @Override
  public String propertyName() {
    return propertyName;
  }

  @Override
  public ExpressionList<?> filterMany() {
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
      loader = (BeanCollectionLoader) DB.byName(ebeanServerName);
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
    this.ebeanServerName = loader.name();
  }

  @Override
  public boolean isReadOnly() {
    return readOnly;
  }

  @Override
  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  final void checkReadOnly() {
    if (readOnly) {
      throw new UnsupportedOperationException();
    }
  }

  // ---------------------------------------------------------
  // Support for modify additions deletions etc - ManyToMany
  // ---------------------------------------------------------

  @Override
  public boolean hasModifications() {
    return modifyHolder != null && modifyHolder.hasModifications();
  }

  @Override
  public ModifyListenMode modifyListening() {
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
  public Set<E> modifyAdditions() {
    if (modifyHolder == null) {
      return null;
    } else {
      return modifyHolder.getModifyAdditions();
    }
  }

  @Override
  public Set<E> modifyRemovals() {
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
   * Copies all relevant properties for a clone. See {@link #shallowCopy()}
   */
  protected void setFromOriginal(AbstractBeanCollection<E> other) {
    this.disableLazyLoad = other.disableLazyLoad;
    this.ebeanServerName = other.ebeanServerName;
    this.loader = other.loader;
    this.ownerBean = other.ownerBean;
    this.propertyName = other.propertyName;
  }
}

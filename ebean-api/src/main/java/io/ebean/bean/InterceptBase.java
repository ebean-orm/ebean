package io.ebean.bean;

import io.ebean.DB;
import io.ebean.Database;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Base EntityBeanIntercept that supports partial loaded state and lazy loading.
 */
abstract class InterceptBase implements EntityBeanIntercept {

  /**
   * Flag for loaded property.
   */
  static final byte FLAG_LOADED_PROP = 1;

  /**
   * The actual entity bean that 'owns' this intercept.
   */
  protected final EntityBean owner;
  protected final byte[] flags;
  protected boolean readOnly;
  protected boolean disableLazyLoad;
  protected boolean errorOnLazyLoad;

  private final ReentrantLock lock = new ReentrantLock();
  private String ebeanServerName;
  private transient BeanLoader beanLoader;
  private transient PersistenceContext persistenceContext;

  /**
   * Flag set when lazy loading failed due to the underlying bean being deleted in the DB.
   */
  protected boolean lazyLoadFailure;
  protected boolean fullyLoadedBean;
  protected int lazyLoadProperty = -1;
  protected Object ownerId;

  /**
   * Create with a given entity.
   */
  InterceptBase(Object ownerBean) {
    this.owner = (EntityBean) ownerBean;
    this.flags = new byte[owner._ebean_getPropertyNames().length];
  }

  /**
   * EXPERIMENTAL - Constructor only for use by serialization frameworks.
   */
  InterceptBase() {
    this.owner = null;
    this.flags = null;
  }

  public void freeze() {
    this.errorOnLazyLoad = true;
    this.readOnly = true;
    this.beanLoader = null;
    this.persistenceContext = null;
  }

  @Override
  public final EntityBean owner() {
    return owner;
  }

  @Override
  public final Object ownerId() {
    return ownerId;
  }

  @Override
  public final void setOwnerId(Object ownerId) {
    this.ownerId = ownerId;
  }

  @Override
  public final void setBeanLoader(BeanLoader beanLoader) {
    this.beanLoader = beanLoader;
    this.ebeanServerName = beanLoader.name();
  }

  @Override
  public final void setBeanLoader(BeanLoader beanLoader, PersistenceContext ctx) {
    this.beanLoader = beanLoader;
    this.persistenceContext = ctx;
    this.ebeanServerName = beanLoader.name();
  }

  @Override
  public final PersistenceContext persistenceContext() {
    return persistenceContext;
  }

  @Override
  public final void setPersistenceContext(PersistenceContext persistenceContext) {
    this.persistenceContext = persistenceContext;
  }


  @Override
  public final boolean isFullyLoadedBean() {
    return fullyLoadedBean;
  }

  @Override
  public final void setFullyLoadedBean(boolean fullyLoadedBean) {
    this.fullyLoadedBean = fullyLoadedBean;
  }

  @Override
  public final boolean isPartial() {
    for (byte flag : flags) {
      if ((flag & FLAG_LOADED_PROP) == 0) {
        return true;
      }
    }
    return false;
  }

  @Override
  public final boolean hasIdOnly(int idIndex) {
    for (int i = 0; i < flags.length; i++) {
      if (i == idIndex) {
        if ((flags[i] & FLAG_LOADED_PROP) == 0) return false;
      } else if ((flags[i] & FLAG_LOADED_PROP) != 0) {
        return false;
      }
    }
    return true;
  }

  @Override
  public final boolean isReadOnly() {
    return readOnly;
  }

  @Override
  public final void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  @Override
  public final void setLazyLoadFailure(Object ownerId) {
    this.lazyLoadFailure = true;
    this.ownerId = ownerId;
  }

  @Override
  public final boolean isLazyLoadFailure() {
    return lazyLoadFailure;
  }

  @Override
  public final boolean isDisableLazyLoad() {
    return disableLazyLoad;
  }

  @Override
  public final void setDisableLazyLoad(boolean disableLazyLoad) {
    this.disableLazyLoad = disableLazyLoad;
  }

  @Override
  public final void errorOnLazyLoad(boolean errorOnLazyLoad) {
    this.errorOnLazyLoad = errorOnLazyLoad;
  }

  @Override
  public final void setEmbeddedLoaded(Object embeddedBean) {
    if (embeddedBean instanceof EntityBean) {
      ((EntityBean) embeddedBean)._ebean_getIntercept().setLoaded();
    }
  }

  @Override
  public final int findProperty(String propertyName) {
    final String[] names = owner._ebean_getPropertyNames();
    for (int i = 0; i < names.length; i++) {
      if (names[i].equals(propertyName)) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public final String property(int propertyIndex) {
    if (propertyIndex == -1) {
      return null;
    }
    return owner._ebean_getPropertyName(propertyIndex);
  }

  @Override
  public final int propertyLength() {
    return flags.length;
  }

  @Override
  public final void setPropertyLoaded(String propertyName, boolean loaded) {
    final int position = findProperty(propertyName);
    if (position == -1) {
      throw new IllegalArgumentException("Property " + propertyName + " not found");
    }
    if (loaded) {
      flags[position] |= FLAG_LOADED_PROP;
    } else {
      flags[position] &= ~FLAG_LOADED_PROP;
    }
  }

  @Override
  public final void setPropertyUnloaded(int propertyIndex) {
    flags[propertyIndex] &= ~FLAG_LOADED_PROP;
  }

  @Override
  public final void initialisedMany(int propertyIndex) {
    flags[propertyIndex] |= FLAG_LOADED_PROP;
  }

  @Override
  public final void setLoadedProperty(int propertyIndex) {
    flags[propertyIndex] |= FLAG_LOADED_PROP;
  }

  @Override
  public final void setLoadedPropertyAll() {
    for (int i = 0; i < flags.length; i++) {
      flags[i] |= FLAG_LOADED_PROP;
    }
  }

  @Override
  public final boolean isLoadedProperty(int propertyIndex) {
    return (flags[propertyIndex] & FLAG_LOADED_PROP) != 0;
  }

  @Override
  public final Set<String> loadedPropertyNames() {
    if (fullyLoadedBean) {
      return null;
    }
    final Set<String> props = new LinkedHashSet<>();
    for (int i = 0; i < flags.length; i++) {
      if ((flags[i] & FLAG_LOADED_PROP) != 0) {
        props.add(property(i));
      }
    }
    return props;
  }


  @Override
  public final StringBuilder loadedPropertyKey() {
    final StringBuilder sb = new StringBuilder();
    final int len = propertyLength();
    for (int i = 0; i < len; i++) {
      if (isLoadedProperty(i)) {
        sb.append(i).append(',');
      }
    }
    return sb;
  }

  @Override
  public final boolean[] loaded() {
    final boolean[] ret = new boolean[flags.length];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = (flags[i] & FLAG_LOADED_PROP) != 0;
    }
    return ret;
  }

  @Override
  public final int lazyLoadPropertyIndex() {
    return lazyLoadProperty;
  }

  @Override
  public final String lazyLoadProperty() {
    return property(lazyLoadProperty);
  }

  @Override
  public final void loadBean(int loadProperty) {
    lock.lock();
    try {
      if (errorOnLazyLoad) {
        throw new PersistenceException("Lazy loading not allowed on this bean");
      }
      if (beanLoader == null) {
        final Database database = DB.byName(ebeanServerName);
        if (database == null) {
          throw new PersistenceException(ebeanServerName == null ? "No registered default server" : "Database [" + ebeanServerName + "] is not registered");
        }
        // For stand alone reference bean or after deserialisation lazy load
        // using the ebeanServer. Synchronise only on the bean.
        loadBeanInternal(loadProperty, database.pluginApi().beanLoader());
        return;
      }
    } finally {
      lock.unlock();
    }
    final Lock lock = beanLoader.lock();
    try {
      // Lazy loading using LoadBeanContext which supports batch loading
      // Synchronise on the beanLoader (a 'node' of the LoadBeanContext 'tree')
      loadBeanInternal(loadProperty, beanLoader);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public final void loadBeanInternal(int loadProperty, BeanLoader loader) {
    if ((flags[loadProperty] & FLAG_LOADED_PROP) != 0) {
      // race condition where multiple threads calling preGetter concurrently
      return;
    }
    if (lazyLoadFailure) {
      // failed when batch lazy loaded by another bean in the batch
      throw new EntityNotFoundException("(Lazy) loading failed on type:" + owner.getClass().getName() + " id:" + ownerId + " - Bean has been deleted. BeanLoader: " + beanLoader);
    }
    if (lazyLoadProperty == -1) {
      lazyLoadProperty = loadProperty;
      loader.loadBean(this);
      if (lazyLoadFailure) {
        // failed when lazy loading this bean
        throw new EntityNotFoundException("Lazy loading failed on type:" + owner.getClass().getName() + " id:" + ownerId + " - Bean has been deleted. BeanLoader: " + beanLoader);
      }
      // bean should be loaded and intercepting now. setLoaded() has
      // been called by the lazy loading mechanism
    }
  }

}

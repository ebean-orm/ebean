package com.avaje.ebean.bean;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;

import com.avaje.ebean.Ebean;

/**
 * This is the object added to every entity bean using byte code enhancement.
 * <p>
 * This provides the mechanisms to support deferred fetching of reference beans
 * and oldValues generation for concurrency checking.
 * </p>
 */
public final class EntityBeanIntercept implements Serializable {

  private static final long serialVersionUID = -3664031775464862648L;

  private transient NodeUsageCollector nodeUsageCollector;

  private transient PropertyChangeSupport pcs;

  private transient PersistenceContext persistenceContext;

  private transient BeanLoader beanLoader;

  private int beanLoaderIndex;

  private String ebeanServerName;

  /**
   * The actual entity bean that 'owns' this intercept.
   */
  private EntityBean owner;

  /**
   * The parent bean by relationship (1-1 or 1-M).
   */
  private Object parentBean;

  /**
   * true if the bean properties have been loaded. false if it is a reference
   * bean (will lazy load etc).
   */
  private volatile boolean loaded;

  /**
   * Flag set to disable lazy loading - typically for SQL "report" type entity
   * beans.
   */
  private boolean disableLazyLoad;

  /**
   * Flag set when lazy loading failed due to the underlying bean being deleted
   * in the DB.
   */
  private boolean lazyLoadFailure;

  /**
   * Set true when loaded or reference. Used to bypass interception when created
   * by user code.
   */
  private boolean intercepting;

  /**
   * The state of the Bean (DEFAULT,UDPATE,READONLY,SHARED).
   */
  private boolean readOnly;

  /**
   * The bean as it was before it was modified. Null if no non-transient setters
   * have been called.
   */
  private Object oldValues;

  /**
   * Used when a bean is partially filled.
   */
  private volatile Set<String> loadedProps;

  /**
   * Set of changed properties.
   */
  private HashSet<String> changedProps;

  private String lazyLoadProperty;

  /**
   * Create a intercept with a given entity.
   * <p>
   * Refer to agent ProxyConstructor.
   * </p>
   */
  public EntityBeanIntercept(Object owner) {
    this.owner = (EntityBean) owner;
  }

  /**
   * Copy the internal state of the intercept to another intercept.
   */
  public void copyStateTo(EntityBeanIntercept dest) {
    dest.loadedProps = loadedProps;
    dest.ebeanServerName = ebeanServerName;

    if (loaded) {
      dest.setLoaded();
    }
  }

  /**
   * Return the 'owning' entity bean.
   */
  public EntityBean getOwner() {
    return owner;
  }

  public String toString() {
    if (!loaded) {
      return "Reference...";
    }
    return "OldValues: " + oldValues;
  }

  /**
   * Return the persistenceContext.
   */
  public PersistenceContext getPersistenceContext() {
    return persistenceContext;
  }

  /**
   * Set the persistenceContext.
   */
  public void setPersistenceContext(PersistenceContext persistenceContext) {
    this.persistenceContext = persistenceContext;
  }

  /**
   * Add a property change listener for this entity bean.
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    if (pcs == null) {
      pcs = new PropertyChangeSupport(owner);
    }
    pcs.addPropertyChangeListener(listener);
  }

  /**
   * Add a property change listener for this entity bean for a specific
   * property.
   */
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    if (pcs == null) {
      pcs = new PropertyChangeSupport(owner);
    }
    pcs.addPropertyChangeListener(propertyName, listener);
  }

  /**
   * Remove a property change listener for this entity bean.
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    if (pcs != null) {
      pcs.removePropertyChangeListener(listener);
    }
  }

  /**
   * Remove a property change listener for this entity bean for a specific
   * property.
   */
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    if (pcs != null) {
      pcs.removePropertyChangeListener(propertyName, listener);
    }
  }

  /**
   * Turn on profile collection.
   */
  public void setNodeUsageCollector(NodeUsageCollector usageCollector) {
    this.nodeUsageCollector = usageCollector;
  }

  /**
   * Return the parent bean (by relationship).
   */
  public Object getParentBean() {
    return parentBean;
  }

  /**
   * Special case for a OneToOne, Set the parent bean (by relationship). This is
   * the owner of a 1-1.
   */
  public void setParentBean(Object parentBean) {
    this.parentBean = parentBean;
  }

  /**
   * Return the index position for batch loading via BeanLoader.
   */
  public int getBeanLoaderIndex() {
    return beanLoaderIndex;
  }

  /**
   * Set Lazy Loading by ebeanServerName.
   * <p>
   * This is for reference beans created by themselves.
   * </p>
   */
  public void setBeanLoaderByServerName(String ebeanServerName) {
    this.beanLoaderIndex = 0;
    this.beanLoader = null;
    this.ebeanServerName = ebeanServerName;
  }

  /**
   * Set the BeanLoader for general lazy loading.
   */
  public void setBeanLoader(int index, BeanLoader beanLoader, PersistenceContext ctx) {
    this.beanLoaderIndex = index;
    this.beanLoader = beanLoader;
    this.persistenceContext = ctx;
    this.ebeanServerName = beanLoader.getName();
  }

  /**
   * Return true if this bean has been directly modified (it has oldValues) or
   * if any embedded beans are either new or dirty (and hence need saving).
   */
  public boolean isDirty() {
    if (oldValues != null) {
      return true;
    }
    // need to check all the embedded beans
    return owner._ebean_isEmbeddedNewOrDirty();
  }

  /**
   * Return true if this entity bean is new and not yet saved.
   */
  public boolean isNew() {
    return !intercepting && !loaded;
  }

  /**
   * Return true if the entity bean is new or dirty (and should be saved).
   */
  public boolean isNewOrDirty() {
    return isNew() || isDirty();
  }

  /**
   * Return true if the entity is a reference.
   */
  public boolean isReference() {
    return intercepting && !loaded;
  }

  /**
   * Set this as a reference object.
   */
  public void setReference() {
    this.loaded = false;
    this.intercepting = true;
  }

  /**
   * Return the old values used for ConcurrencyMode.ALL.
   */
  public Object getOldValues() {
    return oldValues;
  }

  /**
   * Return true if the bean should be treated as readOnly. If a setter method
   * is called when it is readOnly an Exception is thrown.
   */
  public boolean isReadOnly() {
    return readOnly;
  }

  /**
   * Set the readOnly status. If readOnly then calls to setter methods through
   * an exception.
   */
  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  /**
   * Return true if the bean currently has interception on.
   * <p>
   * With interception on the bean will invoke lazy loading and dirty checking.
   * </p>
   */
  public boolean isIntercepting() {
    return intercepting;
  }

  /**
   * Turn interception off or on.
   * <p>
   * This is to support custom serialisation mechanisms that just read all the
   * properties on the bean.
   * </p>
   * 
   */
  public void setIntercepting(boolean intercepting) {
    this.intercepting = intercepting;
  }

  /**
   * Return true if the entity has been loaded.
   */
  public boolean isLoaded() {
    return loaded;
  }

  /**
   * Set the loaded state to true.
   * <p>
   * Calls to setter methods after the bean is loaded can result in 'Old Values'
   * being created to support ConcurrencyMode.ALL
   * </p>
   * <p>
   * Worth noting that this is also set after a insert/update. By doing so it
   * 'resets' the bean for making further changes and saving again.
   * </p>
   */
  public void setLoaded() {
    this.loaded = true;
    this.oldValues = null;
    this.intercepting = true;
    this.owner._ebean_setEmbeddedLoaded();
    this.lazyLoadProperty = null;
    this.changedProps = null;
  }

  /**
   * When finished loading for lazy or refresh on an already partially populated
   * bean.
   */
  public void setLoadedLazy() {
    this.loaded = true;
    this.intercepting = true;
    this.lazyLoadProperty = null;
  }

  /**
   * Mark this bean as having failed lazy loading due to the underlying row
   * being deleted.
   * <p>
   * We mark the bean this way rather than immediately fail as we might be batch
   * lazy loading and this bean might not be used by the client code at all.
   * Instead we will fail as soon as the client code tries to use this bean.
   * </p>
   */
  public void setLazyLoadFailure() {
    this.lazyLoadFailure = true;
  }

  /**
   * Return true if the bean is marked as having failed lazy loading.
   */
  public boolean isLazyLoadFailure() {
    return lazyLoadFailure;
  }

  /**
   * Return true if lazy loading is disabled.
   */
  public boolean isDisableLazyLoad() {
    return disableLazyLoad;
  }

  /**
   * Set true to turn off lazy loading.
   * <p>
   * Typically used to disable lazy loading on SQL based report beans.
   * </p>
   */
  public void setDisableLazyLoad(boolean disableLazyLoad) {
    this.disableLazyLoad = disableLazyLoad;
  }

  /**
   * Set the loaded status for the embedded bean.
   */
  public void setEmbeddedLoaded(Object embeddedBean) {
    if (embeddedBean instanceof EntityBean) {
      EntityBean eb = (EntityBean) embeddedBean;
      eb._ebean_getIntercept().setLoaded();
    }
  }

  /**
   * Return true if the embedded bean is new or dirty and hence needs saving.
   */
  public boolean isEmbeddedNewOrDirty(Object embeddedBean) {

    if (embeddedBean == null) {
      // if it was previously set then the owning bean would
      // have oldValues containing the previous embedded bean
      return false;
    }
    if (embeddedBean instanceof EntityBean) {
      return ((EntityBean) embeddedBean)._ebean_getIntercept().isNewOrDirty();

    } else {
      // non-enhanced so must assume it is new and needs to be saved
      return true;
    }
  }

  /**
   * Set the property names for a partially loaded bean.
   * 
   * @param loadedPropertyNames
   *          the names of the loaded properties
   */
  public void setLoadedProps(Set<String> loadedPropertyNames) {
    this.loadedProps = loadedPropertyNames;
  }

  /**
   * Return the set of property names for a partially loaded bean.
   */
  public Set<String> getLoadedProps() {
    return loadedProps;
  }

  /**
   * Return the set of property names for changed properties.
   */
  public Set<String> getChangedProps() {
    return changedProps;
  }

  /**
   * Return the property read or write that triggered the lazy load.
   */
  public String getLazyLoadProperty() {
    return lazyLoadProperty;
  }

  /**
   * Load the bean when it is a reference.
   */
  protected void loadBean(String loadProperty) {

    synchronized (this) {
      if (beanLoader == null) {
        BeanLoader serverLoader = (BeanLoader) Ebean.getServer(ebeanServerName);
        if (serverLoader == null) {
          throw new PersistenceException("Server [" + ebeanServerName + "] was not found?");
        }

        // For stand alone reference bean or after deserialisation lazy load
        // using the ebeanServer. Synchronise only on the bean.
        loadBeanInternal(loadProperty, serverLoader);
        return;
      }
    }

    synchronized (beanLoader) {
      // Lazy loading using LoadBeanContext which supports batch loading
      // Synchronise on the beanLoader (a 'node' of the LoadBeanContext 'tree')
      loadBeanInternal(loadProperty, beanLoader);
    }
  }

  /**
   * Invoke the lazy loading. This method is synchronised externally.
   */
  private void loadBeanInternal(String loadProperty, BeanLoader loader) {

    if (loaded && (loadedProps == null || loadedProps.contains(loadProperty))) {
      // race condition where multiple threads calling preGetter concurrently
      return;
    }

    if (disableLazyLoad) {
      loaded = true;
      return;
    }

    if (lazyLoadFailure) {
      // failed when batch lazy loaded by another bean in the batch
      throw new EntityNotFoundException("Bean has been deleted - lazy loading failed");
    }

    if (lazyLoadProperty == null) {

      lazyLoadProperty = loadProperty;

      if (nodeUsageCollector != null) {
        nodeUsageCollector.setLoadProperty(lazyLoadProperty);
      }

      loader.loadBean(this);

      if (lazyLoadFailure) {
        // failed when lazy loading this bean
        throw new EntityNotFoundException("Bean has been deleted - lazy loading failed");
      }

      // bean should be loaded and intercepting now. setLoaded() has
      // been called by the lazy loading mechanism
    }
  }

  /**
   * Create a copy of the bean as it is now. This is the original or 'old
   * values' prior to any modification. This is used to perform concurrency
   * testing.
   */
  protected void createOldValues() {

    oldValues = owner._ebean_createCopy();

    if (nodeUsageCollector != null) {
      nodeUsageCollector.setModified();
    }
  }

  /**
   * This is ONLY used for subclass entity beans.
   * <p>
   * This is not used when entity bean classes are enhanced via javaagent or ant
   * etc - only when a subclass is generated.
   * </p>
   * Returns a Serializable instance that is either the 'byte code generated'
   * object or a 'Vanilla' copy of this bean depending on
   * SerializeControl.isVanillaBeans().
   */
  public Object writeReplaceIntercept() throws ObjectStreamException {

    if (!SerializeControl.isVanillaBeans()) {
      return owner;
    }

    // creates a plain vanilla object and
    // copies the values from the owner
    return owner._ebean_createCopy();
  }

  /**
   * Helper method to check if two objects are equal.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  protected boolean areEqual(Object obj1, Object obj2) {
    if (obj1 == null) {
      return (obj2 == null);
    }
    if (obj2 == null) {
      return false;
    }
    if (obj1 == obj2) {
      return true;
    }
    if (obj1 instanceof BigDecimal) {
      // Use comparable for BigDecimal as equals
      // uses scale in comparison...
      if (obj2 instanceof BigDecimal) {
        Comparable com1 = (Comparable) obj1;
        return (com1.compareTo(obj2) == 0);

      } else {
        return false;
      }

    }
    if (obj1 instanceof URL) {
      // use the string format to determine if dirty
      return obj1.toString().equals(obj2.toString());
    }
    return obj1.equals(obj2);
  }

  /**
   * Method that is called prior to a getter method on the actual entity.
   * <p>
   * This checks if the bean is a reference and should be loaded.
   * </p>
   */
  public void preGetter(String propertyName) {
    if (!intercepting) {
      return;
    }

    if (!loaded) {
      loadBean(propertyName);
    } else if (loadedProps != null && !loadedProps.contains(propertyName)) {
      loadBean(propertyName);
    }

    if (nodeUsageCollector != null && loaded) {
      nodeUsageCollector.addUsed(propertyName);
    }
  }

  /**
   * Called for "enhancement" postSetter processing. This is around a PUTFIELD
   * so no need to check the newValue afterwards.
   */
  public void postSetter(PropertyChangeEvent event) {
    if (pcs != null && event != null) {
      pcs.firePropertyChange(event);
    }
  }

  /**
   * Called for "subclassed" postSetter processing. Here the newValue has to be
   * re-fetched (and passed into this method) in case there is code inside the
   * setter that further mutates the value.
   */
  public void postSetter(PropertyChangeEvent event, Object newValue) {
    if (pcs != null && event != null) {
      if (newValue != null && newValue.equals(event.getNewValue())) {
        pcs.firePropertyChange(event);
      } else {
        pcs.firePropertyChange(event.getPropertyName(), event.getOldValue(), newValue);
      }
    }
  }

  /**
   * OneToMany and ManyToMany don't have any interception so just check for
   * PropertyChangeSupport.
   */
  public PropertyChangeEvent preSetterMany(boolean interceptField, String propertyName,
      Object oldValue, Object newValue) {

    // skip setter interception on many's
    if (pcs != null) {
      return new PropertyChangeEvent(owner, propertyName, oldValue, newValue);
    } else {
      return null;
    }
  }

  private final void addDirty(String propertyName) {

    if (!intercepting) {
      return;
    }
    if (readOnly) {
      throw new IllegalStateException("This bean is readOnly");
    }

    if (loaded) {
      if (oldValues == null) {
        // first time this bean is being made dirty
        createOldValues();
      }
      if (changedProps == null) {
        changedProps = new HashSet<String>();
      }
      changedProps.add(propertyName);
    }
  }

  /**
   * Check to see if the values are not equal. If they are not equal then create
   * the old values for use with ConcurrencyMode.ALL.
   */
  public PropertyChangeEvent preSetter(boolean intercept, String propertyName, Object oldValue,
      Object newValue) {

    boolean changed = !areEqual(oldValue, newValue);

    if (intercept && changed) {
      addDirty(propertyName);
    }

    if (changed && pcs != null) {
      return new PropertyChangeEvent(owner, propertyName, oldValue, newValue);
    }

    return null;
  }

  /**
   * Check for primitive boolean.
   */
  public PropertyChangeEvent preSetter(boolean intercept, String propertyName, boolean oldValue,
      boolean newValue) {

    boolean changed = oldValue != newValue;

    if (intercept && changed) {
      addDirty(propertyName);
    }

    if (changed && pcs != null) {
      return new PropertyChangeEvent(owner, propertyName, Boolean.valueOf(oldValue),
          Boolean.valueOf(newValue));
    }

    return null;
  }

  /**
   * Check for primitive int.
   */
  public PropertyChangeEvent preSetter(boolean intercept, String propertyName, int oldValue,
      int newValue) {

    boolean changed = oldValue != newValue;

    if (intercept && changed) {
      addDirty(propertyName);
    }

    if (changed && pcs != null) {
      return new PropertyChangeEvent(owner, propertyName, Integer.valueOf(oldValue),
          Integer.valueOf(newValue));
    }
    return null;
  }

  /**
   * long.
   */
  public PropertyChangeEvent preSetter(boolean intercept, String propertyName, long oldValue,
      long newValue) {

    boolean changed = oldValue != newValue;

    if (intercept && changed) {
      addDirty(propertyName);
    }

    if (changed && pcs != null) {
      return new PropertyChangeEvent(owner, propertyName, Long.valueOf(oldValue),
          Long.valueOf(newValue));
    }
    return null;
  }

  /**
   * double.
   */
  public PropertyChangeEvent preSetter(boolean intercept, String propertyName, double oldValue,
      double newValue) {

    boolean changed = oldValue != newValue;

    if (intercept && changed) {
      addDirty(propertyName);
    }

    if (changed && pcs != null) {
      return new PropertyChangeEvent(owner, propertyName, Double.valueOf(oldValue),
          Double.valueOf(newValue));
    }
    return null;
  }

  /**
   * float.
   */
  public PropertyChangeEvent preSetter(boolean intercept, String propertyName, float oldValue,
      float newValue) {

    boolean changed = oldValue != newValue;

    if (intercept && changed) {
      addDirty(propertyName);
    }

    if (changed && pcs != null) {
      return new PropertyChangeEvent(owner, propertyName, Float.valueOf(oldValue),
          Float.valueOf(newValue));
    }
    return null;
  }

  /**
   * short.
   */
  public PropertyChangeEvent preSetter(boolean intercept, String propertyName, short oldValue,
      short newValue) {

    boolean changed = oldValue != newValue;

    if (intercept && changed) {
      addDirty(propertyName);
    }

    if (changed && pcs != null) {
      return new PropertyChangeEvent(owner, propertyName, Short.valueOf(oldValue),
          Short.valueOf(newValue));
    }
    return null;
  }

  /**
   * char.
   */
  public PropertyChangeEvent preSetter(boolean intercept, String propertyName, char oldValue,
      char newValue) {

    boolean changed = oldValue != newValue;

    if (intercept && changed) {
      addDirty(propertyName);
    }

    if (changed && pcs != null) {
      return new PropertyChangeEvent(owner, propertyName, Character.valueOf(oldValue),
          Character.valueOf(newValue));
    }
    return null;
  }

  /**
   * char.
   */
  public PropertyChangeEvent preSetter(boolean intercept, String propertyName, byte oldValue,
      byte newValue) {

    boolean changed = oldValue != newValue;

    if (intercept && changed) {
      addDirty(propertyName);
    }

    if (changed && pcs != null) {
      return new PropertyChangeEvent(owner, propertyName, Byte.valueOf(oldValue),
          Byte.valueOf(newValue));
    }
    return null;
  }

  /**
   * char[].
   */
  public PropertyChangeEvent preSetter(boolean intercept, String propertyName, char[] oldValue,
      char[] newValue) {

    boolean changed = !areEqualChars(oldValue, newValue);

    if (intercept && changed) {
      addDirty(propertyName);
    }

    if (changed && pcs != null) {
      return new PropertyChangeEvent(owner, propertyName, oldValue, newValue);
    }
    return null;
  }

  /**
   * byte[].
   */
  public PropertyChangeEvent preSetter(boolean intercept, String propertyName, byte[] oldValue,
      byte[] newValue) {

    boolean changed = !areEqualBytes(oldValue, newValue);

    if (intercept && changed) {
      addDirty(propertyName);
    }

    if (changed && pcs != null) {
      return new PropertyChangeEvent(owner, propertyName, oldValue, newValue);
    }
    return null;
  }

  private static boolean areEqualBytes(byte[] b1, byte[] b2) {
    if (b1 == null) {
      return (b2 == null);

    } else if (b2 == null) {
      return false;

    } else if (b1 == b2) {
      return true;

    } else if (b1.length != b2.length) {
      return false;
    }
    for (int i = 0; i < b1.length; i++) {
      if (b1[i] != b2[i]) {
        return false;
      }
    }
    return true;
  }

  private static boolean areEqualChars(char[] b1, char[] b2) {
    if (b1 == null) {
      return (b2 == null);

    } else if (b2 == null) {
      return false;

    } else if (b1 == b2) {
      return true;

    } else if (b1.length != b2.length) {
      return false;
    }
    for (int i = 0; i < b1.length; i++) {
      if (b1[i] != b2[i]) {
        return false;
      }
    }
    return true;
  }
}

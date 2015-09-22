package com.avaje.ebean.bean;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ValuePair;

/**
 * This is the object added to every entity bean using byte code enhancement.
 * <p>
 * This provides the mechanisms to support deferred fetching of reference beans
 * and oldValues generation for concurrency checking.
 * </p>
 */
public final class EntityBeanIntercept implements Serializable {

  private static final long serialVersionUID = -3664031775464862649L;

  private static final int STATE_NEW = 0;
  private static final int STATE_REFERENCE = 1;
  private static final int STATE_LOADED = 2;
  
  private transient NodeUsageCollector nodeUsageCollector;

  private transient PropertyChangeSupport pcs;

  private transient PersistenceContext persistenceContext;

  private transient BeanLoader beanLoader;

  private String ebeanServerName;

  /**
   * The actual entity bean that 'owns' this intercept.
   */
  private final EntityBean owner;

  private EntityBean embeddedOwner;
  private int embeddedOwnerIndex;

  /**
   * One of NEW, REF, UPD.
   */
  private int state;
  
  private boolean readOnly;
  
  private boolean dirty;
  
  /**
   * Flag set to disable lazy loading - typically for SQL "report" type entity beans.
   */
  private boolean disableLazyLoad;

  /**
   * Flag set when lazy loading failed due to the underlying bean being deleted in the DB.
   */
  private boolean lazyLoadFailure;

  /**
   * Used when a bean is partially filled.
   */
  private final boolean[] loadedProps;
  
  private boolean fullyLoadedBean;

  /**
   * Set of changed properties.
   */
  private boolean[] changedProps;
  
  /**
   * Flags indicating if a property is a dirty embedded bean. Used to distingush
   * between an embedded bean being completely overwritten and one of its
   * embedded properties being made dirty.
   */
  private boolean[] embeddedDirty;

  private Object[] origValues;

  private int lazyLoadProperty = -1;

  /**
   * Create a intercept with a given entity.
   * <p>
   * Refer to agent ProxyConstructor.
   * </p>
   */
  public EntityBeanIntercept(Object ownerBean) {
    this.owner = (EntityBean) ownerBean;
    this.loadedProps = new boolean[owner._ebean_getPropertyNames().length];
  }

  /**
   * Return the 'owning' entity bean.
   */
  public EntityBean getOwner() {
    return owner;
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
   * Return the owning bean for an embedded bean.
   */
  public Object getEmbeddedOwner() {
    return embeddedOwner;
  }
  
  /**
   * Return the property index (for the parent) of this embedded bean.
   */
  public int getEmbeddedOwnerIndex() {
    return embeddedOwnerIndex;
  }

  /**
   * Set the embedded beans owning bean.
   */
  public void setEmbeddedOwner(EntityBean parentBean, int embeddedOwnerIndex) {
    this.embeddedOwner = parentBean;
    this.embeddedOwnerIndex = embeddedOwnerIndex;
  }

  /**
   * Set the BeanLoader with PersistenceContext.
   */
  public void setBeanLoader(BeanLoader beanLoader, PersistenceContext ctx) {
    this.beanLoader = beanLoader;
    this.persistenceContext = ctx;
    this.ebeanServerName = beanLoader.getName();
  }

  /**
   * Set the BeanLoader.
   */
  public void setBeanLoader(BeanLoader beanLoader) {
    this.beanLoader = beanLoader;
    this.ebeanServerName = beanLoader.getName();
  }

  public boolean isFullyLoadedBean() {
    return fullyLoadedBean;
  }

  public void setFullyLoadedBean(boolean fullyLoadedBean) {
    this.fullyLoadedBean = fullyLoadedBean;
  }

  /**
   * Return true if this bean has been directly modified (it has oldValues) or
   * if any embedded beans are either new or dirty (and hence need saving).
   */
  public boolean isDirty() {
    return dirty;
  }

  /**
   * Called by an embedded bean onto its owner.
   */
  public void setEmbeddedDirty(int embeddedProperty) {
    this.dirty = true;
    setEmbeddedPropertyDirty(embeddedProperty);
  }
  
  public void setDirty(boolean dirty) {
    this.dirty = dirty;
  }

  /**
   * Return true if this entity bean is new and not yet saved.
   */
  public boolean isNew() {
    return state == STATE_NEW;
  }

  /**
   * Return true if the entity bean is new or dirty (and should be saved).
   */
  public boolean isNewOrDirty() {
    return isNew() || isDirty();
  }

  /**
   * Return true if only the Id property has been loaded.
   */
  public boolean hasIdOnly(int idIndex) {
    for (int i = 0; i < loadedProps.length; i++) {
      if (i == idIndex) {
        if (!loadedProps[i]) return false;
      } else if (loadedProps[i]) {
        return false; 
      }
    }
    return true;
  }
  
  /**
   * Return true if the entity is a reference.
   */
  public boolean isReference() {
    return state == STATE_REFERENCE;
  }

  /**
   * Set this as a reference object.
   */
  public void setReference(int idPos) {
    state = STATE_REFERENCE;
    if (idPos > -1) {
      // For cases where properties are set on constructor
      // set every non Id property to unloaded (for lazy loading)
      for (int i=0; i< loadedProps.length; i++) {
        if (i != idPos) { 
          loadedProps[i] = false;
        }
      }
    }
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
   * Return true if the entity has been loaded.
   */
  public boolean isLoaded() {
    return state == STATE_LOADED;
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
    this.state = STATE_LOADED;
    this.owner._ebean_setEmbeddedLoaded();
    this.lazyLoadProperty = -1;
    this.origValues = null;
    this.changedProps = null;
    this.dirty = false;
  }

  /**
   * When finished loading for lazy or refresh on an already partially populated
   * bean.
   */
  public void setLoadedLazy() {
    this.state = STATE_LOADED;
    this.lazyLoadProperty = -1;
  }

  /**
   * Check if the lazy load succeeded. If not then mark this bean as having
   * failed lazy loading due to the underlying row being deleted.
   * <p>
   * We mark the bean this way rather than immediately fail as we might be batch
   * lazy loading and this bean might not be used by the client code at all.
   * Instead we will fail as soon as the client code tries to use this bean.
   * </p>
   */
  public void checkLazyLoadFailure() {
    if (lazyLoadProperty != -1) {
      this.lazyLoadFailure = true;
    }
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
   * Return the original value that was changed via an update.
   */
  public Object getOrigValue(int propertyIndex) {
    if (origValues == null) {
      return null;
    }
    return origValues[propertyIndex];
  }
  
  /**
   * Finds the index position of a given property. Returns -1 if the property
   * can not be found.
   */
  public int findProperty(String propertyName) {
    String[] names = owner._ebean_getPropertyNames();
    for (int i = 0; i < names.length; i++) {
      if (names[i].equals(propertyName)) {
        return i;
      }
    }
    return -1;
  }
  
  /**
   * Return the property name for the given property.
   */
  public String getProperty(int propertyIndex) {
    if (propertyIndex == -1) {
      return null;
    }
    return owner._ebean_getPropertyName(propertyIndex);
  }
  
  /**
   * Return the number of properties.s
   */
  public int getPropertyLength() {
    return owner._ebean_getPropertyNames().length;
  }

  /**
   * Set the loaded state of the property given it's name.
   */
  public void setPropertyLoaded(String propertyName, boolean loaded) {
    int position = findProperty(propertyName);
    if (position == -1) {
      throw new IllegalArgumentException("Property "+propertyName+" not found");
    }
    loadedProps[position] = loaded;
  }

  /**
   * Set the property to be treated as unloaded. Used for properties initialised in default
   * constructor.
   */
  public void setPropertyUnloaded(int propertyIndex) {
    loadedProps[propertyIndex] = false;
  }
  
  /**
   * Set the property to be loaded.
   */
  public void setLoadedProperty(int propertyIndex) {
    loadedProps[propertyIndex] = true;
  }
  
  /**
   * Return true if the property is loaded.
   */
  public boolean isLoadedProperty(int propertyIndex) {
    return loadedProps[propertyIndex];
  }

  /**
   * Return true if the property is considered changed.
   */
  public boolean isChangedProperty(int propertyIndex) {
    return (changedProps != null && changedProps[propertyIndex]);
  }

  /**
   * Return true if the property was changed or if it is embedded and one of its
   * embedded properties is dirty.
   */
  public boolean isDirtyProperty(int propertyIndex) {
    return (changedProps != null && changedProps[propertyIndex] 
        || embeddedDirty != null && embeddedDirty[propertyIndex]);
  }

  /**
   * Explicitly mark a property as having been changed.
   */
  public void markPropertyAsChanged(int propertyIndex) {
    setChangedProperty(propertyIndex);
    setDirty(true);
  }
  
  private void setChangedProperty(int propertyIndex) {
    if (changedProps == null) {
      changedProps = new boolean[owner._ebean_getPropertyNames().length];
    }
    changedProps[propertyIndex] = true;
  }

  /**
   * Set that an embedded bean has had one of its properties changed.
   */
  private void setEmbeddedPropertyDirty(int propertyIndex) {
    if (embeddedDirty == null) {
      embeddedDirty = new boolean[owner._ebean_getPropertyNames().length];
    }
    embeddedDirty[propertyIndex] = true;
  }
  
  private void setOriginalValue(int propertyIndex, Object value) {
    if (origValues == null) {
      origValues = new Object[owner._ebean_getPropertyNames().length];
    }
    if (origValues[propertyIndex] == null) {
      origValues[propertyIndex] = value;
    }
  }

  /**
   * For forced update on a 'New' bean set all the loaded properties to changed.
   */
  public void setNewBeanForUpdate() {
  
    if (changedProps == null) {
      changedProps = new boolean[owner._ebean_getPropertyNames().length];
    }
    
    for (int i=0; i< loadedProps.length; i++) {
      if (loadedProps[i]) {
        changedProps[i] = true;        
      }
    }
    setDirty(true);
  }
  
  /**
   * Return the set of property names for a partially loaded bean.
   */
  public Set<String> getLoadedPropertyNames() {
    if (fullyLoadedBean) {
      return null;
    }
    Set<String> props = new LinkedHashSet<String>();
    for (int i=0; i<loadedProps.length; i++) {
      if (loadedProps[i]) {
        props.add(getProperty(i));
      }
    }
    return props;
  }

  /**
   * Return the set of dirty properties.
   */
  public Set<String> getDirtyPropertyNames() {
    Set<String> props = new LinkedHashSet<String>();
    addDirtyPropertyNames(props, null);
    return props;
  }
  
  /**
   * Recursively add dirty properties.
   */
  public void addDirtyPropertyNames(Set<String> props, String prefix) {
    int len = getPropertyLength();
    for (int i = 0; i < len; i++) {
      if (changedProps != null && changedProps[i]) {
        // the property has been changed on this bean
        String propName = (prefix == null ? getProperty(i) : prefix + getProperty(i));
        props.add(propName);
      } else if (embeddedDirty != null && embeddedDirty[i]) {
        // an embedded property has been changed - recurse
        EntityBean embeddedBean = (EntityBean)owner._ebean_getField(i);
        embeddedBean._ebean_getIntercept().addDirtyPropertyNames(props, getProperty(i)+".");
      }
    }
  }

  /**
   * Return true if any of the given property names are dirty.
   */
  public boolean hasDirtyProperty(Set<String> propertyNames) {

    String[] names = owner._ebean_getPropertyNames();
    int len = getPropertyLength();
    for (int i = 0; i < len; i++) {
      if (changedProps != null && changedProps[i]) {
        // the property has been changed on this bean
        if (propertyNames.contains(names[i])) {
          return true;
        }
      } else if (embeddedDirty != null && embeddedDirty[i]) {
        if (propertyNames.contains(names[i])) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Return a map of dirty properties with their new and old values.
   */
  public Map<String,ValuePair> getDirtyValues() {
    Map<String,ValuePair> dirtyValues = new LinkedHashMap<String, ValuePair>();
    addDirtyPropertyValues(dirtyValues, null);
    return dirtyValues;
  }
  
  /**
   * Recursively add dirty properties.
   */
  public void addDirtyPropertyValues(Map<String,ValuePair> dirtyValues, String prefix) {
    int len = getPropertyLength();
    for (int i = 0; i < len; i++) {
      if (changedProps != null && changedProps[i]) {
        // the property has been changed on this bean
        String propName = (prefix == null ? getProperty(i) : prefix + getProperty(i));
        Object newVal = owner._ebean_getField(i);
        Object oldVal = getOrigValue(i);

        dirtyValues.put(propName, new ValuePair(newVal, oldVal));
        
      } else if (embeddedDirty != null && embeddedDirty[i]) {
        // an embedded property has been changed - recurse
        EntityBean embeddedBean = (EntityBean)owner._ebean_getField(i);
        embeddedBean._ebean_getIntercept().addDirtyPropertyValues(dirtyValues, getProperty(i) + ".");
      }
    }
  }
  
  /**
   * Return a dirty property hash taking into account embedded beans.
   */
  public int getDirtyPropertyHash() {
    return addDirtyPropertyHash(37);
  }
  
  /**
   * Add and return a dirty property hash recursing into embedded beans.
   */
  public int addDirtyPropertyHash(int hash) {
    int len = getPropertyLength();
    for (int i = 0; i < len; i++) {
      if (changedProps != null && changedProps[i]) {
        // the property has been changed on this bean
        hash = hash * 31 + (i+1);
      } else if (embeddedDirty != null && embeddedDirty[i]) {
        // an embedded property has been changed - recurse
        EntityBean embeddedBean = (EntityBean)owner._ebean_getField(i);
        hash = hash * 31 + embeddedBean._ebean_getIntercept().addDirtyPropertyHash(hash);
      }
    }
    return hash;
  }

  /**
   * Return a loaded property hash.
   */
  public int getLoadedPropertyHash() {
    int hash = 37;
    int len = getPropertyLength();
    for (int i = 0; i < len; i++) {
      if (isLoadedProperty(i)) {
        hash = hash * 31 + (i+1);
      }
    }
    return hash;
  }

  /**
   * Return the set of property names for changed properties.
   */
  public boolean[] getChanged() {
    return changedProps;
  }

  public boolean[] getLoaded() {
    return loadedProps;
  }

  /**
   * Return the index of the property that triggered the lazy load.
   */
  public int getLazyLoadPropertyIndex() {
    return lazyLoadProperty;
  }
  
  /**
   * Return the property that triggered the lazy load.
   */
  public String getLazyLoadProperty() {
    return getProperty(lazyLoadProperty);
  }

  /**
   * Load the bean when it is a reference.
   */
  protected void loadBean(int loadProperty) {

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
  private void loadBeanInternal(int loadProperty, BeanLoader loader) {

    if (loadedProps == null || loadedProps[loadProperty]) {
      // race condition where multiple threads calling preGetter concurrently
      return;
    }

    if (lazyLoadFailure) {
      // failed when batch lazy loaded by another bean in the batch
      throw new EntityNotFoundException("Bean has been deleted - lazy loading failed");
    }

    if (lazyLoadProperty == -1) {

      lazyLoadProperty = loadProperty;

      if (nodeUsageCollector != null) {
        nodeUsageCollector.setLoadProperty(getProperty(lazyLoadProperty));
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
   * Called when a BeanCollection is initialised automatically.
   */
  public void initialisedMany(int propertyIndex) {
    loadedProps[propertyIndex] = true;
  }
  
  /**
   * Method that is called prior to a getter method on the actual entity.
   */
  public void preGetter(int propertyIndex) {
    if (state == STATE_NEW || disableLazyLoad) {
      return;
    }
    
    if (!isLoadedProperty(propertyIndex)) {
      loadBean(propertyIndex);
    }

    if (nodeUsageCollector != null) {
      nodeUsageCollector.addUsed(getProperty(propertyIndex));
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
  public PropertyChangeEvent preSetterMany(boolean interceptField, int propertyIndex, Object oldValue, Object newValue) {

    if (readOnly) {
      throw new IllegalStateException("This bean is readOnly");
    }
    
    setLoadedProperty(propertyIndex);

    // Bean itself not considered dirty when many changed
    if (pcs != null) {
      return new PropertyChangeEvent(owner, getProperty(propertyIndex), oldValue, newValue);
    } else {
      return null;
    }
  }
  
  private void setChangedPropertyValue(int propertyIndex, boolean setDirtyState, Object origValue) {

    if (readOnly) {
      throw new IllegalStateException("This bean is readOnly");
    }
    setChangedProperty(propertyIndex);

    if (setDirtyState) {
      setOriginalValue(propertyIndex, origValue);
      if (!dirty) {
        dirty = true;        
        if (embeddedOwner != null) {
          // Cascade dirty state from Embedded bean to parent bean
          embeddedOwner._ebean_getIntercept().setEmbeddedDirty(embeddedOwnerIndex);
        }
        if (nodeUsageCollector != null) {
          nodeUsageCollector.setModified();
        }
      }
    }
  }
  
  /**
   * Check to see if the values are not equal. If they are not equal then create
   * the old values for use with ConcurrencyMode.ALL.
   */
  public PropertyChangeEvent preSetter(boolean intercept, int propertyIndex, Object oldValue, Object newValue) {

    if (state == STATE_NEW) {
      setLoadedProperty(propertyIndex);
    } else if (!areEqual(oldValue, newValue)) {
      setChangedPropertyValue(propertyIndex, intercept, oldValue);   
    } else {
      return null;
    }
    
    return (pcs == null) ? null : new PropertyChangeEvent(owner, getProperty(propertyIndex), oldValue, newValue); 
  }
  
  
  /**
   * Check for primitive boolean.
   */
  public PropertyChangeEvent preSetter(boolean intercept, int propertyIndex, boolean oldValue, boolean newValue) {

    if (state == STATE_NEW) {
      setLoadedProperty(propertyIndex);
    } else if (oldValue != newValue) {
      setChangedPropertyValue(propertyIndex, intercept, oldValue);
    } else {
      return null;
    }
    return (pcs == null) ? null : new PropertyChangeEvent(owner, getProperty(propertyIndex), oldValue, newValue);
  }

  /**
   * Check for primitive int.
   */
  public PropertyChangeEvent preSetter(boolean intercept, int propertyIndex, int oldValue, int newValue) {

    if (state == STATE_NEW) {
      setLoadedProperty(propertyIndex);
    } else if (oldValue != newValue) {
      setChangedPropertyValue(propertyIndex, intercept, oldValue);
    } else {
      return null;
    }
    return (pcs == null) ? null : new PropertyChangeEvent(owner, getProperty(propertyIndex), oldValue, newValue);
  }

  /**
   * long.
   */
  public PropertyChangeEvent preSetter(boolean intercept, int propertyIndex, long oldValue, long newValue) {

    if (state == STATE_NEW) {
      setLoadedProperty(propertyIndex);  
    } else if (oldValue != newValue) {
      setChangedPropertyValue(propertyIndex, intercept, oldValue);
    } else {
      return null;
    }
    
    return (pcs == null) ? null : new PropertyChangeEvent(owner, getProperty(propertyIndex), oldValue, newValue);
  }

  /**
   * double.
   */
  public PropertyChangeEvent preSetter(boolean intercept, int propertyIndex, double oldValue, double newValue) {

    if (state == STATE_NEW) {
      setLoadedProperty(propertyIndex);
    } else if (oldValue != newValue) {
      setChangedPropertyValue(propertyIndex, intercept, oldValue);  
    } else {
      return null;
    }
    return (pcs == null) ? null : new PropertyChangeEvent(owner, getProperty(propertyIndex), oldValue, newValue);
  }

  /**
   * float.
   */
  public PropertyChangeEvent preSetter(boolean intercept, int propertyIndex, float oldValue, float newValue) {

    if (state == STATE_NEW) {
      setLoadedProperty(propertyIndex);
    } else if (oldValue != newValue) {
      setChangedPropertyValue(propertyIndex, intercept, oldValue);
    } else {
      return null;
    }
    return (pcs == null) ? null :  new PropertyChangeEvent(owner, getProperty(propertyIndex), oldValue, newValue);
  }

  /**
   * short.
   */
  public PropertyChangeEvent preSetter(boolean intercept, int propertyIndex, short oldValue, short newValue) {

    if (state == STATE_NEW) {
      setLoadedProperty(propertyIndex);
    } else if (oldValue != newValue) {
      setChangedPropertyValue(propertyIndex, intercept, oldValue);
    } else {
      return null;
    }
    return (pcs == null) ? null : new PropertyChangeEvent(owner, getProperty(propertyIndex), oldValue, newValue);
  }

  /**
   * char.
   */
  public PropertyChangeEvent preSetter(boolean intercept, int propertyIndex, char oldValue, char newValue) {

    if (state == STATE_NEW) {
      setLoadedProperty(propertyIndex);
    } else if (oldValue != newValue) {
      setChangedPropertyValue(propertyIndex, intercept, oldValue);
    } else {
      return null;
    }
    return (pcs == null) ? null : new PropertyChangeEvent(owner, getProperty(propertyIndex), oldValue, newValue);
  }

  /**
   * byte.
   */
  public PropertyChangeEvent preSetter(boolean intercept, int propertyIndex, byte oldValue, byte newValue) {

    if (state == STATE_NEW) {
      setLoadedProperty(propertyIndex);
    } else if (oldValue != newValue) {
      setChangedPropertyValue(propertyIndex, intercept, oldValue);
    } else {
      return null;
    }
    return (pcs == null) ? null : new PropertyChangeEvent(owner, getProperty(propertyIndex), oldValue, newValue);
  }

  /**
   * char[].
   */
  public PropertyChangeEvent preSetter(boolean intercept, int propertyIndex, char[] oldValue, char[] newValue) {

    if (state == STATE_NEW) {
      setLoadedProperty(propertyIndex);
    } else if (!areEqualChars(oldValue, newValue)) {
      setChangedPropertyValue(propertyIndex, intercept, oldValue);
    } else {
      return null;
    }
    return (pcs == null) ? null: new PropertyChangeEvent(owner, getProperty(propertyIndex), oldValue, newValue);
  }

  /**
   * byte[].
   */
  public PropertyChangeEvent preSetter(boolean intercept, int propertyIndex, byte[] oldValue, byte[] newValue) {

    if (state == STATE_NEW) {
      setLoadedProperty(propertyIndex);
    } else if (!areEqualBytes(oldValue, newValue)) {
      setChangedPropertyValue(propertyIndex, intercept, oldValue);
    } else {
      return null;
    }
    return (pcs == null) ? null : new PropertyChangeEvent(owner, getProperty(propertyIndex), oldValue, newValue);
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

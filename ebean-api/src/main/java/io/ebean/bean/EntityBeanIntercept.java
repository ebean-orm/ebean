package io.ebean.bean;

import io.ebean.ValuePair;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * This is the object associated to every entity bean using byte code enhancement.
 * <p>
 * This provides per property state such as loaded state, changed state, original values
 * as well as bean level dirty state etc.
 */
public interface EntityBeanIntercept extends Serializable {

  /**
   * Return the 'owning' entity bean.
   */
  EntityBean owner();

  /**
   * Return the persistenceContext.
   */
  PersistenceContext persistenceContext();

  /**
   * Set the persistenceContext.
   */
  void setPersistenceContext(PersistenceContext persistenceContext);

  /**
   * When accessing a property that is not loaded, throw a IllegalStateException
   * when true or invoke lazy loading.
   */
  void errorOnLazyLoad(boolean lazyLoadAsError);

  /**
   * Turn on profile collection.
   */
  void setNodeUsageCollector(NodeUsageCollector usageCollector);

  /**
   * Return the ownerId (IdClass).
   */
  Object ownerId();

  /**
   * Set the ownerId (IdClass).
   */
  void setOwnerId(Object ownerId);

  /**
   * Return the owning bean for an embedded bean.
   */
  Object embeddedOwner();

  /**
   * Return the property index (for the parent) of this embedded bean.
   */
  int embeddedOwnerIndex();

  /**
   * Clear the getter callback.
   */
  void clearGetterCallback();

  /**
   * Register the callback to be triggered when getter is called.
   * This is used primarily to automatically flush the JDBC batch.
   */
  void registerGetterCallback(PreGetterCallback getterCallback);

  /**
   * Set the embedded beans owning bean.
   */
  void setEmbeddedOwner(EntityBean parentBean, int embeddedOwnerIndex);

  /**
   * Set the BeanLoader with PersistenceContext.
   */
  void setBeanLoader(BeanLoader beanLoader, PersistenceContext ctx);

  /**
   * Set the BeanLoader.
   */
  void setBeanLoader(BeanLoader beanLoader);

  /**
   * Return true if the bean is fully loaded (not a partial).
   */
  boolean isFullyLoadedBean();

  /**
   * Set true when the bean is fully loaded (not a partial).
   */
  void setFullyLoadedBean(boolean fullyLoadedBean);

  /**
   * Return true if the bean is partially loaded.
   */
  boolean isPartial();

  /**
   * Return true if this bean has been directly modified (it has oldValues) or
   * if any embedded beans are either new or dirty (and hence need saving).
   */
  boolean isDirty();

  /**
   * Called by an embedded bean onto its owner.
   */
  void setEmbeddedDirty(int embeddedProperty);

  /**
   * Marks the bean as dirty.
   */
  void setDirty(boolean dirty);

  /**
   * Return true if this entity bean is new and not yet saved.
   */
  boolean isNew();

  /**
   * Return true if the entity bean is new or dirty (and should be saved).
   */
  boolean isNewOrDirty();

  /**
   * Return true if only the Id property has been loaded.
   */
  boolean hasIdOnly(int idIndex);

  /**
   * Return true if the entity is a reference.
   */
  boolean isReference();

  /**
   * Set this as a reference object.
   */
  void setReference(int idPos);

  /**
   * Set true when the bean has been loaded from L2 bean cache.
   * The effect of this is that we should skip the cache if there
   * is subsequent lazy loading (bean cache partially populated).
   */
  void setLoadedFromCache(boolean loadedFromCache);

  /**
   * Return true if this bean was loaded from L2 bean cache.
   */
  boolean isLoadedFromCache();

  /**
   * Return true if the bean should be treated as readOnly. If a setter method
   * is called when it is readOnly an Exception is thrown.
   */
  boolean isReadOnly();

  /**
   * Set the readOnly status. If readOnly then calls to setter methods through
   * an exception.
   */
  void setReadOnly(boolean readOnly);

  /**
   * Set the bean to be updated when persisted (for merge).
   */
  void setForceUpdate(boolean forceUpdate);

  /**
   * Return true if the entity should be updated.
   */
  boolean isUpdate();

  /**
   * Return true if the entity has been loaded.
   */
  boolean isLoaded();

  /**
   * Set the bean into NEW state.
   */
  void setNew();

  /**
   * Set the loaded state to true.
   * <p>
   * Calls to setter methods after the bean is loaded can result in
   * 'Old Values' being created.
   * <p>
   * Worth noting that this is also set after a insert/update. By doing so it
   * 'resets' the bean for making further changes and saving again.
   */
  void setLoaded();

  /**
   * When finished loading for lazy or refresh on an already partially populated bean.
   */
  void setLoadedLazy();

  /**
   * Set lazy load failure flag.
   */
  void setLazyLoadFailure(Object ownerId);

  /**
   * Return true if the bean is marked as having failed lazy loading.
   */
  boolean isLazyLoadFailure();

  /**
   * Return true if lazy loading is disabled.
   */
  boolean isDisableLazyLoad();

  /**
   * Set true to turn off lazy loading.
   */
  void setDisableLazyLoad(boolean disableLazyLoad);

  /**
   * Set the loaded status for the embedded bean.
   */
  void setEmbeddedLoaded(Object embeddedBean);

  /**
   * Return true if the embedded bean is new or dirty and hence needs saving.
   */
  boolean isEmbeddedNewOrDirty(Object embeddedBean);

  /**
   * Return the original value that was changed via an update.
   */
  Object origValue(int propertyIndex);

  /**
   * Finds the index position of a given property. Returns -1 if the
   * property can not be found.
   */
  int findProperty(String propertyName);

  /**
   * Return the property name for the given property.
   */
  String property(int propertyIndex);

  /**
   * Return the number of properties.
   */
  int propertyLength();

  /**
   * Set the loaded state of the property given it's name.
   */
  void setPropertyLoaded(String propertyName, boolean loaded);

  /**
   * Set the property to be treated as unloaded. Used for properties initialised in default constructor.
   */
  void setPropertyUnloaded(int propertyIndex);

  /**
   * Set the property to be loaded.
   */
  void setLoadedProperty(int propertyIndex);

  /**
   * Set all properties to be loaded (post insert).
   */
  void setLoadedPropertyAll();

  /**
   * Return true if the property is loaded.
   */
  boolean isLoadedProperty(int propertyIndex);

  /**
   * Return true if the property is considered changed.
   */
  boolean isChangedProperty(int propertyIndex);

  /**
   * Return true if the property was changed or if it is embedded and one of its
   * embedded properties is dirty.
   */
  boolean isDirtyProperty(int propertyIndex);

  /**
   * Explicitly mark a property as having been changed.
   */
  void markPropertyAsChanged(int propertyIndex);

  /**
   * Set the changed state for the given property.
   */
  void setChangedProperty(int propertyIndex);

  /**
   * Set the changed and loaded state for the given property.
   */
  void setChangeLoaded(int propertyIndex);

  /**
   * Set that an embedded bean has had one of its properties changed.
   */
  void setEmbeddedPropertyDirty(int propertyIndex);

  /**
   * Set the original value for the property.
   */
  void setOriginalValue(int propertyIndex, Object value);

  /**
   * Set old value but force it to be set regardless if it already has a value.
   */
  void setOriginalValueForce(int propertyIndex, Object value);

  /**
   * For forced update on a 'New' bean set all the loaded properties to changed.
   */
  void setNewBeanForUpdate();

  /**
   * Return the set of property names for a partially loaded bean.
   */
  Set<String> loadedPropertyNames();

  /**
   * Return the array of flags indicating the dirty properties.
   */
  boolean[] dirtyProperties();

  /**
   * Return the set of dirty properties.
   */
  Set<String> dirtyPropertyNames();

  /**
   * Recursively add dirty properties.
   */
  void addDirtyPropertyNames(Set<String> props, String prefix);

  /**
   * Return true if any of the given property names are dirty.
   */
  boolean hasDirtyProperty(Set<String> propertyNames);

  /**
   * Return a map of dirty properties with their new and old values.
   */
  Map<String, ValuePair> dirtyValues();

  /**
   * Recursively add dirty properties.
   */
  void addDirtyPropertyValues(Map<String, ValuePair> dirtyValues, String prefix);

  /**
   * Recursively add dirty properties.
   */
  void addDirtyPropertyValues(BeanDiffVisitor visitor);

  /**
   * Return a dirty property hash taking into account embedded beans.
   */
  StringBuilder dirtyPropertyKey();

  /**
   * Add and return a dirty property hash.
   */
  void addDirtyPropertyKey(StringBuilder sb);

  /**
   * Return a loaded property hash.
   */
  StringBuilder loadedPropertyKey();

  /**
   * Return the loaded state for all the properties.
   */
  boolean[] loaded();

  /**
   * Return the index of the property that triggered the lazy load.
   */
  int lazyLoadPropertyIndex();

  /**
   * Return the property that triggered the lazy load.
   */
  String lazyLoadProperty();

  /**
   * Load the bean when it is a reference.
   */
  void loadBean(int loadProperty);

  /**
   * Invoke the lazy loading. This method is synchronised externally.
   */
  void loadBeanInternal(int loadProperty, BeanLoader loader);

  /**
   * Called when a BeanCollection is initialised automatically.
   */
  void initialisedMany(int propertyIndex);

  /**
   * Invoke the PreGetterCallback if it has been set due to getter for the given property.
   */
  void preGetterCallback(int propertyIndex);

  /**
   * Called prior to Id property getter.
   */
  void preGetId();

  /**
   * Method that is called prior to a getter method on the actual entity.
   */
  void preGetter(int propertyIndex);

  /**
   * OneToMany and ManyToMany only set loaded state.
   */
  void preSetterMany(boolean interceptField, int propertyIndex, Object oldValue, Object newValue);

  /**
   * Set the property changed state, bean dirtyState and property original value.
   */
  void setChangedPropertyValue(int propertyIndex, boolean setDirtyState, Object origValue);

  /**
   * Set the dirty state on the bean.
   */
  void setDirtyStatus();

  /**
   * Check to see if the values are not equal. If they are not equal then create
   * the old values for use with ConcurrencyMode.ALL.
   */
  void preSetter(boolean intercept, int propertyIndex, Object oldValue, Object newValue);

  /**
   * Check for primitive boolean.
   */
  void preSetter(boolean intercept, int propertyIndex, boolean oldValue, boolean newValue);

  /**
   * Check for primitive int.
   */
  void preSetter(boolean intercept, int propertyIndex, int oldValue, int newValue);

  /**
   * Check for primitive long.
   */
  void preSetter(boolean intercept, int propertyIndex, long oldValue, long newValue);

  /**
   * Check for primitive double.
   */
  void preSetter(boolean intercept, int propertyIndex, double oldValue, double newValue);

  /**
   * Check for primitive float.
   */
  void preSetter(boolean intercept, int propertyIndex, float oldValue, float newValue);

  /**
   * Check for primitive short.
   */
  void preSetter(boolean intercept, int propertyIndex, short oldValue, short newValue);

  /**
   * Check for primitive char.
   */
  void preSetter(boolean intercept, int propertyIndex, char oldValue, char newValue);

  /**
   * Check for primitive byte.
   */
  void preSetter(boolean intercept, int propertyIndex, byte oldValue, byte newValue);

  /**
   * Check for primitive char array.
   */
  void preSetter(boolean intercept, int propertyIndex, char[] oldValue, char[] newValue);

  /**
   * Check for primitive byte array.
   */
  void preSetter(boolean intercept, int propertyIndex, byte[] oldValue, byte[] newValue);

  /**
   * Explicitly set an old value with force (the old value is forced even it is already set).
   */
  void setOldValue(int propertyIndex, Object oldValue);

  /**
   * Return the sort order value for an order column.
   */
  int sortOrder();

  /**
   * Set the sort order value for an order column.
   */
  void setSortOrder(int sortOrder);

  /**
   * Set if the entity was deleted from a BeanCollection.
   */
  void setDeletedFromCollection(boolean deletedFromCollection);

  /**
   * Return true if the bean was orphan deleted from a collection.
   */
  boolean isOrphanDelete();

  /**
   * Set the load error that happened on this property.
   */
  void setLoadError(int propertyIndex, Exception t);

  /**
   * Returns the loadErrors.
   */
  Map<String, Exception> loadErrors();

  /**
   * Return true if the property has its changed state set.
   */
  boolean isChangedProp(int propertyIndex);

  /**
   * Return the MutableValueInfo for the given property or null.
   */
  MutableValueInfo mutableInfo(int propertyIndex);

  /**
   * Set the MutableValueInfo for the given property.
   */
  void mutableInfo(int propertyIndex, MutableValueInfo info);

  /**
   * Dirty detection set the next mutable property content and info .
   * <p>
   * Set here as the mutable property dirty detection is based on json content comparison.
   * We only want to perform the json serialisation once so storing it here as part of
   * dirty detection so that we can get it back to bind in insert or update etc.
   */
  void mutableNext(int propertyIndex, MutableValueNext next);

  /**
   * Update the 'next' mutable info returning the content that was obtained via dirty detection.
   */
  String mutableNext(int propertyIndex);
}

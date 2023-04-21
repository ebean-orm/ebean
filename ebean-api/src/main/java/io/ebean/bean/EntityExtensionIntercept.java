package io.ebean.bean;

import io.ebean.ValuePair;

import java.util.Map;
import java.util.Set;

/**
 * Intercept for classes annotated with &#64;EntityExtension. The intercept will delegate all calls to the base intercept of the
 * ExtendableBean and adds given offset to all field operations.
 *
 * @author Roland Praml, FOCONIS AG
 */
public class EntityExtensionIntercept implements EntityBeanIntercept {
  private final EntityBeanIntercept base;
  private final int offset;

  public EntityExtensionIntercept(Object ownerBean, int offset, EntityBean base) {
    this.base = base._ebean_getIntercept();
    this.offset = offset;
  }

  @Override
  public EntityBean owner() {
    return base.owner();
  }

  @Override
  public PersistenceContext persistenceContext() {
    return base.persistenceContext();
  }

  @Override
  public void setPersistenceContext(PersistenceContext persistenceContext) {
    base.setPersistenceContext(persistenceContext);
  }

  @Override
  public void setNodeUsageCollector(NodeUsageCollector usageCollector) {
    base.setNodeUsageCollector(usageCollector);
  }

  @Override
  public Object ownerId() {
    return base.ownerId();
  }

  @Override
  public void setOwnerId(Object ownerId) {
    base.setOwnerId(ownerId);
  }

  @Override
  public Object embeddedOwner() {
    return base.embeddedOwner();
  }

  @Override
  public int embeddedOwnerIndex() {
    return base.embeddedOwnerIndex();
  }

  @Override
  public void clearGetterCallback() {
    base.clearGetterCallback();
  }

  @Override
  public void registerGetterCallback(PreGetterCallback getterCallback) {
    base.registerGetterCallback(getterCallback);
  }

  @Override
  public void setEmbeddedOwner(EntityBean parentBean, int embeddedOwnerIndex) {
    base.setEmbeddedOwner(parentBean, embeddedOwnerIndex);
  }

  @Override
  public void setBeanLoader(BeanLoader beanLoader, PersistenceContext ctx) {
    base.setBeanLoader(beanLoader, ctx);
  }

  @Override
  public void setBeanLoader(BeanLoader beanLoader) {
    base.setBeanLoader(beanLoader);
  }

  @Override
  public boolean isFullyLoadedBean() {
    return base.isFullyLoadedBean();
  }

  @Override
  public void setFullyLoadedBean(boolean fullyLoadedBean) {
    base.setFullyLoadedBean(fullyLoadedBean);
  }

  @Override
  public boolean isPartial() {
    return base.isPartial();
  }

  @Override
  public boolean isDirty() {
    return base.isDirty();
  }

  @Override
  public void setEmbeddedDirty(int embeddedProperty) {
    base.setEmbeddedDirty(embeddedProperty + offset);
  }

  @Override
  public void setDirty(boolean dirty) {
    base.setDirty(dirty);
  }

  @Override
  public boolean isNew() {
    return base.isNew();
  }

  @Override
  public boolean isNewOrDirty() {
    return base.isNewOrDirty();
  }

  @Override
  public boolean hasIdOnly(int idIndex) {
    return base.hasIdOnly(idIndex + offset);
  }

  @Override
  public boolean isReference() {
    return base.isReference();
  }

  @Override
  public void setReference(int idPos) {
    base.setReference(idPos + offset);
  }

  @Override
  public void setLoadedFromCache(boolean loadedFromCache) {
    base.setLoadedFromCache(loadedFromCache);
  }

  @Override
  public boolean isLoadedFromCache() {
    return base.isLoadedFromCache();
  }

  @Override
  public boolean isReadOnly() {
    return base.isReadOnly();
  }

  @Override
  public void setReadOnly(boolean readOnly) {
    base.setReadOnly(readOnly);
  }

  @Override
  public void setForceUpdate(boolean forceUpdate) {
    base.setForceUpdate(forceUpdate);
  }

  @Override
  public boolean isUpdate() {
    return base.isUpdate();
  }

  @Override
  public boolean isLoaded() {
    return base.isLoaded();
  }

  @Override
  public void setNew() {
    base.setNew();
  }

  @Override
  public void setLoaded() {
    base.setLoaded();
  }

  @Override
  public void setLoadedLazy() {
    base.setLoadedLazy();
  }

  @Override
  public void setLazyLoadFailure(Object ownerId) {
    base.setLazyLoadFailure(ownerId);
  }

  @Override
  public boolean isLazyLoadFailure() {
    return base.isLazyLoadFailure();
  }

  @Override
  public boolean isDisableLazyLoad() {
    return base.isDisableLazyLoad();
  }

  @Override
  public void setDisableLazyLoad(boolean disableLazyLoad) {
    base.setDisableLazyLoad(disableLazyLoad);
  }

  @Override
  public void setEmbeddedLoaded(Object embeddedBean) {
    base.setEmbeddedLoaded(embeddedBean);
  }

  @Override
  public boolean isEmbeddedNewOrDirty(Object embeddedBean) {
    return base.isEmbeddedNewOrDirty(embeddedBean);
  }

  @Override
  public Object origValue(int propertyIndex) {
    return base.origValue(propertyIndex + offset);
  }

  @Override
  public int findProperty(String propertyName) {
    return base.findProperty(propertyName);
  }

  @Override
  public String property(int propertyIndex) {
    return base.property(propertyIndex + offset);
  }

  @Override
  public int propertyLength() {
    return base.propertyLength();
  }

  @Override
  public void setPropertyLoaded(String propertyName, boolean loaded) {
    base.setPropertyLoaded(propertyName, loaded);
  }

  @Override
  public void setPropertyUnloaded(int propertyIndex) {
    base.setPropertyUnloaded(propertyIndex + offset);
  }

  @Override
  public void setLoadedProperty(int propertyIndex) {
    base.setLoadedProperty(propertyIndex + offset);
  }

  @Override
  public void setLoadedPropertyAll() {
    base.setLoadedPropertyAll();
  }

  @Override
  public boolean isLoadedProperty(int propertyIndex) {
    return base.isLoadedProperty(propertyIndex + offset);
  }

  @Override
  public boolean isChangedProperty(int propertyIndex) {
    return base.isChangedProperty(propertyIndex + offset);
  }

  @Override
  public boolean isDirtyProperty(int propertyIndex) {
    return base.isDirtyProperty(propertyIndex + offset);
  }

  @Override
  public void markPropertyAsChanged(int propertyIndex) {
    base.markPropertyAsChanged(propertyIndex + offset);
  }

  @Override
  public void setChangedProperty(int propertyIndex) {
    base.setChangedProperty(propertyIndex + offset);
  }

  @Override
  public void setChangeLoaded(int propertyIndex) {
    base.setChangeLoaded(propertyIndex + offset);
  }

  @Override
  public void setEmbeddedPropertyDirty(int propertyIndex) {
    base.setEmbeddedPropertyDirty(propertyIndex + offset);
  }

  @Override
  public void setOriginalValue(int propertyIndex, Object value) {
    base.setOriginalValue(propertyIndex + offset, value);
  }

  @Override
  public void setOriginalValueForce(int propertyIndex, Object value) {
    base.setOriginalValueForce(propertyIndex + offset, value);
  }

  @Override
  public void setNewBeanForUpdate() {
    base.setNewBeanForUpdate();
  }

  @Override
  public Set<String> loadedPropertyNames() {
    return base.loadedPropertyNames();
  }

  @Override
  public boolean[] dirtyProperties() {
    return base.dirtyProperties();
  }

  @Override
  public Set<String> dirtyPropertyNames() {
    return base.dirtyPropertyNames();
  }

  @Override
  public void addDirtyPropertyNames(Set<String> props, String prefix) {
    base.addDirtyPropertyNames(props, prefix);
  }

  @Override
  public boolean hasDirtyProperty(Set<String> propertyNames) {
    return base.hasDirtyProperty(propertyNames);
  }

  @Override
  public Map<String, ValuePair> dirtyValues() {
    return base.dirtyValues();
  }

  @Override
  public void addDirtyPropertyValues(Map<String, ValuePair> dirtyValues, String prefix) {
    base.addDirtyPropertyValues(dirtyValues, prefix);
  }

  @Override
  public void addDirtyPropertyValues(BeanDiffVisitor visitor) {
    base.addDirtyPropertyValues(visitor);
  }

  @Override
  public StringBuilder dirtyPropertyKey() {
    return base.dirtyPropertyKey();
  }

  @Override
  public void addDirtyPropertyKey(StringBuilder sb) {
    base.addDirtyPropertyKey(sb);
  }

  @Override
  public StringBuilder loadedPropertyKey() {
    return base.loadedPropertyKey();
  }

  @Override
  public boolean[] loaded() {
    return base.loaded();
  }

  @Override
  public int lazyLoadPropertyIndex() {
    return base.lazyLoadPropertyIndex() - offset;
  }

  @Override
  public String lazyLoadProperty() {
    return base.lazyLoadProperty();
  }

  @Override
  public void loadBean(int loadProperty) {
    base.loadBean(loadProperty);
  }

  @Override
  public void loadBeanInternal(int loadProperty, BeanLoader loader) {
    base.loadBeanInternal(loadProperty + offset, loader);
  }

  @Override
  public void initialisedMany(int propertyIndex) {
    base.initialisedMany(propertyIndex + offset);
  }

  @Override
  public void preGetterCallback(int propertyIndex) {
    base.preGetterCallback(propertyIndex + offset);
  }

  @Override
  public void preGetId() {
    base.preGetId();
  }

  @Override
  public void preGetter(int propertyIndex) {
    base.preGetter(propertyIndex + offset);
  }

  @Override
  public void preSetterMany(boolean interceptField, int propertyIndex, Object oldValue, Object newValue) {
    base.preSetterMany(interceptField, propertyIndex + offset, oldValue, newValue);
  }

  @Override
  public void setChangedPropertyValue(int propertyIndex, boolean setDirtyState, Object origValue) {
    base.setChangedPropertyValue(propertyIndex + offset, setDirtyState, origValue);
  }

  @Override
  public void setDirtyStatus() {
    base.setDirtyStatus();
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, Object oldValue, Object newValue) {
    base.preSetter(intercept, propertyIndex + offset, oldValue, newValue);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, boolean oldValue, boolean newValue) {
    base.preSetter(intercept, propertyIndex + offset, oldValue, newValue);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, int oldValue, int newValue) {
    base.preSetter(intercept, propertyIndex + offset, oldValue, newValue);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, long oldValue, long newValue) {
    base.preSetter(intercept, propertyIndex + offset, oldValue, newValue);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, double oldValue, double newValue) {
    base.preSetter(intercept, propertyIndex + offset, oldValue, newValue);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, float oldValue, float newValue) {
    base.preSetter(intercept, propertyIndex + offset, oldValue, newValue);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, short oldValue, short newValue) {
    base.preSetter(intercept, propertyIndex + offset, oldValue, newValue);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, char oldValue, char newValue) {
    base.preSetter(intercept, propertyIndex + offset, oldValue, newValue);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, byte oldValue, byte newValue) {
    base.preSetter(intercept, propertyIndex + offset, oldValue, newValue);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, char[] oldValue, char[] newValue) {
    base.preSetter(intercept, propertyIndex + offset, oldValue, newValue);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, byte[] oldValue, byte[] newValue) {
    base.preSetter(intercept, propertyIndex + offset, oldValue, newValue);
  }

  @Override
  public void setOldValue(int propertyIndex, Object oldValue) {
    base.setOldValue(propertyIndex + offset, oldValue);
  }

  @Override
  public int sortOrder() {
    return base.sortOrder();
  }

  @Override
  public void setSortOrder(int sortOrder) {
    base.setSortOrder(sortOrder);
  }

  @Override
  public void setDeletedFromCollection(boolean deletedFromCollection) {
    base.setDeletedFromCollection(deletedFromCollection);
  }

  @Override
  public boolean isOrphanDelete() {
    return base.isOrphanDelete();
  }

  @Override
  public void setLoadError(int propertyIndex, Exception t) {
    base.setLoadError(propertyIndex + offset, t);
  }

  @Override
  public Map<String, Exception> loadErrors() {
    return base.loadErrors();
  }

  @Override
  public boolean isChangedProp(int propertyIndex) {
    return base.isChangedProp(propertyIndex + offset);
  }

  @Override
  public MutableValueInfo mutableInfo(int propertyIndex) {
    return base.mutableInfo(propertyIndex + offset);
  }

  @Override
  public void mutableInfo(int propertyIndex, MutableValueInfo info) {
    base.mutableInfo(propertyIndex + offset, info);
  }

  @Override
  public void mutableNext(int propertyIndex, MutableValueNext next) {
    base.mutableNext(propertyIndex + offset, next);
  }

  @Override
  public String mutableNext(int propertyIndex) {
    return base.mutableNext(propertyIndex + offset);
  }

  @Override
  public Object value(int propertyIndex) {
    return base.value(propertyIndex + offset);
  }

  @Override
  public Object valueIntercept(int propertyIndex) {
    return base.valueIntercept(propertyIndex + offset);
  }

  @Override
  public void setValue(int propertyIndex, Object value) {
    base.setValue(propertyIndex + offset, value);
  }

  @Override
  public void setValueIntercept(int propertyIndex, Object value) {
    base.setValueIntercept(propertyIndex + offset, value);
  }
}

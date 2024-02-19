package io.ebean.bean;

import io.ebean.ValuePair;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * EntityBeanIntercept optimised for read only use.
 * <p>
 * For the read only use this intercept doesn't need to hold any state that is normally
 * required for updates such as per property changed, dirty state, original values
 * bean state etc.
 */
public final class InterceptReadOnly extends InterceptBase {

  /**
   * Create with a given entity.
   */
  public InterceptReadOnly(Object ownerBean) {
    super(ownerBean);
  }

  @Override
  public void preGetId() {
    // do nothing
  }

  @Override
  public void preGetter(int propertyIndex) {
    if (disableLazyLoad) {
      return;
    }
    if (!isLoadedProperty(propertyIndex)) {
      loadBean(propertyIndex);
    }
  }

  private void preSetter(int propertyIndex) {
    if (readOnly) {
      throw new IllegalStateException("This bean is readOnly");
    }
    setLoadedProperty(propertyIndex);
  }

  @Override
  public void preSetterMany(boolean interceptField, int propertyIndex, Object oldValue, Object newValue) {
    preSetter(propertyIndex);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, Object oldValue, Object newValue) {
    preSetter(propertyIndex);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, boolean oldValue, boolean newValue) {
    preSetter(propertyIndex);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, int oldValue, int newValue) {
    preSetter(propertyIndex);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, long oldValue, long newValue) {
    preSetter(propertyIndex);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, double oldValue, double newValue) {
    preSetter(propertyIndex);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, float oldValue, float newValue) {
    preSetter(propertyIndex);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, short oldValue, short newValue) {
    preSetter(propertyIndex);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, char oldValue, char newValue) {
    preSetter(propertyIndex);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, byte oldValue, byte newValue) {
    preSetter(propertyIndex);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, char[] oldValue, char[] newValue) {
    preSetter(propertyIndex);
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, byte[] oldValue, byte[] newValue) {
    preSetter(propertyIndex);
  }


  // ------------------------------------------------------------------------------------
  // The following method have "Do Nothing" behaviour as not interested in mutation state
  // ------------------------------------------------------------------------------------

  @Override
  public void setNodeUsageCollector(NodeUsageCollector usageCollector) {

  }

  @Override
  public Object embeddedOwner() {
    return null;
  }

  @Override
  public int embeddedOwnerIndex() {
    return 0;
  }

  @Override
  public void clearGetterCallback() {

  }

  @Override
  public void registerGetterCallback(PreGetterCallback getterCallback) {

  }

  @Override
  public void setEmbeddedOwner(EntityBean parentBean, int embeddedOwnerIndex) {

  }

  @Override
  public boolean isDirty() {
    return false;
  }

  @Override
  public void setEmbeddedDirty(int embeddedProperty) {

  }

  @Override
  public void setDirty(boolean dirty) {

  }

  @Override
  public boolean isNew() {
    return false;
  }

  @Override
  public boolean isNewOrDirty() {
    return false;
  }

  @Override
  public boolean isReference() {
    return false;
  }

  @Override
  public void setReference(int idPos) {

  }

  @Override
  public void setLoadedFromCache(boolean loadedFromCache) {

  }

  @Override
  public boolean isLoadedFromCache() {
    return false;
  }

  @Override
  public void setForceUpdate(boolean forceUpdate) {

  }

  @Override
  public boolean isUpdate() {
    return false;
  }

  @Override
  public boolean isLoaded() {
    return true;
  }

  @Override
  public void setNew() {

  }

  @Override
  public void setLoaded() {

  }

  @Override
  public void setLoadedLazy() {

  }

  @Override
  public boolean isEmbeddedNewOrDirty(Object embeddedBean) {
    return false;
  }

  @Override
  public Object origValue(int propertyIndex) {
    return null;
  }


  @Override
  public boolean isChangedProperty(int propertyIndex) {
    return false;
  }

  @Override
  public boolean isDirtyProperty(int propertyIndex) {
    return false;
  }

  @Override
  public void markPropertyAsChanged(int propertyIndex) {

  }

  @Override
  public void setChangedProperty(int propertyIndex) {

  }

  @Override
  public void setChangeLoaded(int propertyIndex) {

  }

  @Override
  public void setEmbeddedPropertyDirty(int propertyIndex) {

  }

  @Override
  public void setOriginalValue(int propertyIndex, Object value) {

  }

  @Override
  public void setOriginalValueForce(int propertyIndex, Object value) {

  }

  @Override
  public void setNewBeanForUpdate() {

  }

  @Override
  public boolean[] dirtyProperties() {
    return new boolean[0];
  }

  @Override
  public Set<String> dirtyPropertyNames() {
    return Collections.emptySet();
  }

  @Override
  public void addDirtyPropertyNames(Set<String> props, String prefix) {

  }

  @Override
  public boolean hasDirtyProperty(Set<String> propertyNames) {
    return false;
  }

  @Override
  public Map<String, ValuePair> dirtyValues() {
    return Collections.emptyMap();
  }

  @Override
  public void addDirtyPropertyValues(Map<String, ValuePair> dirtyValues, String prefix) {

  }

  @Override
  public void addDirtyPropertyValues(BeanDiffVisitor visitor) {

  }

  @Override
  public StringBuilder dirtyPropertyKey() {
    return null;
  }

  @Override
  public void addDirtyPropertyKey(StringBuilder sb) {

  }

  @Override
  public void preGetterCallback(int propertyIndex) {

  }

  @Override
  public void setChangedPropertyValue(int propertyIndex, boolean setDirtyState, Object origValue) {

  }

  @Override
  public void setDirtyStatus() {

  }

  @Override
  public void setOldValue(int propertyIndex, Object oldValue) {

  }

  @Override
  public int sortOrder() {
    return 0;
  }

  @Override
  public void setSortOrder(int sortOrder) {

  }

  @Override
  public void setDeletedFromCollection(boolean deletedFromCollection) {

  }

  @Override
  public boolean isOrphanDelete() {
    return false;
  }

  @Override
  public void setLoadError(int propertyIndex, Exception t) {

  }

  @Override
  public Map<String, Exception> loadErrors() {
    return null;
  }

  @Override
  public boolean isChangedProp(int i) {
    return false;
  }

  @Override
  public MutableValueInfo mutableInfo(int propertyIndex) {
    return null;
  }

  @Override
  public void mutableInfo(int propertyIndex, MutableValueInfo info) {

  }

  @Override
  public void mutableNext(int propertyIndex, MutableValueNext next) {

  }

  @Override
  public String mutableNext(int propertyIndex) {
    return null;
  }
}

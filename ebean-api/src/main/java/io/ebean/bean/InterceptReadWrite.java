package io.ebean.bean;

import io.ebean.ValuePair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;

/**
 * This is the object added to every entity bean using byte code enhancement.
 * <p>
 * This provides the mechanisms to support deferred fetching of reference beans
 * and oldValues generation for concurrency checking.
 */
public final class InterceptReadWrite extends InterceptBase {

  private static final long serialVersionUID = -3664031775464862649L;

  private static final int STATE_NEW = 0;
  private static final int STATE_REFERENCE = 1;
  private static final int STATE_LOADED = 2;

  private static final byte FLAG_CHANGED_PROP = 2;
  private static final byte FLAG_CHANGEDLOADED_PROP = 3;
  /**
   * Flags indicating if a property is a dirty embedded bean. Used to distinguish between an
   * embedded bean being completely overwritten vs one with embedded properties that are dirty.
   */
  private static final byte FLAG_EMBEDDED_DIRTY = 4;
  private static final byte FLAG_ORIG_VALUE_SET = 8;
  /**
   * Flags indicating if the mutable hash is set.
   */
  private static final byte FLAG_MUTABLE_HASH_SET = 16;

  private transient NodeUsageCollector nodeUsageCollector;
  private transient PreGetterCallback preGetterCallback;

  private EntityBean embeddedOwner;
  private int embeddedOwnerIndex;
  /**
   * One of NEW, REF, LOADED.
   */
  private int state;
  private boolean deletedFromCollection;
  private boolean forceUpdate;
  private boolean dirty;
  private boolean loadedFromCache;
  private Object[] origValues;
  private Exception[] loadErrors;
  private int sortOrder;

  /**
   * Holds information of json loaded jackson beans (e.g. the original json or checksum).
   */
  private MutableValueInfo[] mutableInfo;

  /**
   * Holds json content determined at point of dirty check.
   * Stored here on dirty check such that we only convert to json once.
   */
  private MutableValueNext[] mutableNext;

  /**
   * Create with a given entity.
   */
  public InterceptReadWrite(Object ownerBean) {
    super(ownerBean);
  }

  /**
   * EXPERIMENTAL - Constructor only for use by serialization frameworks.
   */
  public InterceptReadWrite() {
    super();
  }

  @Override
  public void setNodeUsageCollector(NodeUsageCollector usageCollector) {
    this.nodeUsageCollector = usageCollector;
  }

  @Override
  public Object embeddedOwner() {
    return embeddedOwner;
  }

  @Override
  public int embeddedOwnerIndex() {
    return embeddedOwnerIndex;
  }

  @Override
  public void clearGetterCallback() {
    this.preGetterCallback = null;
  }

  @Override
  public void registerGetterCallback(PreGetterCallback getterCallback) {
    this.preGetterCallback = getterCallback;
  }

  @Override
  public void setEmbeddedOwner(EntityBean parentBean, int embeddedOwnerIndex) {
    this.embeddedOwner = parentBean;
    this.embeddedOwnerIndex = embeddedOwnerIndex;
  }

  @Override
  public boolean isDirty() {
    if (dirty) {
      return true;
    }
    if (mutableInfo != null) {
      for (int i = 0; i < mutableInfo.length; i++) {
        if (mutableInfo[i] != null && !mutableInfo[i].isEqualToObject(owner._ebean_getField(i))) {
          dirty = true;
          break;
        }
      }
    }
    return dirty;
  }

  @Override
  public void setEmbeddedDirty(int embeddedProperty) {
    this.dirty = true;
    setEmbeddedPropertyDirty(embeddedProperty);
  }

  @Override
  public void setDirty(boolean dirty) {
    this.dirty = dirty;
  }

  @Override
  public boolean isNew() {
    return state == STATE_NEW;
  }

  @Override
  public boolean isNewOrDirty() {
    return isNew() || isDirty();
  }

  @Override
  public boolean isReference() {
    return state == STATE_REFERENCE;
  }

  @Override
  public void setReference(int idPos) {
    state = STATE_REFERENCE;
    if (idPos > -1) {
      // For cases where properties are set on constructor
      // set every non Id property to unloaded (for lazy loading)
      for (int i = 0; i < flags.length; i++) {
        if (i != idPos) {
          flags[i] &= ~FLAG_LOADED_PROP;
        }
      }
    }
  }

  @Override
  public void setLoadedFromCache(boolean loadedFromCache) {
    this.loadedFromCache = loadedFromCache;
  }

  @Override
  public boolean isLoadedFromCache() {
    return loadedFromCache;
  }

  @Override
  public void setForceUpdate(boolean forceUpdate) {
    this.forceUpdate = forceUpdate;
  }

  @Override
  public boolean isUpdate() {
    return forceUpdate || state == STATE_LOADED || state == STATE_REFERENCE;
  }

  @Override
  public boolean isLoaded() {
    return state == STATE_LOADED;
  }

  @Override
  public void setNew() {
    this.state = STATE_NEW;
  }

  @Override
  public void setLoaded() {
    this.state = STATE_LOADED;
    this.owner._ebean_setEmbeddedLoaded();
    this.lazyLoadProperty = -1;
    this.origValues = null;
    // after save, transfer the mutable next values back to mutable info
    if (mutableNext != null) {
      for (int i = 0; i < mutableNext.length; i++) {
        MutableValueNext next = mutableNext[i];
        if (next != null) {
          mutableInfo(i, next.info());
        }
      }
    }
    this.mutableNext = null;
    for (int i = 0; i < flags.length; i++) {
      flags[i] &= ~(FLAG_CHANGED_PROP | FLAG_ORIG_VALUE_SET);
    }
    this.dirty = false;
  }

  @Override
  public void setLoadedLazy() {
    this.state = STATE_LOADED;
    this.lazyLoadProperty = -1;
  }

  @Override
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

  @Override
  public Object origValue(int propertyIndex) {
    if ((flags[propertyIndex] & (FLAG_ORIG_VALUE_SET | FLAG_MUTABLE_HASH_SET)) == FLAG_MUTABLE_HASH_SET) {
      // mutable hash set, but not ORIG_VALUE
      setOriginalValue(propertyIndex, mutableInfo[propertyIndex].get());
    }
    if (origValues == null) {
      return null;
    }
    return origValues[propertyIndex];
  }

  @Override
  public boolean isChangedProperty(int propertyIndex) {
    return (flags[propertyIndex] & FLAG_CHANGED_PROP) != 0;
  }

  @Override
  public boolean isDirtyProperty(int propertyIndex) {
    return (flags[propertyIndex] & (FLAG_CHANGED_PROP + FLAG_EMBEDDED_DIRTY)) != 0;
  }

  @Override
  public void markPropertyAsChanged(int propertyIndex) {
    setChangedProperty(propertyIndex);
    setDirty(true);
  }

  @Override
  public void setChangedProperty(int propertyIndex) {
    flags[propertyIndex] |= FLAG_CHANGED_PROP;
  }

  @Override
  public void setChangeLoaded(int propertyIndex) {
    flags[propertyIndex] |= FLAG_CHANGEDLOADED_PROP;
  }

  @Override
  public void setEmbeddedPropertyDirty(int propertyIndex) {
    flags[propertyIndex] |= FLAG_EMBEDDED_DIRTY;
  }

  @Override
  public void setOriginalValue(int propertyIndex, Object value) {
    if (origValues == null) {
      origValues = new Object[flags.length];
    }
    if ((flags[propertyIndex] & FLAG_ORIG_VALUE_SET) == 0) {
      flags[propertyIndex] |= FLAG_ORIG_VALUE_SET;
      origValues[propertyIndex] = value;
    }
  }

  @Override
  public void setOriginalValueForce(int propertyIndex, Object value) {
    if (origValues == null) {
      origValues = new Object[flags.length];
    }
    origValues[propertyIndex] = value;
  }

  @Override
  public void setNewBeanForUpdate() {
    for (int i = 0; i < flags.length; i++) {
      if ((flags[i] & FLAG_LOADED_PROP) != 0) {
        flags[i] |= FLAG_CHANGED_PROP;
      }
    }
    setDirty(true);
  }

  @Override
  public boolean[] dirtyProperties() {
    final int len = propertyLength();
    final boolean[] dirties = new boolean[len];
    for (int i = 0; i < len; i++) {
      // this, or an embedded property has been changed - recurse
      dirties[i] = (flags[i] & (FLAG_CHANGED_PROP + FLAG_EMBEDDED_DIRTY)) != 0;
    }
    return dirties;
  }

  @Override
  public Set<String> dirtyPropertyNames() {
    final Set<String> props = new LinkedHashSet<>();
    addDirtyPropertyNames(props, null);
    return props;
  }

  @Override
  public void addDirtyPropertyNames(Set<String> props, String prefix) {
    final int len = propertyLength();
    for (int i = 0; i < len; i++) {
      if (isChangedProp(i)) {
        // the property has been changed on this bean
        props.add((prefix == null ? property(i) : prefix + property(i)));
      } else if ((flags[i] & FLAG_EMBEDDED_DIRTY) != 0) {
        // an embedded property has been changed - recurse
        final EntityBean embeddedBean = (EntityBean) owner._ebean_getField(i);
        embeddedBean._ebean_getIntercept().addDirtyPropertyNames(props, property(i) + ".");
      }
    }
  }

  @Override
  public boolean hasDirtyProperty(Set<String> propertyNames) {
    final String[] names = owner._ebean_getPropertyNames();
    final int len = propertyLength();
    for (int i = 0; i < len; i++) {
      if (isChangedProp(i)) {
        if (propertyNames.contains(names[i])) {
          return true;
        }
      } else if ((flags[i] & FLAG_EMBEDDED_DIRTY) != 0) {
        if (propertyNames.contains(names[i])) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public Map<String, ValuePair> dirtyValues() {
    final Map<String, ValuePair> dirtyValues = new LinkedHashMap<>();
    addDirtyPropertyValues(dirtyValues, null);
    return dirtyValues;
  }

  @Override
  public void addDirtyPropertyValues(Map<String, ValuePair> dirtyValues, String prefix) {
    final int len = propertyLength();
    for (int i = 0; i < len; i++) {
      if (isChangedProp(i)) {
        // the property has been changed on this bean
        final String propName = (prefix == null ? property(i) : prefix + property(i));
        final Object newVal = owner._ebean_getField(i);
        final Object oldVal = origValue(i);
        if (notEqual(oldVal, newVal)) {
          dirtyValues.put(propName, new ValuePair(newVal, oldVal));
        }
      } else if ((flags[i] & FLAG_EMBEDDED_DIRTY) != 0) {
        // an embedded property has been changed - recurse
        final EntityBean embeddedBean = (EntityBean) owner._ebean_getField(i);
        embeddedBean._ebean_getIntercept().addDirtyPropertyValues(dirtyValues, property(i) + ".");
      }
    }
  }

  @Override
  public void addDirtyPropertyValues(BeanDiffVisitor visitor) {
    final int len = propertyLength();
    for (int i = 0; i < len; i++) {
      if (isChangedProp(i)) {
        // the property has been changed on this bean
        final Object newVal = owner._ebean_getField(i);
        final Object oldVal = origValue(i);
        if (notEqual(oldVal, newVal)) {
          visitor.visit(i, newVal, oldVal);
        }
      } else if ((flags[i] & FLAG_EMBEDDED_DIRTY) != 0) {
        // an embedded property has been changed - recurse
        final EntityBean embeddedBean = (EntityBean) owner._ebean_getField(i);
        visitor.visitPush(i);
        embeddedBean._ebean_getIntercept().addDirtyPropertyValues(visitor);
        visitor.visitPop();
      }
    }
  }

  @Override
  public StringBuilder dirtyPropertyKey() {
    final StringBuilder sb = new StringBuilder();
    addDirtyPropertyKey(sb);
    return sb;
  }

  @Override
  public void addDirtyPropertyKey(StringBuilder sb) {
    if (sortOrder > 0) {
      sb.append("s,");
    }
    final int len = propertyLength();
    for (int i = 0; i < len; i++) {
      if ((flags[i] & FLAG_CHANGED_PROP) != 0) { // we do not check against mutablecontent here.
        sb.append(i).append(',');
      } else if ((flags[i] & FLAG_EMBEDDED_DIRTY) != 0) {
        // an embedded property has been changed - recurse
        sb.append(i).append('[');
        ((EntityBean) owner._ebean_getField(i))._ebean_getIntercept().addDirtyPropertyKey(sb);
        sb.append(']');
      }
    }
  }

  /**
   * Helper method to check if two objects are equal.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  static boolean notEqual(Object obj1, Object obj2) {
    if (obj1 == null) {
      return (obj2 != null);
    }
    if (obj2 == null) {
      return true;
    }
    if (obj1 == obj2) {
      return false;
    }
    if (obj1 instanceof BigDecimal) {
      // Use comparable for BigDecimal as equals
      // uses scale in comparison...
      if (obj2 instanceof BigDecimal) {
        Comparable com1 = (Comparable) obj1;
        return (com1.compareTo(obj2) != 0);
      } else {
        return true;
      }
    }
    if (obj1 instanceof URL) {
      // use the string format to determine if dirty
      return !obj1.toString().equals(obj2.toString());
    }
    if (obj1 instanceof File && obj2 instanceof File) {
      File file1 = (File) obj1;
      File file2 = (File) obj2;
      if (file1.exists() && file2.exists() && file1.length() == file2.length()) {
        return notEqualContent(file1, file2);
      }
    }
    return !obj1.equals(obj2);
  }

  private static boolean notEqualContent(File file1, File file2) {
    try (InputStream is1 = new FileInputStream(file1); InputStream is2 = new FileInputStream(file2)) {
      byte[] buf1 = new byte[16384];
      byte[] buf2 = new byte[16384];
      int len1;
      int len2;
      while ((len1 = is1.read(buf1)) != -1 && (len2 = is2.read(buf2)) != -1) {
        if (len1 != len2) {
          return true;
        }
        if (!Arrays.equals(buf1, buf2)) {
          // it does not matter, if we compare more than len1/len2 as the remainig
          // bytes in the buffers are either 0 or equals from the prev. loop.
          return true;
        }
      }
      return false;
    } catch (IOException e) {
      return true; // handle them as "not equals"
    }
  }

  @Override
  public void preGetterCallback(int propertyIndex) {
    PreGetterCallback preGetterCallback = this.preGetterCallback;
    if (preGetterCallback != null) {
      preGetterCallback.preGetterTrigger(propertyIndex);
    }
  }

  @Override
  public void preGetId() {
    preGetterCallback(-1);
  }

  @Override
  public void preGetter(int propertyIndex) {
    preGetterCallback(propertyIndex);
    if (state == STATE_NEW || disableLazyLoad) {
      return;
    }
    if (!isLoadedProperty(propertyIndex)) {
      loadBean(propertyIndex);
    }
    if (nodeUsageCollector != null) {
      nodeUsageCollector.addUsed(property(propertyIndex));
    }
  }

  @Override
  public void preSetterMany(boolean interceptField, int propertyIndex, Object oldValue, Object newValue) {
    if (state == STATE_NEW) {
      setLoadedProperty(propertyIndex);
    } else {
      if (readOnly) {
        throw new IllegalStateException("This bean is readOnly");
      }
      setChangeLoaded(propertyIndex);
    }
  }

  @Override
  public void setChangedPropertyValue(int propertyIndex, boolean setDirtyState, Object origValue) {
    if (readOnly) {
      throw new IllegalStateException("This bean is readOnly");
    }
    setChangedProperty(propertyIndex);
    if (setDirtyState) {
      setOriginalValue(propertyIndex, origValue);
      setDirtyStatus();
    }
  }

  @Override
  public void setDirtyStatus() {
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

  @Override
  public void preSetter(boolean intercept, int propertyIndex, Object oldValue, Object newValue) {
    if (state == STATE_NEW) {
      setLoadedProperty(propertyIndex);
    } else if (notEqual(oldValue, newValue)) {
      setChangedPropertyValue(propertyIndex, intercept, oldValue);
    }
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, boolean oldValue, boolean newValue) {
    if (state == STATE_NEW) {
      setLoadedProperty(propertyIndex);
    } else if (oldValue != newValue) {
      setChangedPropertyValue(propertyIndex, intercept, oldValue);
    }
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, int oldValue, int newValue) {
    if (state == STATE_NEW) {
      setLoadedProperty(propertyIndex);
    } else if (oldValue != newValue) {
      setChangedPropertyValue(propertyIndex, intercept, oldValue);
    }
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, long oldValue, long newValue) {
    if (state == STATE_NEW) {
      setLoadedProperty(propertyIndex);
    } else if (oldValue != newValue) {
      setChangedPropertyValue(propertyIndex, intercept, oldValue);
    }
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, double oldValue, double newValue) {
    if (state == STATE_NEW) {
      setLoadedProperty(propertyIndex);
    } else if (Double.compare(oldValue, newValue) != 0) {
      setChangedPropertyValue(propertyIndex, intercept, oldValue);
    }
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, float oldValue, float newValue) {
    if (state == STATE_NEW) {
      setLoadedProperty(propertyIndex);
    } else if (Float.compare(oldValue, newValue) != 0) {
      setChangedPropertyValue(propertyIndex, intercept, oldValue);
    }
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, short oldValue, short newValue) {
    if (state == STATE_NEW) {
      setLoadedProperty(propertyIndex);
    } else if (oldValue != newValue) {
      setChangedPropertyValue(propertyIndex, intercept, oldValue);
    }
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, char oldValue, char newValue) {
    if (state == STATE_NEW) {
      setLoadedProperty(propertyIndex);
    } else if (oldValue != newValue) {
      setChangedPropertyValue(propertyIndex, intercept, oldValue);
    }
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, byte oldValue, byte newValue) {
    if (state == STATE_NEW) {
      setLoadedProperty(propertyIndex);
    } else if (oldValue != newValue) {
      setChangedPropertyValue(propertyIndex, intercept, oldValue);
    }
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, char[] oldValue, char[] newValue) {
    if (state == STATE_NEW) {
      setLoadedProperty(propertyIndex);
    } else if (!Arrays.equals(oldValue, newValue)) {
      setChangedPropertyValue(propertyIndex, intercept, oldValue);
    }
  }

  @Override
  public void preSetter(boolean intercept, int propertyIndex, byte[] oldValue, byte[] newValue) {
    if (state == STATE_NEW) {
      setLoadedProperty(propertyIndex);
    } else if (!Arrays.equals(oldValue, newValue)) {
      setChangedPropertyValue(propertyIndex, intercept, oldValue);
    }
  }

  @Override
  public void setOldValue(int propertyIndex, Object oldValue) {
    setChangedProperty(propertyIndex);
    setOriginalValueForce(propertyIndex, oldValue);
    setDirtyStatus();
  }

  @Override
  public int sortOrder() {
    return sortOrder;
  }

  @Override
  public void setSortOrder(int sortOrder) {
    this.sortOrder = sortOrder;
  }

  @Override
  public void setDeletedFromCollection(final boolean deletedFromCollection) {
    this.deletedFromCollection = deletedFromCollection;
  }

  @Override
  public boolean isOrphanDelete() {
    return deletedFromCollection && !isNew();
  }

  @Override
  public void setLoadError(int propertyIndex, Exception t) {
    if (loadErrors == null) {
      loadErrors = new Exception[flags.length];
    }
    loadErrors[propertyIndex] = t;
    flags[propertyIndex] |= FLAG_LOADED_PROP;
  }

  @Override
  public Map<String, Exception> loadErrors() {
    if (loadErrors == null) {
      return Collections.emptyMap();
    }
    Map<String, Exception> ret = null;
    int len = propertyLength();
    for (int i = 0; i < len; i++) {
      final Exception loadError = loadErrors[i];
      if (loadError != null) {
        if (ret == null) {
          ret = new LinkedHashMap<>();
        }
        ret.put(property(i), loadError);
      }
    }
    return ret;
  }

  @Override
  public boolean isChangedProp(int i) {
    if ((flags[i] & FLAG_CHANGED_PROP) != 0) {
      return true;
    } else if (mutableInfo == null || mutableInfo[i] == null || mutableInfo[i].isEqualToObject(owner._ebean_getField(i))) {
      return false;
    } else {
      // mark for change
      flags[i] |= FLAG_CHANGED_PROP;
      dirty = true; // this makes the bean automatically dirty!
      return true;
    }
  }

  @Override
  public MutableValueInfo mutableInfo(int propertyIndex) {
    return mutableInfo == null ? null : mutableInfo[propertyIndex];
  }

  @Override
  public void mutableInfo(int propertyIndex, MutableValueInfo info) {
    if (mutableInfo == null) {
      mutableInfo = new MutableValueInfo[flags.length];
    }
    flags[propertyIndex] |= FLAG_MUTABLE_HASH_SET;
    mutableInfo[propertyIndex] = info;
  }

  @Override
  public void mutableNext(int propertyIndex, MutableValueNext next) {
    if (mutableNext == null) {
      mutableNext = new MutableValueNext[flags.length];
    }
    mutableNext[propertyIndex] = next;
  }

  @Override
  public String mutableNext(int propertyIndex) {
    if (mutableNext == null) {
      return null;
    }
    final MutableValueNext next = mutableNext[propertyIndex];
    return next != null ? next.content() : null;
  }

  @Override
  public void freeze() {
    super.freeze();
    // not allowed to be used for updates
    this.dirty = false;
    this.forceUpdate = false;
    this.deletedFromCollection = false;
    this.origValues = null;
    this.nodeUsageCollector = null;
    this.embeddedOwner = null;
  }
}

package io.ebean.bean;

import io.avaje.applog.AppLog;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.ValuePair;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.Logger.Level.WARNING;

/**
 * This is the object added to every entity bean using byte code enhancement.
 * <p>
 * This provides the mechanisms to support deferred fetching of reference beans
 * and oldValues generation for concurrency checking.
 */
public final class InterceptReadWrite extends InterceptBase implements EntityBeanIntercept {
  private static final long serialVersionUID = 1834735632647183821L;

  public static final System.Logger log = AppLog.getLogger("io.ebean.bean");

  private static final int STATE_NEW = 0;
  private static final int STATE_REFERENCE = 1;
  private static final int STATE_LOADED = 2;

  /**
   * Used when a bean is partially loaded.
   */
  private static final byte FLAG_LOADED_PROP = 1;
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

  /**
   * Flag indicates that warning was logged.
   */
  private static final byte FLAG_MUTABLE_WARN_LOGGED = 32;

  private final ReentrantLock lock = new ReentrantLock();
  private transient NodeUsageCollector nodeUsageCollector;
  private transient PersistenceContext persistenceContext;
  private transient BeanLoader beanLoader;
  private transient PreGetterCallback preGetterCallback;

  private String ebeanServerName;
  private boolean deletedFromCollection;
  private EntityBean embeddedOwner;
  private int embeddedOwnerIndex;
  /**
   * One of NEW, REF, UPD.
   */
  private int state;
  private boolean forceUpdate;
  private boolean readOnly;
  private boolean dirty;
  /**
   * Flag set to disable lazy loading.
   */
  private boolean disableLazyLoad;
  /**
   * Flag set when lazy loading failed due to the underlying bean being deleted in the DB.
   */
  private boolean lazyLoadFailure;
  private boolean fullyLoadedBean;
  private boolean loadedFromCache;
  private final byte[] flags;
  private Object[] origValues;
  private Exception[] loadErrors;
  private int lazyLoadProperty = -1;
  private Object ownerId;
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
    super((EntityBean) ownerBean);
    this.flags = new byte[super.propertyLength()];
  }

  /**
   * EXPERIMENTAL - Constructor only for use by serialization frameworks.
   */
  public InterceptReadWrite() {
    super(null);
    this.flags = null;
  }

  @Override
  public String toString() {
    return "InterceptReadWrite@" + hashCode() + "{state=" + state +
      (dirty ? " dirty;" : "") +
      (forceUpdate ? " forceUpdate;" : "") +
      (readOnly ? " readOnly;" : "") +
      (disableLazyLoad ? " disableLazyLoad;" : "") +
      (lazyLoadFailure ? " lazyLoadFailure;" : "") +
      (fullyLoadedBean ? " fullyLoadedBean;" : "") +
      (loadedFromCache ? " loadedFromCache;" : "") +
      ", pc=" + System.identityHashCode(persistenceContext) +
      ", flags=" + Arrays.toString(flags) +
      (lazyLoadProperty > -1 ? (", lazyLoadProperty=" + lazyLoadProperty) : "") +
      ", loader=" + beanLoader +
      (ownerId != null ? (", ownerId=" + ownerId) : "") +
      '}';
  }

  @Override
  public EntityBean owner() {
    return owner;
  }

  @Override
  public PersistenceContext persistenceContext() {
    return persistenceContext;
  }

  @Override
  public void setPersistenceContext(PersistenceContext persistenceContext) {
    this.persistenceContext = persistenceContext;
  }

  @Override
  public void setNodeUsageCollector(NodeUsageCollector usageCollector) {
    this.nodeUsageCollector = usageCollector;
  }

  @Override
  public Object ownerId() {
    return ownerId;
  }

  @Override
  public void setOwnerId(Object ownerId) {
    this.ownerId = ownerId;
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
  public void setBeanLoader(BeanLoader beanLoader, PersistenceContext ctx) {
    this.beanLoader = beanLoader;
    this.persistenceContext = ctx;
    this.ebeanServerName = beanLoader.name();
  }

  @Override
  public void setBeanLoader(BeanLoader beanLoader) {
    this.beanLoader = beanLoader;
    this.ebeanServerName = beanLoader.name();
  }

  @Override
  public boolean isFullyLoadedBean() {
    return fullyLoadedBean;
  }

  @Override
  public void setFullyLoadedBean(boolean fullyLoadedBean) {
    this.fullyLoadedBean = fullyLoadedBean;
  }

  @Override
  public boolean isPartial() {
    for (byte flag : flags) {
      if ((flag & FLAG_LOADED_PROP) == 0) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isDirty() {
    if (dirty) {
      return true;
    }
    if (mutableInfo != null) {
      for (int i = 0; i < mutableInfo.length; i++) {
        if ((flags[i] & FLAG_MUTABLE_WARN_LOGGED) == FLAG_MUTABLE_WARN_LOGGED) {
           break; // do not check again and do NOT mark as dirty
        }
        if (mutableInfo[i] != null && !mutableInfo[i].isEqualToObject(value(i))) {
          if (readOnly) {
            log.log(WARNING, "Mutable object in {0}.{1} ({2}) changed. Not setting bean dirty, because it is readonly",
              owner.getClass().getName(), property(i), owner);
            flags[i] |= FLAG_MUTABLE_WARN_LOGGED;
          } else {
            dirty = true;
          }
          break;
        }
      }
    }
    return dirty;
  }

  @Override
  public void setEmbeddedDirty(int embeddedProperty) {
    checkReadonly();
    this.dirty = true;
    setEmbeddedPropertyDirty(embeddedProperty);
  }

  @Override
  public void setDirty(boolean dirty) {
    if (dirty) {
      checkReadonly();
    }
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
  public boolean hasIdOnly(int idIndex) {
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
  public boolean isReadOnly() {
    return readOnly;
  }

  @Override
  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
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
  public void setLazyLoadFailure(Object ownerId) {
    this.lazyLoadFailure = true;
    this.ownerId = ownerId;
  }

  @Override
  public boolean isLazyLoadFailure() {
    return lazyLoadFailure;
  }

  @Override
  public boolean isDisableLazyLoad() {
    return disableLazyLoad;
  }

  @Override
  public void setDisableLazyLoad(boolean disableLazyLoad) {
    this.disableLazyLoad = disableLazyLoad;
  }

  @Override
  public void setEmbeddedLoaded(Object embeddedBean) {
    if (embeddedBean instanceof EntityBean) {
      ((EntityBean) embeddedBean)._ebean_getIntercept().setLoaded();
    }
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
  public int propertyLength() {
    return flags.length;
  }

  @Override
  public void setPropertyLoaded(String propertyName, boolean loaded) {
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
  public void setPropertyUnloaded(int propertyIndex) {
    flags[propertyIndex] &= ~FLAG_LOADED_PROP;
  }

  @Override
  public void setLoadedProperty(int propertyIndex) {
    flags[propertyIndex] |= FLAG_LOADED_PROP;
  }

  @Override
  public void setLoadedPropertyAll() {
    for (int i = 0; i < flags.length; i++) {
      flags[i] |= FLAG_LOADED_PROP;
    }
  }

  @Override
  public boolean isLoadedProperty(int propertyIndex) {
    return (flags[propertyIndex] & FLAG_LOADED_PROP) != 0;
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
  public Set<String> loadedPropertyNames() {
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
        final EntityBean embeddedBean = (EntityBean) value(i);
        embeddedBean._ebean_getIntercept().addDirtyPropertyNames(props, property(i) + ".");
      }
    }
  }

  @Override
  public boolean hasDirtyProperty(Set<String> propertyNames) {
    String[] names = owner._ebean_getPropertyNames();
    final int len = propertyLength();
    int i;
    for (i = 0; i < len; i++) {
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
    for (ExtensionAccessor acc : owner._ebean_getExtensionAccessors()) {
      names = acc.getProperties();
      for (int j = 0; j < names.length; j++) {
        if (isChangedProp(i)) {
          if (propertyNames.contains(names[j])) {
            return true;
          }
        } else if ((flags[i] & FLAG_EMBEDDED_DIRTY) != 0) {
          if (propertyNames.contains(names[j])) {
            return true;
          }
        }
        i++;
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
        final Object newVal = value(i);
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
        final Object newVal = value(i);
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

  @Override
  public StringBuilder loadedPropertyKey() {
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
  public boolean[] loaded() {
    final boolean[] ret = new boolean[flags.length];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = (flags[i] & FLAG_LOADED_PROP) != 0;
    }
    return ret;
  }

  @Override
  public int lazyLoadPropertyIndex() {
    return lazyLoadProperty;
  }

  @Override
  public String lazyLoadProperty() {
    return property(lazyLoadProperty);
  }

  @Override
  public void loadBean(int loadProperty) {
    lock.lock();
    try {
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
  public void loadBeanInternal(int loadProperty, BeanLoader loader) {
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
  public void initialisedMany(int propertyIndex) {
    flags[propertyIndex] |= FLAG_LOADED_PROP;
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
      checkReadonly();
      setChangeLoaded(propertyIndex);
    }
  }

  @Override
  public void setChangedPropertyValue(int propertyIndex, boolean setDirtyState, Object origValue) {
    checkReadonly();
    setChangedProperty(propertyIndex);
    if (setDirtyState) {
      setOriginalValue(propertyIndex, origValue);
      setDirtyStatus();
    }
  }

  @Override
  public void setDirtyStatus() {
    if (!dirty) {
      checkReadonly();
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

  private void checkReadonly() {
    if (readOnly) {
      throw new IllegalStateException("This bean is readOnly");
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
    } else if (mutableInfo == null || mutableInfo[i] == null || mutableInfo[i].isEqualToObject(value(i))) {
      return false;
    } else {
      checkReadonly();
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
}

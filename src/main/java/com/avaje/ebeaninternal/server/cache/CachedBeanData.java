package com.avaje.ebeaninternal.server.cache;

import java.util.Arrays;

/**
 * Data held in the bean cache for cached beans.
 */
public class CachedBeanData {

  private final long whenCreated;
  private final Object sharableBean;
  private final boolean[] loaded;
  private final Object[] data;
  
  private final boolean naturalKeyUpdate;
  private final Object naturalKey;
  private final Object oldNaturalKey;

  public CachedBeanData(Object sharableBean, boolean[] loaded, Object[] data, Object naturalKey, Object oldNaturalKey) {
    this.whenCreated = System.currentTimeMillis();
    this.sharableBean = sharableBean;
    this.loaded = loaded;
    this.data = data;
    this.naturalKeyUpdate = naturalKey != null;
    this.naturalKey = (naturalKey != null) ? naturalKey : oldNaturalKey;
    this.oldNaturalKey = oldNaturalKey;
  }

  public String toString() {
    return Arrays.toString(data);
  }
  
  /**
   * Return a copy of the property data.
   */
  public Object[] copyData() {
    Object[] dest = new Object[data.length];
    System.arraycopy(data, 0, dest, 0, data.length);
    return dest;
  }

  /**
   * Return a copy of the loaded status for the properties.
   */
  public boolean[] copyLoaded() {
    boolean[] dest = new boolean[data.length];
    for (int i = 0; i < dest.length; i++) {
      dest[i] = loaded[i];
    }
    return dest;
  }

  /**
   * Return when the cached data was created.
   */
  public long getWhenCreated() {
    return whenCreated;
  }

  /**
   * Return a sharable (immutable read only) bean.
   */
  public Object getSharableBean() {
    return sharableBean;
  }

  /**
   * Return true if this data requires an update to the natural key cache.
   */
  public boolean isNaturalKeyUpdate() {
    return naturalKeyUpdate;
  }

  /**
   * Return the new/current natural key value.
   */
  public Object getNaturalKey() {
    return naturalKey;
  }
  
  /**
   * Return the old natural key (its entry should be removed).
   */
  public Object getOldNaturalKey() {
    return oldNaturalKey;
  }

  /**
   * Return the data for the specific property.
   */
  public Object getData(int i) {
    return data[i];
  }

  /**
   * Return true if the property is contained in this data.
   */
  public boolean isLoaded(int i) {
    return loaded[i];
  }

}

package com.avaje.ebeaninternal.server.cache;

/**
 * Data held in the bean cache for cached beans.
 */
public class CachedBeanData {

  private final Object sharableBean;
  private final boolean[] loaded;
  private final Object[] data;
  
  private final boolean naturalKeyUpdate;
  private final Object naturalKey;
  private final Object oldNaturalKey;

  public CachedBeanData(Object sharableBean, boolean[] loaded, Object[] data, Object naturalKey, Object oldNaturalKey) {
    this.sharableBean = sharableBean;
    this.loaded = loaded;
    this.data = data;
    this.naturalKeyUpdate = naturalKey != null;
    this.naturalKey = (naturalKey != null) ? naturalKey : oldNaturalKey;
    this.oldNaturalKey = oldNaturalKey;
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
  
  public Object getSharableBean() {
    return sharableBean;
  }

  public boolean isNaturalKeyUpdate() {
    return naturalKeyUpdate;
  }

  public Object getNaturalKey() {
    return naturalKey;
  }
  
  public Object getOldNaturalKey() {
    return oldNaturalKey;
  }

  public boolean containsProperty(int propIndex) {
    return loaded[propIndex];
  }

  public boolean[] getLoaded() {
    return loaded;
  }

  public Object getData(int i) {
    return data[i];
  }

  public boolean isLoaded(int i) {
    return loaded[i];
  }

}

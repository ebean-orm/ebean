package com.avaje.ebeaninternal.server.cache;

public class CachedBeanData {

  private final Object sharableBean;
  private final boolean[] loaded;
  private final Object[] data;
  private final int naturalKeyUpdate;

  public CachedBeanData(Object sharableBean, boolean[] loaded, Object[] data, int naturalKeyUpdate) {
    this.sharableBean = sharableBean;
    this.loaded = loaded;
    this.data = data;
    this.naturalKeyUpdate = naturalKeyUpdate;
  }

  public Object getSharableBean() {
    return sharableBean;
  }

  public boolean isNaturalKeyUpdate() {
    return naturalKeyUpdate > -1;
  }

  public Object getNaturalKey() {
    return data[naturalKeyUpdate];
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

  public Object[] copyData() {
    Object[] dest = new Object[data.length];
    System.arraycopy(data, 0, dest, 0, data.length);
    return dest;
  }

  public boolean isLoaded(int i) {
    return loaded[i];
  }

}

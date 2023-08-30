package io.ebeaninternal.server.cache;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data held in the bean cache for cached beans.
 */
public final class CachedBeanData implements Externalizable {

  private long whenCreated;
  private long version;
  private Map<String, Object> data;
  /**
   * The sharable bean is effectively transient (near cache only).
   */
  private transient Object sharableBean;

  /**
   * Construct from a loaded bean.
   */
  public CachedBeanData(Object sharableBean, Map<String, Object> data, long version) {
    this.whenCreated = System.currentTimeMillis();
    this.sharableBean = sharableBean;
    this.data = data;
    this.version = version;
  }

  /**
   * Construct from serialisation.
   */
  public CachedBeanData() {
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeLong(version);
    out.writeLong(whenCreated);
    out.writeInt(data.size());
    for (Map.Entry<String, Object> entry : data.entrySet()) {
      out.writeUTF(entry.getKey());
      out.writeObject(entry.getValue());
    }
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    version = in.readLong();
    whenCreated = in.readLong();
    data = new LinkedHashMap<>();
    int count = in.readInt();
    for (int i = 0; i < count; i++) {
      String key = in.readUTF();
      Object val = in.readObject();
      data.put(key, val);
    }
  }

  @Override
  public String toString() {
    return data.toString();
  }

  /**
   * Create and return a new version of CachedBeanData based on this
   * entry applying the given changes.
   */
  public CachedBeanData update(Map<String, Object> changes, long version) {

    Map<String, Object> copy = new HashMap<>();
    copy.putAll(data);
    copy.putAll(changes);
    return new CachedBeanData(null, copy, version);
  }

  /**
   * Return when the cached data was created.
   */
  public long getWhenCreated() {
    return whenCreated;
  }

  /**
   * Return the version value.
   */
  public long getVersion() {
    return version;
  }

  /**
   * Return a sharable (immutable read only) bean. Near cache only use.
   */
  public Object getSharableBean() {
    return sharableBean;
  }

  /**
   * Return true if the property is held.
   */
  public boolean isLoaded(String propertyName) {
    return data.containsKey(propertyName);
  }

  /**
   * Return the value for a given property name.
   */
  public Object getData(String propertyName) {
    return data.get(propertyName);
  }

  /**
   * Return all the property data.
   */
  public Map<String, Object> getData() {
    return data;
  }
}

package io.ebeaninternal.server.cache;

import io.ebeaninternal.api.BinaryReadContext;
import io.ebeaninternal.api.BinaryWritable;
import io.ebeaninternal.api.BinaryWriteContext;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Cache events broadcast across the cluster.
 */
public class RemoteCacheEvent implements BinaryWritable {

  private boolean clearAll;

  private List<String> clearCaches;

  /**
   * Clear all the caches.
   */
  public RemoteCacheEvent(boolean clearAll) {
    this.clearAll = clearAll;
    this.clearCaches = null;
  }

  /**
   * Clear caches for the given bean type.
   */
  public RemoteCacheEvent(Class<?> beanType) {
    this.clearAll = false;
    this.clearCaches = new ArrayList<>(1);
    this.clearCaches.add(beanType.getName());
  }

  /**
   * Create with the read options.
   */
  public RemoteCacheEvent(boolean clearAll, List<String> beanTypes) {
    this.clearAll = clearAll;
    this.clearCaches = beanTypes;
  }

  public boolean isClearAll() {
    return clearAll;
  }

  public List<String> getClearCaches() {
    return clearCaches;
  }

  @Override
  public String toString() {
    return "CacheEvent[ clearAll:" + clearAll + " caches:" + clearCaches + "]";
  }

  public static RemoteCacheEvent readBinaryMessage(BinaryReadContext dataInput) throws IOException {

    boolean clearAll = dataInput.readBoolean();
    int size = dataInput.readInt();

    List<String> clearCache = null;
    if (size > 0) {
      clearCache = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        clearCache.add(dataInput.readUTF());
      }
    }

    return new RemoteCacheEvent(clearAll, clearCache);
  }

  @Override
  public void writeBinary(BinaryWriteContext out) throws IOException {
    DataOutputStream os = out.start(TYPE_CACHE);
    os.writeBoolean(clearAll);
    if (clearCaches == null) {
      os.writeInt(0);
    } else {
      os.writeInt(clearCaches.size());
      for (String cacheName : clearCaches) {
        os.writeUTF(cacheName);
      }
    }
  }
}

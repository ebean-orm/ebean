package io.ebean.cache;

import java.util.Set;

/**
 * Notification event that dependent tables have been modified.
 * <p>
 * This is sent to other interested servers (in the cluster).
 * </p>
 */
public class ServerCacheNotification {

  private final long modifyTimestamp;

  private final Set<String> dependentTables;

  public ServerCacheNotification(long modifyTimestamp, Set<String> dependentTables) {
    this.modifyTimestamp = modifyTimestamp;
    this.dependentTables = dependentTables;
  }

  public long getModifyTimestamp() {
    return modifyTimestamp;
  }

  public Set<String> getDependentTables() {
    return dependentTables;
  }
}

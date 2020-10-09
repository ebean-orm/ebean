package io.ebean.cache;

import java.util.Set;

/**
 * Notification event that dependent tables have been modified.
 * <p>
 * This is sent to other interested servers (in the cluster).
 * </p>
 */
public class ServerCacheNotification {

  private final Set<String> dependentTables;

  public ServerCacheNotification(Set<String> dependentTables) {
    this.dependentTables = dependentTables;
  }

  @Override
  public String toString() {
    return "tables:" + dependentTables;
  }

  public Set<String> getDependentTables() {
    return dependentTables;
  }
}

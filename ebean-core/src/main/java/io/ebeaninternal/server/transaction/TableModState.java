package io.ebeaninternal.server.transaction;

import io.avaje.applog.AppLog;
import io.ebean.cache.QueryCacheEntry;
import io.ebean.cache.QueryCacheEntryValidate;
import io.ebean.cache.ServerCacheNotification;
import io.ebean.cache.ServerCacheNotify;

import java.lang.System.Logger.Level;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds timestamp of last modification per table.
 * <p>
 * This information is used to validate entries in the L2 query caches.
 * </p>
 */
public final class TableModState implements QueryCacheEntryValidate, ServerCacheNotify {

  private static final System.Logger log = AppLog.getLogger("io.ebean.cache.TABLEMOD");

  private final Map<String, Long> tableModStamp = new ConcurrentHashMap<>();

  public TableModState() {
  }

  /**
   * Set the modified timestamp on the tables that have been touched.
   */
  void touch(Set<String> touchedTables) {
    long modNanoTime = System.nanoTime();
    for (String tableName : touchedTables) {
      tableModStamp.put(tableName, modNanoTime);
    }
    if (log.isLoggable(Level.DEBUG)) {
      log.log(Level.DEBUG, "TableModState updated - touched:{0} modNanoTime:{1}", touchedTables, modNanoTime);
    }
  }

  /**
   * Return true if all the tables are valid based on timestamp comparison.
   */
  boolean isValid(Set<String> tables, long sinceNanoTime) {
    for (String tableName : tables) {
      Long modTime = tableModStamp.get(tableName);
      if (modTime != null && modTime >= sinceNanoTime) {
        if (log.isLoggable(Level.TRACE)) {
          log.log(Level.TRACE, "Invalidate on table:{0}", tableName);
        }
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean isValid(QueryCacheEntry entry) {
    Set<String> dependentTables = entry.getDependentTables();
    if (dependentTables != null && !dependentTables.isEmpty()) {
      return isValid(dependentTables, entry.getTimestamp());
    }
    return true;
  }

  /**
   * Update the table modification timestamps based on remote table modification events.
   * <p>
   * Generally this is used with distributed caches (Hazelcast, Ignite etc) via topic.
   */
  @Override
  public void notify(ServerCacheNotification notification) {
    // use local clock - for slightly more aggressive invalidation (as later)
    // that removes any concern regarding clock syncing across cluster
    if (log.isLoggable(Level.DEBUG)) {
      log.log(Level.DEBUG, "ServerCacheNotification:{0}", notification);
    }
    touch(notification.getDependentTables());
  }

  /**
   * Update from Remote transaction event.
   * <p>
   * Generally this is used with Clustering (ebean-cluster, k8scache).
   */
  public void notify(RemoteTableMod tableMod) {
    // use local clock - for slightly more aggressive invalidation (as later)
    // that removes any concern regarding clock syncing across cluster
    if (log.isLoggable(Level.DEBUG)) {
      log.log(Level.DEBUG, "RemoteTableMod:{0}", tableMod);
    }
    touch(tableMod.getTables());
  }
}

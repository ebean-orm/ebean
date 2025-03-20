package io.ebeaninternal.server.transaction;

import io.avaje.applog.AppLog;
import io.ebean.cache.QueryCacheEntry;
import io.ebean.cache.QueryCacheEntryValidate;
import io.ebean.cache.ServerCacheNotification;
import io.ebean.cache.ServerCacheNotify;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;

/**
 * Holds timestamp of last modification per table.
 * <p>
 * This information is used to validate entries in the L2 query caches.
 * </p>
 */
public final class TableModState implements QueryCacheEntryValidate, ServerCacheNotify {

  private static final System.Logger log = AppLog.getLogger("io.ebean.cache.TABLEMOD");

  private final Map<String, Instant> tableModStamp = new ConcurrentHashMap<>();

  public TableModState() {
  }

  /**
   * Set the modified timestamp on the tables that have been touched.
   */
  void touch(Set<String> touchedTables) {
    final var modTime = Instant.now();
    for (String tableName : touchedTables) {
      tableModStamp.put(tableName, modTime);
    }
    if (log.isLoggable(DEBUG)) {
      log.log(DEBUG, "TableModState updated - touched:{0} modTime:{1}", touchedTables, modTime);
    }
  }

  /**
   * Return true if all the tables are valid based on timestamp comparison.
   */
  boolean isValid(Set<String> tables, Instant sinceTime) {
    for (String tableName : tables) {
      final var modTime = tableModStamp.get(tableName);
      if (modTime != null && !modTime.isBefore(sinceTime)) {
        if (log.isLoggable(TRACE)) {
          log.log(TRACE, "Invalidate on table:{0}", tableName);
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
    if (log.isLoggable(DEBUG)) {
      log.log(DEBUG, "ServerCacheNotification:{0}", notification);
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
    if (log.isLoggable(DEBUG)) {
      log.log(DEBUG, "RemoteTableMod:{0}", tableMod);
    }
    touch(tableMod.getTables());
  }
}

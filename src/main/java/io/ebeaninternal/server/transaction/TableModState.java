package io.ebeaninternal.server.transaction;

import io.ebean.cache.QueryCacheEntry;
import io.ebean.cache.QueryCacheEntryValidate;
import io.ebean.cache.ServerCacheNotification;
import io.ebean.cache.ServerCacheNotify;
import io.ebeaninternal.server.core.ClockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds timestamp of last modification per table.
 * <p>
 * This information is used to validate entries in the L2 query caches.
 * </p>
 */
public class TableModState implements QueryCacheEntryValidate, ServerCacheNotify {

  private static final Logger log = LoggerFactory.getLogger("io.ebean.cache.TABLEMOD");

  private final ClockService clockService;

  private Map<String,Long> tableModStamp = new ConcurrentHashMap<>();

  public TableModState(ClockService clockService) {
    this.clockService = clockService;
  }

  /**
   * Set the modified timestamp on the tables that have been touched.
   */
  void touch(Set<String> touchedTables, long modTimestamp) {
    for (String tableName : touchedTables) {
      tableModStamp.put(tableName, modTimestamp);
    }
    if (log.isDebugEnabled()) {
      log.debug("TableModState updated - touched:{} modTimestamp:{}", touchedTables, modTimestamp);
    }
  }

  /**
   * Return true if all the tables are valid based on timestamp comparison.
   */
  boolean isValid(Set<String> tables, long sinceTimestamp) {
    for (String tableName : tables) {
      Long modTime = tableModStamp.get(tableName);
      if (modTime != null && modTime >= sinceTimestamp ) {
        if (log.isTraceEnabled()) {
          log.trace("Invalidate on table:{}", tableName);
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
   * </p>
   */
  @Override
  public void notify(ServerCacheNotification notification) {

    // use local clock - for slightly more aggressive invalidation (as later)
    // that removes any concern regarding clock syncing across cluster
    if (log.isDebugEnabled()) {
      log.debug("ServerCacheNotification:{}", notification);
    }
    touch(notification.getDependentTables(), clockService.nowMillis());
  }

  /**
   * Update from Remote transaction event.
   * <p>
   * Generally this is used with Clustering (ebean-cluster, k8scache).
   * </p>
   */
  public void notify(RemoteTableMod tableMod) {

    // use local clock - for slightly more aggressive invalidation (as later)
    // that removes any concern regarding clock syncing across cluster
    if (log.isDebugEnabled()) {
      log.debug("RemoteTableMod:{}", tableMod);
    }
    touch(tableMod.getTables(), clockService.nowMillis());
  }
}

package io.ebeaninternal.server.transaction;

import io.ebean.cache.QueryCacheEntry;
import io.ebean.cache.QueryCacheEntryValidate;
import io.ebean.cache.ServerCacheNotification;
import io.ebean.cache.ServerCacheNotify;
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

  private Map<String,Long> tableModStamp = new ConcurrentHashMap<>();

  /**
   * Set the modified timestamp on the tables that have been touched.
   */
  void touch(Set<String> touchedTables, long modTimestamp) {
    for (String tableName : touchedTables) {
      tableModStamp.put(tableName, modTimestamp);
    }
    if (log.isDebugEnabled()) {
      log.debug("TableModState updated - " + tableModStamp);
    }
  }

  /**
   * Return true if all the tables are valid based on timestamp comparison.
   */
  boolean isValid(Set<String> tables, long sinceTimestamp) {
    for (String tableName : tables) {
      Long modTime = tableModStamp.get(tableName);
      if (modTime != null && modTime > sinceTimestamp ) {
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
   */
  @Override
  public void notify(ServerCacheNotification notification) {

    // TODO: Change to use ClockService ...
    long modifyTimestamp = notification.getModifyTimestamp();
    touch(notification.getDependentTables(), modifyTimestamp);
  }
}

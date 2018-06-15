package io.ebeaninternal.server.transaction;

import io.ebean.cache.QueryCacheEntry;
import io.ebean.cache.QueryCacheEntryValidate;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds timestamp of last modification per table.
 * <p>
 * This information is used to validate entries in the L2 query caches.
 * </p>
 */
public class TableModState implements QueryCacheEntryValidate {

  private Map<String,Long> tableModStamp = new ConcurrentHashMap<>();

  /**
   * Set the modified timestamp on the tables that have been touched.
   */
  public void touch(Set<String> touchedTables, long modTimestamp) {

    for (String tableName : touchedTables) {
      tableModStamp.put(tableName, modTimestamp);
    }
  }

  /**
   * Return true if all the tables are valid based on timestamp comparison.
   */
  public boolean isValid(Set<String> tables, long sinceTimestamp) {
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
}

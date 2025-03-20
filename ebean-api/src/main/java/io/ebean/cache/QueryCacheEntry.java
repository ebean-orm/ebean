package io.ebean.cache;

import java.time.Instant;
import java.util.Set;

/**
 * For query cache entries we additionally hold the dependent tables and timestamp for the query result.
 * <p>
 * We use the dependent tables and timestamp to validate that tables the query joins to have not been
 * modified since the query cache entry was cached. If any dependent tables have since been modified
 * the query cache entry is treated as invalid.
 * </p>
 */
public final class QueryCacheEntry {

  private final Object value;
  private final Set<String> dependentTables;
  private final Instant timestamp;

  /**
   * Create with dependent tables and timestamp.
   *
   * @param value           The query result being cached
   * @param dependentTables The extra tables the query is dependent on (joins to)
   * @param timestamp       The timestamp that the query uses to check for modifications
   */
  public QueryCacheEntry(Object value, Set<String> dependentTables, Instant timestamp) {
    this.value = value;
    this.dependentTables = dependentTables;
    this.timestamp = timestamp;
  }

  /**
   * Return the actual query result.
   */
  public Object value() {
    return value;
  }

  /**
   * Return the tables the query result is dependent on.
   */
  public Set<String> dependentTables() {
    return dependentTables;
  }

  /**
   * Return the timestamp used to check for modifications on the dependent tables.
   */
  public Instant timestamp() {
    return timestamp;
  }
}

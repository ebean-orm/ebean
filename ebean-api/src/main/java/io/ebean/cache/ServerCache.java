package io.ebean.cache;

import io.ebean.meta.MetricVisitor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents part of the "L2" server side cache.
 * <p>
 * This is used to cache beans or query results (bean collections).
 * <p>
 * There are 2 ServerCache's for each bean type. One is used as the 'bean cache'
 * which holds beans of a given type. The other is the 'query cache' holding
 * query results for a given type.
 */
public interface ServerCache {

  /**
   * Get values for many keys.
   */
  default Map<Object, Object> getAll(Set<Object> keys) {
    Map<Object, Object> map = new LinkedHashMap<>();
    for (Object key : keys) {
      Object value = get(key);
      if (value != null) {
        map.put(key, value);
      }
    }
    return map;
  }

  /**
   * Return the value given the key.
   */
  Object get(Object id);

  /**
   * Put all the values in the cache.
   */
  default void putAll(Map<Object, Object> keyValues) {
    keyValues.forEach(this::put);
  }

  /**
   * Put the value in the cache with a given id.
   */
  void put(Object id, Object value);

  /**
   * Remove the entries from the cache given the id values.
   */
  default void removeAll(Set<Object> keys) {
    keys.forEach(this::remove);
  }

  /**
   * Remove a entry from the cache given its id.
   */
  void remove(Object id);

  /**
   * Clear all entries from the cache.
   */
  void clear();

  /**
   * Return the number of entries in the cache.
   */
  int size();

  /**
   * Return the hit ratio the cache is currently getting.
   */
  default int hitRatio() {
    return getHitRatio();
  }

  /**
   * Deprecated migrate to hitRatio().
   */
  @Deprecated
  int getHitRatio();

  /**
   * Return statistics for the cache.
   *
   * @param reset if true the statistics are reset.
   */
  default ServerCacheStatistics statistics(boolean reset) {
    return getStatistics(reset);
  }

  /**
   * Deprecated migrate to statistics().
   */
  @Deprecated
  ServerCacheStatistics getStatistics(boolean reset);

  /**
   * Visit the metrics for the cache.
   */
  default void visit(MetricVisitor visitor) {
    // do nothing by default
  }
}

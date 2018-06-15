package io.ebeaninternal.server.cache;

import io.ebean.cache.QueryCacheEntry;
import io.ebean.cache.QueryCacheEntryValidate;

/**
 * Server cache for query caching.
 * <p>
 * Entries in this cache contain QueryCacheEntry and we need to additionally
 * validate the entries when hit for changes to dependent tables.
 * </p>
 */
public class DefaultServerQueryCache extends DefaultServerCache {

  private final QueryCacheEntryValidate queryCacheEntryValidate;

  public DefaultServerQueryCache(DefaultServerCacheConfig config) {
    super(config);
    this.queryCacheEntryValidate = config.getQueryCacheEntryValidate();
  }

  protected Object unwrapEntry(CacheEntry entry) {
    return ((QueryCacheEntry) entry.getValue()).getValue();
  }

  @Override
  protected CacheEntry getCacheEntry(Object id) {
    Object key = key(id);
    CacheEntry entry = map.get(key);
    if (entry == null) {
      return null;
    }
    QueryCacheEntry value = (QueryCacheEntry) entry.getValue();
    if (!queryCacheEntryValidate.isValid(value)) {
      map.remove(key);
      removeCount.increment();
      return null;
    }
    return entry;
  }
}

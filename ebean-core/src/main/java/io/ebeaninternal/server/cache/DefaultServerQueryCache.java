package io.ebeaninternal.server.cache;

import io.ebean.cache.QueryCacheEntry;
import io.ebean.cache.QueryCacheEntryValidate;

import java.lang.ref.SoftReference;

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

  @Override
  protected Object unwrapEntry(CacheEntry entry) {
    return ((QueryCacheEntry) entry.getValue()).value();
  }

  @Override
  protected CacheEntry getCacheEntry(Object key) {
    final SoftReference<CacheEntry> ref = map.get(key);
    CacheEntry entry = ref != null ? ref.get() : null;
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

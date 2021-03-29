package io.ebeaninternal.api;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Used for bean cache lookup with a single id value.
 */
public class CacheIdLookupSingle<T> implements CacheIdLookup<T> {

  private final Object idValue;
  private boolean found;

  public CacheIdLookupSingle(Object idValue) {
    this.idValue = idValue;
  }

  @Override
  public Collection<?> idValues() {
    return Collections.singleton(idValue);
  }

  @Override
  public List<T> removeHits(BeanCacheResult<T> cacheResult) {
    final List<BeanCacheResult.Entry<T>> hits = cacheResult.hits();
    if (hits.size() == 1) {
      found = true;
      return Collections.singletonList(hits.get(0).getBean());
    }
    return Collections.emptyList();
  }

  @Override
  public boolean allHits() {
    return found;
  }
}

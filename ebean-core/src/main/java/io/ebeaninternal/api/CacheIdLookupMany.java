package io.ebeaninternal.api;

import io.ebeaninternal.server.expression.IdInExpression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Used for bean cache lookup with where ids in expression.
 */
public class CacheIdLookupMany<T> implements CacheIdLookup<T> {

  private final IdInExpression idInExpression;

  private int remaining;

  public CacheIdLookupMany(IdInExpression idInExpression) {
    this.idInExpression = idInExpression;
  }

  /**
   * Return the Id values for the in expression.
   */
  @Override
  public Collection<?> idValues() {
    return idInExpression.idValues();
  }

  /**
   * Process the hits returning the beans fetched from cache and
   * adjusting the in expression (to not fetch the hits).
   */
  @Override
  public List<T> removeHits(BeanCacheResult<T> cacheResult) {

    Set<Object> hitIds = new HashSet<>();
    List<T> beans = new ArrayList<>(hitIds.size());

    for (BeanCacheResult.Entry<T> hit : cacheResult.hits()) {
      hitIds.add(hit.getKey());
      beans.add(hit.getBean());
    }

    this.remaining = idInExpression.removeIds(hitIds);
    return beans;
  }

  @Override
  public boolean allHits() {
    return remaining == 0;
  }
}

package io.ebeaninternal.api.filter;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Provides a context object for filtering a list.
 *
 * This is mainly to handle permutations.
 *
 */
public interface FilterContext {

  /**
   * Computes a cache value for this run. This may be used to cache values for a subquery.
   */
  <C> C computeIfAbsent(Object key, Supplier<C> supplier);

  /**
   * Entry point for traversing filter permutations.
   */
  FilterPermutations getFilterPermutations(String propName, Collection<?> src);

}

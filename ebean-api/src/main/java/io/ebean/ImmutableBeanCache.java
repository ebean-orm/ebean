package io.ebean;

import org.jspecify.annotations.NullMarked;
import java.util.Map;
import java.util.Set;

/**
 * Query-scoped immutable bean cache.
 *
 * @param <T> The bean type.
 */
@NullMarked
public interface ImmutableBeanCache<T> {

  /**
   * Return the bean type this cache provides values for.
   */
  Class<T> type();

  /**
   * Return immutable cached beans by id (loading and populating misses as needed).
   */
  Map<Object, T> getAll(Set<Object> ids);
}

package io.ebean;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
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
   * Return a cached immutable bean by id or null.
   */
  @Nullable
  T get(Object id);

  /**
   * Return immutable cached beans by id (loading and populating misses as needed).
   */
  default Map<Object, T> getAll(Set<Object> ids) {
    if (ids.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<Object, T> map = new LinkedHashMap<>();
    for (Object id : ids) {
      T bean = get(id);
      if (bean != null) {
        map.put(id, bean);
      }
    }
    return map;
  }
}

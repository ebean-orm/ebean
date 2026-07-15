package io.ebean;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Identity-keyed cache of already-mapped source -&gt; target instances, shared across one
 * top-level {@link DtoMapper#mapList(java.util.List)} call (or an explicitly shared context).
 * <p>
 * Keyed by source object <b>identity</b> (an {@link IdentityHashMap}, not {@code equals()}/
 * {@code hashCode()}) because the source is an Ebean entity graph, where repeated references to
 * the same row within one query already resolve to the same Java object instance.
 * <p>
 * The identity map is partitioned <b>per target DTO type</b>. This matters because the same
 * source instance can legitimately need to be mapped to more than one target type within a
 * single graph - e.g. a top-level {@code CustomerDtoMapper} maps a {@code Customer} to a full
 * {@code CustomerDto}, while a nested {@code ContactDtoMapper} maps the very same {@code Customer}
 * instance (accessed via {@code contact.getCustomer()}) to a shallow {@code CustomerRefDto} to
 * avoid a cycle. A single un-partitioned {@code IdentityHashMap<Object,Object>} would have the
 * two mappers collide on the same source key and incorrectly hand back the other mapper's
 * (wrong-typed) cached result. Partitioning by target type keeps each mapper's cache isolated
 * while still sharing one context/instance per top-level mapping call.
 * <p>
 * Not thread-safe - a context is expected to be created per top-level mapping call and not
 * shared across threads.
 */
public final class DtoMapContext {

  private final Map<Class<?>, Map<Object, Object>> mappedByType = new HashMap<>();

  /**
   * Return the already-mapped target for the given source instance if present, otherwise map it
   * via {@code mappingFunction}, register it, and return it.
   *
   * @param targetType the DTO type being produced - used to partition the identity cache so that
   *     mapping the same source to different target types never collides.
   */
  @SuppressWarnings("unchecked")
  public <S, T> T computeIfAbsent(Class<T> targetType, S source, Function<S, T> mappingFunction) {
    Map<Object, Object> mapped = mappedByType.computeIfAbsent(targetType, t -> new IdentityHashMap<>());
    T existing = (T) mapped.get(source);
    if (existing != null) {
      return existing;
    }
    T created = mappingFunction.apply(source);
    mapped.put(source, created);
    return created;
  }
}

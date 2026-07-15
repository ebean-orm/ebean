package io.ebean;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper interface implemented by generated (or hand-written) entity -&gt; DTO graph mappers.
 * <p>
 * Used with nested entity-to-DTO graph mapping (see {@code query.mapTo(SomeDto.class)}) as
 * distinct from the existing flat, single-row {@link DtoQuery} pipeline. Each entity/DTO type
 * pair gets its own small, composable mapper implementation (mirroring MapStruct's per-type
 * mapper generation) rather than one large mapper inlining every nested type. Nested mappers are
 * wired together via constructor injection, not static singletons - this keeps mappers stateless,
 * substitutable (e.g. for tests) and avoids global mutable state.
 * <p>
 * A {@link DtoMapContext} is threaded through every nested {@code map(...)} call within one
 * top-level {@link #mapList(List)} invocation, so that repeated references to the same source
 * entity instance (e.g. several {@code Contact}s sharing the same {@code Customer}) map to the
 * <b>same</b> target DTO instance rather than creating duplicate-but-equal copies. This mirrors
 * the identity semantics Ebean's own entity graph already has, and is what makes the resulting
 * DTO graph "graph shaped" rather than "tree of copies shaped".
 * <p>
 * Implementations contain no reflection or {@code MethodHandles} - only direct getter calls and
 * constructor invocation - so generated mappers are safe under GraalVM native-image with zero
 * additional reachability metadata.
 *
 * @param <SOURCE> the source entity (or embeddable) type
 * @param <TARGET> the target DTO type
 */
public interface DtoMapper<SOURCE, TARGET> {

  /**
   * Return the {@link FetchGroup} of exactly the source properties (and nested paths) needed to
   * populate the target DTO graph - the select()/fetch() spec is derived from the DTO's declared
   * shape rather than maintained separately by hand. Used by {@code query.mapTo(TARGET.class)}
   * to automatically apply the correct fetch spec before the query is executed.
   */
  FetchGroup<SOURCE> fetchGroup();

  /**
   * Map a single source instance to its target DTO, reusing/registering the mapping in the
   * given context so that repeated references to the same source instance de-duplicate to the
   * same target instance. Must return {@code null} when given {@code null}.
   */
  TARGET map(SOURCE source, DtoMapContext context);

  /**
   * Map a single source instance using a fresh, one-off context. Convenience for mapping a
   * single object in isolation (no de-duplication opportunity since there's nothing else in
   * scope to de-duplicate against).
   */
  default TARGET map(SOURCE source) {
    return map(source, new DtoMapContext());
  }

  /**
   * Map a list of source instances to a list of target DTOs sharing the given context,
   * preserving order.
   */
  default List<TARGET> mapList(List<SOURCE> source, DtoMapContext context) {
    List<TARGET> result = new ArrayList<>(source.size());
    for (SOURCE s : source) {
      result.add(map(s, context));
    }
    return result;
  }

  /**
   * Map a list of source instances to a list of target DTOs using a fresh context shared across
   * the whole list - this is the usual top-level entry point, e.g. mapping the result of a
   * {@code query.findList()} call.
   */
  default List<TARGET> mapList(List<SOURCE> source) {
    return mapList(source, new DtoMapContext());
  }
}

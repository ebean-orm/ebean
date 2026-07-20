package io.ebean;

import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Query that maps an entity graph query result to a nested DTO graph, produced by
 * {@code query.mapTo(SomeDto.class)}.
 * <p>
 * Distinct from the existing flat, single-row {@link DtoQuery} pipeline (see {@link
 * QueryBuilder#asDto(Class)}) - this executes the underlying entity ORM query (with the
 * select()/fetch() spec automatically derived from the target DTO's declared shape, see
 * {@link DtoMapper#fetchGroup()}), forces {@code setUnmodifiable(true)}, and then maps the
 * resulting (unmodifiable) entity graph into a DTO graph via the generated {@link DtoMapper},
 * supporting nested ToOne/ToMany and identity-aware de-duplication.
 *
 * @param <D> the target DTO type
 */
@NullMarked
public interface MappedQuery<D> extends StreamableQuery<MappedQuery<D>, D> {

  /**
   * Execute the query returning the mapped DTO list.
   */
  @Override
  List<D> findList();

  /**
   * Execute the query returning a paged list of mapped DTOs.
   * <p>
   * Mirrors {@code Query#findPagedList()} - the underlying entity graph query is paged (via
   * {@code setFirstRow(int)}/{@code setMaxRows(int)}) and executed as normal, then each page's
   * result is mapped to the target DTO graph. Row-count/page-index metadata
   * ({@link PagedList#getTotalCount()}, {@link PagedList#hasNext()}, etc.) reflects the
   * underlying entity query and is unaffected by the DTO mapping.
   */
  @Override
  PagedList<D> findPagedList();

  /**
   * Execute the query returning the result as a Stream of mapped DTOs.
   * <p>
   * Mirrors {@link QueryBuilder#findStream()} - the underlying entity graph query is streamed
   * (supporting very large queries iterating any number of results, potentially using multiple
   * persistence contexts internally) and each entity is mapped to its target DTO lazily as the
   * stream is consumed, sharing one {@link DtoMapContext} across the whole stream so that
   * repeated references to the same source entity still de-duplicate to the same DTO instance.
   * <pre>{@code
   *
   *  // use try with resources to ensure Stream is closed
   *
   *  try (Stream<CustomerDto> stream = query.mapTo(CustomerDto.class).findStream()) {
   *    stream
   *    .map(...)
   *    .collect(...);
   *  }
   *
   * }</pre>
   */
  @Override
  Stream<D> findStream();

  /**
   * Execute the query processing the mapped DTOs one at a time.
   * <p>
   * Mirrors {@link QueryBuilder#findEach(Consumer)} - the underlying entity graph query is
   * streamed one entity at a time and each entity is mapped to its target DTO lazily as it is
   * consumed, sharing one {@link DtoMapContext} across the whole callback so that repeated
   * references to the same source entity still de-duplicate to the same DTO instance.
   * <p>
   * This method is appropriate to process very large query results as the mapped DTOs are
   * consumed one at a time and do not need to be held in memory (unlike {@link #findList()}).
   *
   * @param consumer the consumer used to process the mapped DTOs.
   */
  @Override
  void findEach(Consumer<D> consumer);
  /**
   * Execute findEach streaming query batching the mapped DTOs for consuming.
   * <p>
   * Mirrors {@link QueryBuilder#findEach(int, Consumer)} - typically used when we want to do
   * further processing on the mapped DTOs in batch form, for example 100 at a time. Each batch
   * shares one {@link DtoMapContext} with the rest of the query so that repeated references to
   * the same source entity still de-duplicate to the same DTO instance.
   *
   * @param batch    The number of mapped DTOs processed in the batch
   * @param consumer Process the batch of mapped DTOs
   */
  @Override
  void findEach(int batch, Consumer<List<D>> consumer);

  /**
   * Execute the query using callbacks to process the resulting mapped DTOs one at a time,
   * with the ability to stop processing part way through.
   * <p>
   * Mirrors {@link QueryBuilder#findEachWhile(Predicate)} - returning {@code false} after
   * processing a DTO stops the iteration through the query results. Sharing one
   * {@link DtoMapContext} across the whole callback so that repeated references to the same
   * source entity still de-duplicate to the same DTO instance.
   *
   * @param consumer the consumer used to process the mapped DTOs, returning {@code false} to
   *                 stop processing.
   */
  @Override
  void findEachWhile(Predicate<D> consumer);

}

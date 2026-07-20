package io.ebean;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import jakarta.persistence.EntityNotFoundException;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
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
public interface MappedQuery<D> extends CancelableQuery {

  /**
   * Execute the query returning the mapped DTO list.
   */
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
  Stream<D> findStream();

  /**
   * Execute the query returning a single mapped DTO, or {@code null} if there is no matching row.
   */
  @Nullable
  D findOne();

  /**
   * Execute the query returning an optional mapped DTO.
   */
  Optional<D> findOneOrEmpty();

  /**
   * Execute the query returning a single mapped DTO or throwing a
   * {@link jakarta.persistence.EntityNotFoundException} if there is no matching row.
   * <p>
   * The exception message reflects the underlying entity type and its id or single
   * equality predicate (a likely natural/unique key) when the query is that simple,
   * otherwise a generic "not found" message.
   */
  default D findOneOrThrow() {
    return findOneOrEmpty().orElseThrow(() -> new EntityNotFoundException("Not found"));
  }

  /**
   * Execute the query returning a single mapped DTO or throwing the exception produced
   * by the given supplier if there is no matching row.
   */
  default D findOneOrThrow(Supplier<? extends RuntimeException> exceptionSupplier) {
    return findOneOrEmpty().orElseThrow(exceptionSupplier);
  }

  /**
   * Ensure the master DataSource is used when useMaster is true. Otherwise, the read only
   * data source can be used if defined.
   */
  MappedQuery<D> usingMaster(boolean useMaster);

  /**
   * Use the explicit transaction to execute the query.
   */
  MappedQuery<D> usingTransaction(Transaction transaction);

  /**
   * Execute the query using the given connection.
   */
  MappedQuery<D> usingConnection(Connection connection);

}

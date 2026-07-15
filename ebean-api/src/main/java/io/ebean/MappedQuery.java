package io.ebean;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

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
public interface MappedQuery<D> {

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
   * Execute the query returning a single mapped DTO, or {@code null} if there is no matching row.
   */
  @Nullable
  D findOne();

  /**
   * Execute the query returning an optional mapped DTO.
   */
  Optional<D> findOneOrEmpty();
}

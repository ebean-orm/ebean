package io.ebean;

import org.jspecify.annotations.NullMarked;

import java.util.stream.Stream;

/**
 * A {@link FindableQuery} that additionally supports streaming results and paging.
 *
 * @param <SELF> The query type (used for method chaining)
 * @param <T>    The type of the result
 */
@NullMarked
public interface StreamableQuery<SELF extends StreamableQuery<SELF, T>, T> extends FindableQuery<SELF, T> {

  /**
   * Execute the query returning the result as a Stream.
   * <p>
   * Note that this can support very large queries iterating any number of results.
   * To do so internally it can use multiple persistence contexts.
   * <p>
   * Note that the Stream holds resources related to the underlying resultSet and
   * potentially connection and MUST be closed. We should use the Stream in a
   * <em>try with resource block</em>.
   * <pre>{@code
   *
   *  // use try with resources to ensure Stream is closed
   *
   *  try (Stream<T> stream = query.findStream()) {
   *    stream
   *    .map(...)
   *    .collect(...);
   *  }
   *
   * }</pre>
   */
  Stream<T> findStream();

  /**
   * Return a PagedList for this query using firstRow and maxRows.
   * <p>
   * The benefit of using this over findList() is that it provides functionality to get the
   * total row count etc.
   * <p>
   * If maxRows is not set on the query prior to calling findPagedList() then a
   * PersistenceException is thrown.
   *
   * @return The PagedList
   */
  PagedList<T> findPagedList();

}

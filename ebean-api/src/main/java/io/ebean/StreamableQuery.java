package io.ebean;

import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
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

  /**
   * Execute the query processing the results one at a time.
   * <p>
   * This method is appropriate to process very large query results as the results are
   * consumed one at a time and do not need to be held in memory (unlike {@link #findList()}).
   * <p>
   * Note that internally Ebean can inform the JDBC driver that it is expecting a larger
   * resultSet and specifically for MySQL this hint is required to stop its JDBC driver
   * from buffering the entire resultSet. As such, for smaller resultSets findList() is
   * generally preferable.
   * <p>
   * Compared with {@link #findEachWhile(Predicate)} this will always process all the results
   * whereas findEachWhile() provides a way to stop processing the query result early before
   * all the results have been read.
   *
   * @param consumer the consumer used to process the queried results.
   */
  void findEach(Consumer<T> consumer);

  /**
   * Execute findEach streaming query batching the results for consuming.
   * <p>
   * This query execution will stream the results and is suited to consuming
   * large numbers of results from the database.
   * <p>
   * Typically, we use this batch consumer when we want to do further processing on
   * the results and want to do that processing in batch form, for example - 100 at
   * a time.
   *
   * @param batch    The number of results processed in the batch
   * @param consumer Process the batch of results
   */
  void findEach(int batch, Consumer<List<T>> consumer);

  /**
   * Execute the query using callbacks to process the resulting results one at a time,
   * with the ability to stop processing part way through.
   * <p>
   * Returning {@code false} after processing a result stops the iteration through the
   * query results.
   *
   * @param consumer the consumer used to process the queried results, returning
   *                 {@code false} to stop processing.
   */
  void findEachWhile(Predicate<T> consumer);

}

package io.ebean;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import jakarta.persistence.EntityNotFoundException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Common find operations shared by the query types that can execute and return
 * results - {@link SqlQuery}, {@link DtoQuery}, {@link MappedQuery} and {@link QueryBuilder}.
 *
 * @param <SELF> The query type (used for method chaining)
 * @param <T>    The type of the result
 */
@NullMarked
public interface FindableQuery<SELF extends FindableQuery<SELF, T>, T> extends CancelableQuery {

  /**
   * Execute the query returning the list of results.
   */
  List<T> findList();

  /**
   * Execute the query returning a single result, or {@code null} if there is no matching row.
   * <p>
   * If more than 1 row is found for this query then a PersistenceException is thrown.
   */
  @Nullable
  T findOne();

  /**
   * Execute the query returning an optional result.
   */
  Optional<T> findOneOrEmpty();

  /**
   * Execute the query returning a single result or throwing a
   * {@link jakarta.persistence.EntityNotFoundException} if there is no matching row.
   */
  default T findOneOrThrow() {
    return findOneOrEmpty().orElseThrow(() -> new EntityNotFoundException("Not found"));
  }

  /**
   * Execute the query returning a single result or throwing the exception produced
   * by the given supplier if there is no matching row.
   */
  default T findOneOrThrow(Supplier<? extends RuntimeException> exceptionSupplier) {
    return findOneOrEmpty().orElseThrow(exceptionSupplier);
  }

  /**
   * Execute the query using the given transaction.
   */
  SELF usingTransaction(Transaction transaction);

  /**
   * Execute the query using the given connection.
   */
  SELF usingConnection(Connection connection);

  /**
   * Ensure that the master DataSource is used if there is a read only data source
   * being used (that is using a read replica database potentially with replication lag).
   * <p>
   * When the database is configured with a read-only DataSource via
   * say {@link DatabaseBuilder#readOnlyDataSource(DataSource)} then
   * by default when a query is run without an active transaction, it uses the read-only data
   * source. We use {@code usingMaster()} to instead ensure that the query is executed
   * against the master data source.
   */
  default SELF usingMaster() {
    return usingMaster(true);
  }

  /**
   * Ensure the master DataSource is used when useMaster is true. Otherwise, the read only
   * data source can be used if defined.
   *
   * @see #usingMaster()
   */
  SELF usingMaster(boolean useMaster);

}

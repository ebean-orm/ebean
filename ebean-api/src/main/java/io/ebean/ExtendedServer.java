package io.ebean;

import io.avaje.lang.Nullable;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * The extended API for Database.
 * <p>
 * Deprecated in favour of using {@link Query#usingTransaction(Transaction)} instead.
 * <p>
 * This provides the finder methods that take an explicit transaction rather than obtaining
 * the transaction from the usual mechanism (which is ThreadLocal based).
 * <p>
 * Note that in all cases the transaction supplied can be null and in this case the Database
 * will use the normal mechanism to obtain the transaction to use.
 */
public interface ExtendedServer {

  /**
   * Deprecated but no yet determined suitable replacement (to support testing only change of clock).
   * <p>
   * Set the Clock to use for <code>@WhenCreated</code> and <code>@WhenModified</code>.
   * <p>
   * Note that we only expect to change the Clock for testing purposes.
   * </p>
   */
  @Deprecated
  void setClock(Clock clock);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  <T> boolean exists(Query<T> ormQuery, Transaction transaction);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  <T> int findCount(Query<T> query, Transaction transaction);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  <A, T> List<A> findIds(Query<T> query, Transaction transaction);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  <T> QueryIterator<T> findIterate(Query<T> query, Transaction transaction);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  <T> Stream<T> findStream(Query<T> query, Transaction transaction);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  <T> void findEach(Query<T> query, Consumer<T> consumer, Transaction transaction);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  <T> void findEach(Query<T> query, int batch, Consumer<List<T>> consumer, Transaction t);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  <T> void findEachWhile(Query<T> query, Predicate<T> consumer, Transaction transaction);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  <T> List<Version<T>> findVersions(Query<T> query, Transaction transaction);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  <T> List<T> findList(Query<T> query, Transaction transaction);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  <T> FutureRowCount<T> findFutureCount(Query<T> query, Transaction transaction);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  <T> FutureIds<T> findFutureIds(Query<T> query, Transaction transaction);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  <T> FutureList<T> findFutureList(Query<T> query, Transaction transaction);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  <T> PagedList<T> findPagedList(Query<T> query, Transaction transaction);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  <T> Set<T> findSet(Query<T> query, Transaction transaction);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  <K, T> Map<K, T> findMap(Query<T> query, Transaction transaction);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  <A, T> List<A> findSingleAttributeList(Query<T> query, Transaction transaction);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  <A, T> Set<A> findSingleAttributeSet(Query<T> query, Transaction transaction);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  @Nullable
  <T> T findOne(Query<T> query, Transaction transaction);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  <T> Optional<T> findOneOrEmpty(Query<T> query, Transaction transaction);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  <T> int delete(Query<T> query, Transaction transaction);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  <T> int update(Query<T> query, Transaction transaction);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  List<SqlRow> findList(SqlQuery query, Transaction transaction);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  void findEach(SqlQuery query, Consumer<SqlRow> consumer, Transaction transaction);

  /**
   * Deprecated migrate to using {@link Query#usingTransaction(Transaction)}.
   */
  @Deprecated
  void findEachWhile(SqlQuery query, Predicate<SqlRow> consumer, Transaction transaction);

  /**
   * Deprecated migrate to using {@link SqlQuery#usingTransaction(Transaction)}.
   */
  @Deprecated
  @Nullable
  SqlRow findOne(SqlQuery query, Transaction transaction);

}

package io.ebeaninternal.api;

import org.jspecify.annotations.Nullable;
import io.ebean.*;
import io.ebean.bean.BeanCollectionLoader;
import io.ebean.bean.CallOrigin;
import io.ebean.meta.MetricVisitor;
import io.ebean.plugin.SpiServer;
import io.ebeaninternal.api.SpiQuery.Type;
import io.ebeaninternal.server.core.SpiResultSet;
import io.ebeaninternal.server.core.timezone.DataTimeZone;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.query.CQuery;
import io.ebeaninternal.server.transaction.RemoteTransactionEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Service Provider extension to EbeanServer.
 */
public interface SpiEbeanServer extends SpiServer, ExtendedServer, BeanCollectionLoader {

  /**
   * Return the NOW time from the Clock.
   */
  long clockNow();

  /**
   * Return true if the L2 cache has been disabled.
   */
  boolean isDisableL2Cache();

  /**
   * Return the log manager.
   */
  SpiLogManager log();

  /**
   * Return the server extended Json context.
   */
  SpiJsonContext jsonExtended();

  /**
   * Return true if updates in JDBC batch should include all columns if unspecified on the transaction.
   */
  boolean isUpdateAllPropertiesInBatch();

  /**
   * Return the current Tenant Id.
   */
  Object currentTenantId();

  /**
   * Create an object to represent the current CallStack.
   * <p>
   * Typically used to identify the origin of queries for AutoTune and object
   * graph costing.
   */
  CallOrigin createCallOrigin();

  /**
   * Override in order to return SpiQuery
   */
  <T> SpiQuery<T> createQuery(Class<T> beanType);

  /**
   * Return the PersistenceContextScope to use defined at query or server level.
   */
  PersistenceContextScope persistenceContextScope(SpiQuery<?> query);

  /**
   * Clear the query execution statistics.
   */
  void clearQueryStatistics();

  /**
   * Return the transaction manager.
   */
  SpiTransactionManager transactionManager();

  /**
   * End the current transaction if it is active.
   */
  void endTransaction();

  /**
   * Return all the descriptors.
   */
  List<BeanDescriptor<?>> descriptors();

  /**
   * Return the BeanDescriptor for a given type of bean.
   */
  <T> BeanDescriptor<T> descriptor(Class<T> type);

  /**
   * Return BeanDescriptor using it's unique id.
   */
  BeanDescriptor<?> descriptorById(String className);

  /**
   * Return BeanDescriptors mapped to this table.
   */
  List<BeanDescriptor<?>> descriptors(String tableName);

  /**
   * Process committed changes from another framework.
   * <p>
   * This notifies this instance of the framework that beans have been committed
   * externally to it. Either by another framework or clustered server. It uses
   * this to maintain its cache and text indexes appropriately.
   * </p>
   */
  void externalModification(TransactionEventTable event);

  /**
   * Clear an implicit transaction from the scope.
   */
  void clearServerTransaction();

  /**
   * Begin a managed transaction (Uses scope manager / ThreadLocal).
   */
  SpiTransaction beginServerTransaction();

  /**
   * Return the current transaction or null if there is no current transaction.
   */
  SpiTransaction currentServerTransaction();

  /**
   * Create a ServerTransaction for query purposes.
   *
   * @param tenantId For multi-tenant lazy loading provide the tenantId to use.
   * @param useMaster Set to true when the query should use the master data source.
   */
  SpiTransaction createReadOnlyTransaction(Object tenantId, boolean useMaster);

  /**
   * An event from another server in the cluster used to notify local
   * BeanListeners of remote inserts updates and deletes.
   */
  void remoteTransactionEvent(RemoteTransactionEvent event);

  /**
   * Compile a query.
   */
  <T> CQuery<T> compileQuery(Type type, SpiQuery<T> query, Transaction transaction);

  /**
   * Execute the findId's query but without copying the query.
   * <p>
   * Used so that the list of Id's can be made accessible to client code before
   * the query has finished (if executing in a background thread).
   * </p>
   */
  <A, T> List<A> findIdsWithCopy(SpiQuery<T> query);

  /**
   * Execute the findCount query but without copying the query.
   */
  <T> int findCountWithCopy(SpiQuery<T> query);

  /**
   * Load a batch of Associated One Beans.
   */
  void loadBean(LoadBeanRequest loadRequest);

  /**
   * Lazy load a batch of Many's.
   */
  void loadMany(LoadManyRequest loadRequest);

  /**
   * Return the default batch size for lazy loading.
   */
  int lazyLoadBatchSize();

  /**
   * Return true if the type is known as an Entity or Xml type or a List Set or
   * Map of known bean types.
   */
  boolean isSupportedType(java.lang.reflect.Type genericType);

  /**
   * Return the DataTimeZone to use when reading/writing timestamps via JDBC.
   */
  DataTimeZone dataTimeZone();

  /**
   * Check for slow query event.
   */
  void slowQueryCheck(long executionTimeMicros, int rowCount, SpiQuery<?> query);

  /**
   * SqlQuery find single attribute.
   */
  <T> T findSingleAttribute(SpiSqlQuery query, Class<T> cls);

  /**
   * SqlQuery find single attribute list.
   */
  <T> List<T> findSingleAttributeList(SpiSqlQuery query, Class<T> cls);

  /**
   * SqlQuery find single attribute streaming the result to a consumer.
   */
  <T> void findSingleAttributeEach(SpiSqlQuery query, Class<T> cls, Consumer<T> consumer);

  /**
   * SqlQuery find one with mapper.
   */
  <T> T findOneMapper(SpiSqlQuery query, RowMapper<T> mapper);

  /**
   * SqlQuery find list with mapper.
   */
  <T> List<T> findListMapper(SpiSqlQuery query, RowMapper<T> mapper);

  /**
   * SqlQuery find each with consumer.
   */
  void findEachRow(SpiSqlQuery query, RowConsumer consumer);

  /**
   * DTO findIterate query.
   */
  <T> QueryIterator<T> findDtoIterate(SpiDtoQuery<T> query);

  /**
   * DTO findStream query.
   */
  <T> Stream<T> findDtoStream(SpiDtoQuery<T> query);

  /**
   * DTO findList query.
   */
  <T> List<T> findDtoList(SpiDtoQuery<T> query);

  /**
   * DTO findOne query.
   */
  @Nullable
  <T> T findDtoOne(SpiDtoQuery<T> query);

  /**
   * DTO findEach query.
   */
  <T> void findDtoEach(SpiDtoQuery<T> query, Consumer<T> consumer);

  /**
   * DTO findEach batch query.
   */
  <T> void findDtoEach(SpiDtoQuery<T> query, int batch, Consumer<List<T>> consumer);

  /**
   * DTO findEachWhile query.
   */
  <T> void findDtoEachWhile(SpiDtoQuery<T> query, Predicate<T> consumer);

  /**
   * Return / wrap the ORM query as a DTO query.
   */
  <D> DtoQuery<D> findDto(Class<D> dtoType, SpiQuery<?> ormQuery);

  /**
   * Execute the underlying ORM query returning as a JDBC ResultSet to map to DTO beans.
   */
  SpiResultSet findResultSet(SpiQuery<?> ormQuery);

  /**
   * Visit all the metrics (typically reporting them).
   */
  void visitMetrics(MetricVisitor visitor);

  /**
   * Return true if a row for the bean type and id exists.
   */
  boolean exists(Class<?> beanType, Object beanId, Transaction transaction);

  /**
   * Add to JDBC batch for later execution.
   */
  void addBatch(SpiSqlUpdate defaultSqlUpdate, SpiTransaction transaction);

  /**
   * Execute the batched statement.
   */
  int[] executeBatch(SpiSqlUpdate defaultSqlUpdate, SpiTransaction transaction);

  /**
   * Execute the sql update regardless of transaction batch mode.
   */
  int executeNow(SpiSqlUpdate sqlUpdate);

  /**
   * Create a query bind capture for the given query plan.
   */
  SpiQueryBindCapture createQueryBindCapture(SpiQueryPlan queryPlan);

  <T> boolean exists(SpiQuery<T> ormQuery);

  <T> int findCount(SpiQuery<T> query);

  <A, T> List<A> findIds(SpiQuery<T> query);

  <T> QueryIterator<T> findIterate(SpiQuery<T> query);

  <T> Stream<T> findStream(SpiQuery<T> query);

  <T> void findEach(SpiQuery<T> query, Consumer<T> consumer);

  <T> void findEach(SpiQuery<T> query, int batch, Consumer<List<T>> consumer);

  <T> void findEachWhile(SpiQuery<T> query, Predicate<T> consumer);

  <T> List<Version<T>> findVersions(SpiQuery<T> query);

  <T> List<T> findList(SpiQuery<T> query);

  <T> FutureRowCount<T> findFutureCount(SpiQuery<T> query);

  <T> FutureIds<T> findFutureIds(SpiQuery<T> query);

  <T> FutureList<T> findFutureList(SpiQuery<T> query);

  <T> PagedList<T> findPagedList(SpiQuery<T> query);

  <T> Set<T> findSet(SpiQuery<T> query);

  <K, T> Map<K, T> findMap(SpiQuery<T> query);

  <A, T> List<A> findSingleAttributeList(SpiQuery<T> query);

  <A, T> Set<A> findSingleAttributeSet(SpiQuery<T> query);

  @Nullable
  <T> T findOne(SpiQuery<T> query);

  <T> Optional<T> findOneOrEmpty(SpiQuery<T> query);

  <T> int delete(SpiQuery<T> query);

  <T> int update(SpiQuery<T> query);

  List<SqlRow> findList(SpiSqlQuery query);

  void findEach(SpiSqlQuery query, Consumer<SqlRow> consumer);

  void findEachWhile(SpiSqlQuery query, Predicate<SqlRow> consumer);

  @Nullable
  SqlRow findOne(SpiSqlQuery query);
}

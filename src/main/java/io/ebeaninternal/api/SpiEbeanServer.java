package io.ebeaninternal.api;

import io.ebean.DtoQuery;
import io.ebean.EbeanServer;
import io.ebean.ExtendedServer;
import io.ebean.PersistenceContextScope;
import io.ebean.Query;
import io.ebean.RowConsumer;
import io.ebean.RowMapper;
import io.ebean.Transaction;
import io.ebean.TxScope;
import io.ebean.bean.BeanCollectionLoader;
import io.ebean.bean.BeanLoader;
import io.ebean.bean.CallStack;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.DatabasePlatform;
import io.ebean.event.readaudit.ReadAuditLogger;
import io.ebean.event.readaudit.ReadAuditPrepare;
import io.ebean.meta.MetricVisitor;
import io.ebeaninternal.dbmigration.ddlgeneration.DdlHandler;
import io.ebeaninternal.server.core.SpiResultSet;
import io.ebeaninternal.server.core.timezone.DataTimeZone;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.query.CQuery;
import io.ebeaninternal.server.transaction.RemoteTransactionEvent;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Service Provider extension to EbeanServer.
 */
public interface SpiEbeanServer extends ExtendedServer, EbeanServer, BeanLoader, BeanCollectionLoader {

  /**
   * Return the log manager.
   */
  SpiLogManager log();

  /**
   * Return the server extended Json context.
   */
  SpiJsonContext jsonExtended();

  /**
   * For internal use, shutdown of the server invoked by JVM Shutdown.
   */
  void shutdownManaged();

  /**
   * Return true if query origins should be collected.
   */
  boolean isCollectQueryOrigins();

  /**
   * Return true if updates in JDBC batch should include all columns if unspecified on the transaction.
   */
  boolean isUpdateAllPropertiesInBatch();

  /**
   * Return the current Tenant Id.
   */
  Object currentTenantId();

  /**
   * Return the server configuration.
   */
  ServerConfig getServerConfig();

  /**
   * Return the DatabasePlatform for this server.
   */
  DatabasePlatform getDatabasePlatform();

  /**
   * Create an object to represent the current CallStack.
   * <p>
   * Typically used to identify the origin of queries for AutoTune and object
   * graph costing.
   * </p>
   */
  CallStack createCallStack();

  /**
   * Return the PersistenceContextScope to use defined at query or server level.
   */
  PersistenceContextScope getPersistenceContextScope(SpiQuery<?> query);

  /**
   * Clear the query execution statistics.
   */
  void clearQueryStatistics();

  /**
   * Return the transaction manager.
   */
  SpiTransactionManager getTransactionManager();

  /**
   * Return all the descriptors.
   */
  List<BeanDescriptor<?>> getBeanDescriptors();

  /**
   * Return the BeanDescriptor for a given type of bean.
   */
  <T> BeanDescriptor<T> getBeanDescriptor(Class<T> type);

  /**
   * Return BeanDescriptor using it's unique id.
   */
  BeanDescriptor<?> getBeanDescriptorById(String className);

  /**
   * Return BeanDescriptor using it's unique doc store queueId.
   */
  BeanDescriptor<?> getBeanDescriptorByQueueId(String queueId);

  /**
   * Return BeanDescriptors mapped to this table.
   */
  List<BeanDescriptor<?>> getBeanDescriptors(String tableName);

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
   */
  SpiTransaction createQueryTransaction(Object tenantId);

  /**
   * An event from another server in the cluster used to notify local
   * BeanListeners of remote inserts updates and deletes.
   */
  void remoteTransactionEvent(RemoteTransactionEvent event);

  /**
   * Compile a query.
   */
  <T> CQuery<T> compileQuery(Query<T> query, Transaction t);

  /**
   * Execute the findId's query but without copying the query.
   * <p>
   * Used so that the list of Id's can be made accessible to client code before
   * the query has finished (if executing in a background thread).
   * </p>
   */
  <A, T> List<A> findIdsWithCopy(Query<T> query, Transaction t);

  /**
   * Execute the findCount query but without copying the query.
   */
  <T> int findCountWithCopy(Query<T> query, Transaction t);

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
  int getLazyLoadBatchSize();

  /**
   * Return true if the type is known as an Entity or Xml type or a List Set or
   * Map of known bean types.
   */
  boolean isSupportedType(java.lang.reflect.Type genericType);

  /**
   * Collect query statistics by ObjectGraphNode. Used for Lazy loading reporting.
   */
  void collectQueryStats(ObjectGraphNode objectGraphNode, long loadedBeanCount, long timeMicros);

  /**
   * Return the ReadAuditLogger to use for logging all read audit events.
   */
  ReadAuditLogger getReadAuditLogger();

  /**
   * Return the ReadAuditPrepare used to populate the read audit events with
   * user context information (user id, user ip address etc).
   */
  ReadAuditPrepare getReadAuditPrepare();

  /**
   * Return the DataTimeZone to use when reading/writing timestamps via JDBC.
   */
  DataTimeZone getDataTimeZone();

  /**
   * Check for slow query event.
   */
  void slowQueryCheck(long executionTimeMicros, int rowCount, SpiQuery<?> query);

  /**
   * Create DDL handler given the platform and configuration of the server.
   */
  DdlHandler createDdlHandler();

  /**
   * Start an enhanced transactional method.
   */
  void scopedTransactionEnter(TxScope txScope);

  /**
   * Handle the end of an enhanced Transactional method.
   */
  void scopedTransactionExit(Object returnOrThrowable, int opCode);

  /**
   * SqlQuery find single attribute.
   */
  <T> T findSingleAttribute(SpiSqlQuery query, Class<T> cls);

  /**
   * SqlQuery find single attribute list.
   */
  <T> List<T> findSingleAttributeList(SpiSqlQuery query, Class<T> cls);

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
   * DTO findList query.
   */
  <T> List<T> findDtoList(SpiDtoQuery<T> query);

  /**
   * DTO findOne query.
   */
  <T> T findDtoOne(SpiDtoQuery<T> query);

  /**
   * DTO findEach query.
   */
  <T> void findDtoEach(SpiDtoQuery<T> query, Consumer<T> consumer);

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
  SpiResultSet findResultSet(SpiQuery<?> ormQuery, SpiTransaction transaction);

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

}

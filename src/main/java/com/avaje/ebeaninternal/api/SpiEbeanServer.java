package com.avaje.ebeaninternal.api;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.PersistenceContextScope;
import com.avaje.ebean.Query;
import com.avaje.ebean.Transaction;
import com.avaje.ebean.TxScope;
import com.avaje.ebean.bean.BeanCollectionLoader;
import com.avaje.ebean.bean.BeanLoader;
import com.avaje.ebean.bean.CallStack;
import com.avaje.ebean.bean.ObjectGraphNode;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebean.event.readaudit.ReadAuditLogger;
import com.avaje.ebean.event.readaudit.ReadAuditPrepare;
import com.avaje.ebeaninternal.server.core.SpiOrmQueryRequest;
import com.avaje.ebean.dbmigration.DdlGenerator;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.query.CQuery;
import com.avaje.ebeaninternal.server.query.CQueryEngine;
import com.avaje.ebeaninternal.server.transaction.RemoteTransactionEvent;

import java.util.List;

/**
 * Service Provider extension to EbeanServer.
 */
public interface SpiEbeanServer extends EbeanServer, BeanLoader, BeanCollectionLoader {

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
   * Create a ServerTransaction.
   * <p>
   * To specify to use the default transaction isolation use a value of -1.
   * </p>
   */
  SpiTransaction createServerTransaction(boolean isExplicit, int isolationLevel);

  /**
   * Return the current transaction or null if there is no current transaction.
   */
  SpiTransaction getCurrentServerTransaction();

  /**
   * Create a ScopeTrans for a method for the given scope definition.
   */
  ScopeTrans createScopeTrans(TxScope txScope);

  /**
   * Create a ServerTransaction for query purposes.
   */
  SpiTransaction createQueryTransaction();

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
  <T> List<Object> findIdsWithCopy(Query<T> query, Transaction t);

  /**
   * Execute the findRowCount query but without copying the query.
   */
  <T> int findRowCountWithCopy(Query<T> query, Transaction t);

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
}

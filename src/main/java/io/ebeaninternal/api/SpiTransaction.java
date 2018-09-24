package io.ebeaninternal.api;

import io.ebean.ProfileLocation;
import io.ebean.Transaction;
import io.ebean.annotation.DocStoreMode;
import io.ebean.bean.PersistenceContext;
import io.ebean.event.changelog.BeanChange;
import io.ebean.event.changelog.ChangeSet;
import io.ebeaninternal.server.core.PersistDeferredRelationship;
import io.ebeaninternal.server.core.PersistRequest;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.persist.BatchControl;
import io.ebeaninternal.server.transaction.ProfileStream;
import io.ebeanservice.docstore.api.DocStoreTransaction;

import javax.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Extends Transaction with additional API required on server.
 * <p>
 * Provides support for batching and TransactionContext.
 * </p>
 */
public interface SpiTransaction extends Transaction {

  /**
   * Return the user defined label for the transaction.
   */
  String getLabel();

  /**
   * Return the string prefix with the transaction id and label used in logging.
   */
  String getLogPrefix();

  /**
   * Return true if generated SQL and Bind values should be logged to the
   * transaction log.
   */
  boolean isLogSql();

  /**
   * Return true if summary level events should be logged to the transaction
   * log.
   */
  boolean isLogSummary();

  /**
   * Log a message to the SQL logger.
   */
  void logSql(String msg);

  /**
   * Log a message to the SUMMARY logger.
   */
  void logSummary(String msg);

  /**
   * Register a "Deferred Relationship" that requires an additional update later.
   */
  void registerDeferred(PersistDeferredRelationship derived);

  /**
   * Add a deleting bean to the registered list.
   * <p>
   * This is to handle bi-directional relationships where both sides Cascade.
   * </p>
   */
  void registerDeleteBean(Integer hash);

  /**
   * Unregister the hash of the bean.
   */
  void unregisterDeleteBean(Integer hash);

  /**
   * Return true if this is a bean that has already been saved/deleted.
   */
  boolean isRegisteredDeleteBean(Integer hash);

  /**
   * Unregister the persisted bean.
   */
  void unregisterBean(Object bean);

  /**
   * Return true if this is a bean that has already been persisted in the
   * current recursive save request. The goal is to stop recursively saving
   * the bean when cascade persist is on both sides of a relationship).
   * <p>
   * This will register the bean if it is not already.
   * </p>
   */
  boolean isRegisteredBean(Object bean);

  /**
   * Returns a String used to identify the transaction. This id is used for
   * Transaction logging.
   */
  String getId();

  /**
   * Return the start timestamp for the transaction (JVM side).
   */
  long getStartMillis();

  /**
   * Return true if this transaction has updateAllLoadedProperties set.
   * If null is returned the server default is used (set on ServerConfig).
   */
  Boolean isUpdateAllLoadedProperties();

  /**
   * Return the batchSize specifically set for this transaction or 0.
   * <p>
   * Returning 0 implies to use the system wide default batch size.
   * </p>
   */
  DocStoreMode getDocStoreMode();

  /**
   * Return the batch size to us for ElasticSearch Bulk API calls
   * as a result of this transaction.
   */
  int getDocStoreBatchSize();

  /**
   * Return the batchSize specifically set for this transaction or 0.
   * <p>
   * Returning 0 implies to use the system wide default batch size.
   * </p>
   */
  @Override
  int getBatchSize();

  /**
   * Return the getGeneratedKeys setting for this transaction.
   */
  Boolean getBatchGetGeneratedKeys();

  /**
   * Modify and return the current 'depth' of the transaction.
   * <p>
   * As we cascade save or delete we traverse the object graph tree. Going up
   * to Assoc Ones the depth decreases and going down to Assoc Manys the depth
   * increases.
   * </p>
   * <p>
   * The depth is used for ordering batching statements. The lowest depth get
   * executed first during save.
   * </p>
   */
  void depth(int diff);

  /**
   * Return the current depth.
   */
  int depth();

  /**
   * Return true if this transaction was created explicitly via
   * <code>Ebean.beginTransaction()</code>.
   */
  boolean isExplicit();

  /**
   * Get the object that holds the event details.
   * <p>
   * This information is used maintain the table state, cache and text
   * indexes. On commit the Table modifications this generates is broadcast
   * around the cluster (if you have a cluster).
   * </p>
   */
  TransactionEvent getEvent();

  /**
   * Whether persistCascade is on for save and delete.
   */
  boolean isPersistCascade();

  /**
   * Return true if this request should be batched. Conversely returns false
   * if this request should be executed immediately.
   */
  boolean isBatchThisRequest();

  /**
   * Return the BatchControl used to batch up persist requests.
   */
  BatchControl getBatchControl();

  /**
   * Set the BatchControl used to batch up persist requests. There should only be one
   * PersistQueue set per transaction.
   */
  void setBatchControl(BatchControl control);

  /**
   * Return the persistence context associated with this transaction.
   * <p>
   * You may wish to hold onto this and set it against another transaction
   * later. This is along the lines of 'extended persistence context'
   * behaviour.
   * </p>
   */
  PersistenceContext getPersistenceContext();

  /**
   * Set the persistence context to this transaction.
   * <p>
   * This could be considered similar to 'EJB3 Extended Persistence Context'.
   * In that you can get the PersistenceContext from a transaction, hold onto
   * it, and then set it back later to a second transaction. In general there
   * is one PersistenceContext per Transaction. The getPersistenceContext()
   * and setPersistenceContext() enable a developer to reuse a single
   * PersistenceContext with multiple transactions.
   * </p>
   */
  void setPersistenceContext(PersistenceContext context);

  /**
   * Return the underlying Connection for internal use.
   * <p>
   * If the connection is made from Transaction and the user code calls
   * that method we can no longer trust the query only status of a
   * Transaction.
   * </p>
   */
  Connection getInternalConnection();

  /**
   * Return true if the manyToMany intersection should be persisted for this particular relationship direction.
   */
  boolean isSaveAssocManyIntersection(String intersectionTable, String beanName);

  /**
   * Return true if batch mode got escalated for this request (and associated cascades).
   */
  boolean checkBatchEscalationOnCascade(PersistRequestBean<?> request);

  /**
   * If batch mode was turned on for the request then flush the batch.
   */
  void flushBatchOnCascade();

  /**
   * If batch was on then effectively clear the batch such that we can handle exceptions and continue.
   */
  void flushBatchOnRollback();

  /**
   * Translate the SQLException.
   */
  PersistenceException translate(String message, SQLException cause);

  /**
   * Mark the transaction explicitly as not being query only.
   */
  void markNotQueryOnly();

  /**
   * Potentially escalate batch mode on saving or deleting a collection.
   */
  void checkBatchEscalationOnCollection();

  /**
   * Flush batch if we escalated batch mode on saving or deleting a collection.
   */
  void flushBatchOnCollection();

  /**
   * Add a bean change to the change log.
   */
  void addBeanChange(BeanChange beanChange);

  /**
   * Send the change set to be prepared and then logged.
   */
  void sendChangeLog(ChangeSet changeSet);

  /**
   * Return a document store transaction.
   */
  DocStoreTransaction getDocStoreTransaction();

  /**
   * Set the current Tenant Id.
   */
  void setTenantId(Object tenantId);

  /**
   * Return the current Tenant Id.
   */
  Object getTenantId();

  /**
   * Return the offset time from the start of the transaction.
   */
  long profileOffset();

  /**
   * Check if the event should be added to a profiling transaction.
   */
  void profileEvent(SpiProfileTransactionEvent event);

  /**
   * Set the profileStream to catch and time all the events for this transaction.
   */
  void setProfileStream(ProfileStream profileStream);

  /**
   * Return the stream that profiling events are written to.
   */
  ProfileStream profileStream();

  /**
   * Set the profile location for this transaction.
   */
  void setProfileLocation(ProfileLocation profileLocation);

  /**
   * Return the profile location for this transaction.
   */
  ProfileLocation getProfileLocation();

  /**
   * Return true when nested transactions should create Savepoints.
   */
  boolean isNestedUseSavepoint();
}

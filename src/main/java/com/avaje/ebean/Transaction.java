package com.avaje.ebean;

import com.avaje.ebean.annotation.DocStoreMode;
import com.avaje.ebean.config.DocStoreConfig;
import com.avaje.ebean.config.PersistBatch;

import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;
import java.io.Closeable;
import java.sql.Connection;

/**
 * The Transaction object. Typically representing a JDBC or JTA transaction.
 */
public interface Transaction extends Closeable {

  /**
   * Read Committed transaction isolation. Same as
   * java.sql.Connection.TRANSACTION_READ_COMMITTED.
   */
  int READ_COMMITTED = java.sql.Connection.TRANSACTION_READ_COMMITTED;

  /**
   * Read Uncommitted transaction isolation. Same as
   * java.sql.Connection.TRANSACTION_READ_UNCOMMITTED.
   */
  int READ_UNCOMMITTED = java.sql.Connection.TRANSACTION_READ_UNCOMMITTED;

  /**
   * Repeatable read transaction isolation. Same as
   * java.sql.Connection.TRANSACTION_REPEATABLE_READ.
   */
  int REPEATABLE_READ = java.sql.Connection.TRANSACTION_REPEATABLE_READ;

  /**
   * Serializable transaction isolation. Same as
   * java.sql.Connection.TRANSACTION_SERIALIZABLE.
   */
  int SERIALIZABLE = java.sql.Connection.TRANSACTION_SERIALIZABLE;

  /**
   * Register a TransactionCallback with this transaction.
   */
  void register(TransactionCallback callback);

  /**
   * Return true if this transaction is read only.
   */
  boolean isReadOnly();

  /**
   * Set whether this transaction should be readOnly.
   */
  void setReadOnly(boolean readOnly);

  /**
   * Commit the transaction.
   */
  void commit() throws RollbackException;

  /**
   * Rollback the transaction.
   */
  void rollback() throws PersistenceException;

  /**
   * Rollback the transaction specifying a throwable that caused the rollback to
   * occur.
   * <p>
   * If you are using transaction logging this will log the throwable in the
   * transaction logs.
   * </p>
   */
  void rollback(Throwable e) throws PersistenceException;

  /**
   * If the transaction is active then perform rollback. Otherwise do nothing.
   */
  void end() throws PersistenceException;

  /**
   * Return true if the transaction is active.
   */
  boolean isActive();

  /**
   * Set the behavior for document store updates on this transaction.
   * <p>
   *   For example, set the mode to DocStoreEvent.IGNORE for this transaction and
   *   then any changes via this transaction are not sent to the doc store. This
   *   would be used when doing large bulk inserts into the database and we want
   *   to control how that is sent to the document store.
   * </p>
   */
  void setDocStoreUpdateMode(DocStoreMode updateMode);

  /**
   * Set the batch size to use for sending messages to the document store.
   * <p>
   *   You might set this if you know the changes in this transaction result in especially large or
   *   especially small payloads and want to adjust the batch size to match.
   * </p>
   * <p>
   *   Setting this overrides the default of {@link DocStoreConfig#getBulkBatchSize()}
   * </p>
   */
  void setDocStoreUpdateBatchSize(int batchSize);

  /**
   * Explicitly turn off or on the cascading nature of save() and delete(). This
   * gives the developer exact control over what beans are saved and deleted
   * rather than Ebean cascading detecting 'dirty/modified' beans etc.
   * <p>
   * This is useful if you can getting back entity beans from a layer of code
   * (potentially remote) and you prefer to have exact control.
   * </p>
   * <p>
   * This may also be useful if you are using jdbc batching with jdbc drivers
   * that do not support getGeneratedKeys.
   * </p>
   */
  void setPersistCascade(boolean persistCascade);

  /**
   * Set to true when you want all loaded properties to be included in the update
   * (rather than just the changed properties).
   * <p>
   *   You might set this when using JDBC batch in order to get multiple updates
   *   with slightly different sets of changed properties into the same statement
   *   and hence better JDBC batch performance.
   * </p>
   */
  void setUpdateAllLoadedProperties(boolean updateAllLoadedProperties);

  /**
   * Turn on or off statement batching. Statement batching can be transparent
   * for drivers and databases that support getGeneratedKeys. Otherwise you may
   * wish to specifically control when batching is used via this method.
   * <p>
   * Refer to <code>java.sql.PreparedStatement.addBatch();</code>
   * <p>
   * Note that you may also wish to use the setPersistCascade method to stop
   * save and delete cascade behaviour. You may do this to have full control
   * over the order of execution rather than the normal cascading fashion.
   * </p>
   * <p>
   * Note that the <em>execution order</em> in batch mode may be different from
   * non batch mode execution order. Also note that <em>insert behaviour</em>
   * may be different depending on the JDBC driver and its support for
   * getGeneratedKeys. That is, for JDBC drivers that do not support
   * getGeneratedKeys you may not get back the generated IDs (used for inserting
   * associated detail beans etc).
   * </p>
   * <p>
   * Calls to save(), delete(), insert() and execute() all support batch
   * processing. This includes normal beans, MapBean, CallableSql and UpdateSql.
   * </p>
   * <p>
   * The flushing of the batched statements is automatic but you can call
   * batchFlush when you like. Note that flushing occurs when a query is
   * executed or when you mix UpdateSql and CallableSql with save and delete of
   * beans.
   * </p>
   * <p>
   * Example: batch processing executing every 3 rows
   * </p>
   * 
   * <pre>{@code
   *
   * String data = "This is a simple test of the batch processing"
   *             + " mode and the transaction execute batch method";
   * 
   * String[] da = data.split(" ");
   * 
   * String sql = "{call sp_t3(?,?)}";
   * 
   * CallableSql cs = new CallableSql(sql);
   * cs.registerOut(2, Types.INTEGER);
   * 
   * // (optional) inform eBean this stored procedure
   * // inserts into a table called sp_test
   * cs.addModification("sp_test", true, false, false);
   * 
   * Transaction txn = ebeanServer.beginTransaction();
   * txn.setBatchMode(true);
   * txn.setBatchSize(3);
   * try {
   *   for (int i = 0; i < da.length;) {
   *     cs.setParameter(1, da[i]);
   *     ebeanServer.execute(cs);
   *   }
   * 
   *   // NB: commit implicitly flushes
   *   txn.commit();
   * 
   * } finally {
   *   txn.end();
   * }
   *
   * }</pre>
   * 
   */
  void setBatchMode(boolean useBatch);

  /**
   * The JDBC batch mode to use for this transaction.
   * <p>
   * If this is NONE then JDBC batch can still be used for each request - save(), insert(), update() or delete()
   * and this would be useful if the request cascades to detail beans.
   * </p>
   *
   * @param persistBatchMode the batch mode to use for this transaction
   *
   * @see com.avaje.ebean.config.ServerConfig#setPersistBatch(com.avaje.ebean.config.PersistBatch)
   */
  void setBatch(PersistBatch persistBatchMode);

  /**
   * Return the batch mode at the transaction level.
   */
  PersistBatch getBatch();

  /**
   * Set the JDBC batch mode to use for a save() or delete() request.
   * <p>
   * This only takes effect when batch mode on the transaction has not already meant that
   * JDBC batch mode is being used.
   * </p>
   * <p>
   * This is useful when the single save() or delete() cascades. For example, inserting a 'master' cascades
   * and inserts a collection of 'detail' beans. The detail beans can be inserted using JDBC batch.
   * </p>
   *
   * @param batchOnCascadeMode the batch mode to use per save(), insert(), update() or delete()
   *
   * @see com.avaje.ebean.config.ServerConfig#setPersistBatchOnCascade(com.avaje.ebean.config.PersistBatch)
   */
  void setBatchOnCascade(PersistBatch batchOnCascadeMode);

  /**
   * Return the batch mode at the request level (for each save(), insert(), update() or delete()).
   */
  PersistBatch getBatchOnCascade();

  /**
   * Specify the number of statements before a batch is flushed automatically.
   */
  void setBatchSize(int batchSize);

  /**
   * Return the current batch size.
   */
  int getBatchSize();

  /**
   * Specify if you want batched inserts to use getGeneratedKeys.
   * <p>
   * By default batched inserts will try to use getGeneratedKeys if it is
   * supported by the underlying jdbc driver and database.
   * </p>
   * <p>
   * You may want to turn getGeneratedKeys off when you are inserting a large
   * number of objects and you don't care about getting back the ids.
   * </p>
   */
  void setBatchGetGeneratedKeys(boolean getGeneratedKeys);

  /**
   * By default when mixing UpdateSql (or CallableSql) with Beans the batch is
   * automatically flushed when you change (between persisting beans and
   * executing UpdateSql or CallableSql).
   * <p>
   * If you want to execute both WITHOUT having the batch automatically flush
   * you need to call this with batchFlushOnMixed = false.
   * </p>
   * <p>
   * Note that UpdateSql and CallableSql are ALWAYS executed first (before the
   * beans are executed). This is because the UpdateSql and CallableSql have
   * already been bound to their PreparedStatements. The beans on the other hand
   * have a 2 step process (delayed binding).
   * </p>
   */
  void setBatchFlushOnMixed(boolean batchFlushOnMixed);

  /**
   * By default executing a query will automatically flush any batched
   * statements (persisted beans, executed UpdateSql etc).
   * <p>
   * Calling this method with batchFlushOnQuery = false means that you can
   * execute a query and the batch will not be automatically flushed.
   * </p>
   */
  void setBatchFlushOnQuery(boolean batchFlushOnQuery);

  /**
   * Return true if the batch (of persisted beans or executed UpdateSql etc)
   * should be flushed prior to executing a query.
   * <p>
   * The default is for this to be true.
   * </p>
   */
  boolean isBatchFlushOnQuery();

  /**
   * The batch will be flushing automatically but you can use this to explicitly
   * flush the batch if you like.
   * <p>
   * Flushing occurs automatically when:
   * </p>
   * <ul>
   * <li>the batch size is reached</li>
   * <li>A query is executed on the same transaction</li>
   * <li>UpdateSql or CallableSql are mixed with bean save and delete</li>
   * <li>Transaction commit occurs</li>
   * </ul>
   */
  void flushBatch() throws PersistenceException;

  /**
   * Return the underlying Connection object.
   * <p>
   * Useful where a Developer wishes to use the JDBC API directly. Note that the
   * commit() rollback() and end() methods on the Transaction should still be
   * used. Calling these methods on the Connection would be a big no no unless
   * you know what you are doing.
   * </p>
   * <p>
   * Examples of when a developer may wish to use the connection directly are:
   * Savepoints, advanced CLOB BLOB use and advanced stored procedure calls.
   * </p>
   */
  Connection getConnection();

  /**
   * Add table modification information to the TransactionEvent.
   * <p>
   * Use this in conjunction with getConnection() and raw JDBC.
   * </p>
   * <p>
   * This effectively informs Ebean of the data that has been changed by the
   * transaction and this information is normally automatically handled by Ebean
   * when you save entity beans or use UpdateSql etc.
   * </p>
   * <p>
   * If you use raw JDBC then you can use this method to inform Ebean for the
   * tables that have been modified. Ebean uses this information to keep its
   * caches in synch and maintain text indexes.
   * </p>
   */
  void addModification(String tableName, boolean inserts, boolean updates, boolean deletes);

  /**
   * Add an arbitrary user object to the transaction. The objects added have no
   * impact on any internals of ebena and are solely meant as a convenient
   * method push user information to e.g. the
   * {@link com.avaje.ebean.event.TransactionEventListener}.
   */
  void putUserObject(String name, Object value);

  /**
   * Get an object added with {@link #putUserObject(String, Object)}.
   */
  Object getUserObject(String name);
}

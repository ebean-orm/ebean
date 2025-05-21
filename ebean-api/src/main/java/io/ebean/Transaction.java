package io.ebean;

import io.ebean.annotation.DocStoreMode;
import io.ebean.annotation.PersistBatch;
import io.ebean.bean.PersistenceContext;
import io.ebean.config.DatabaseConfig;
import io.ebean.config.DocStoreConfig;

import jakarta.persistence.PersistenceException;
import java.sql.Connection;

/**
 * The Transaction object. Typically representing a JDBC or JTA transaction.
 */
public interface Transaction extends AutoCloseable {

  /**
   * Return the current transaction (of the default database) or null if there is
   * no current transaction in scope.
   * <p>
   * This is the same as <code>DB.currentTransaction()</code>
   * </p>
   * <p>
   * This returns the current transaction for the default database.
   * </p>
   *
   * @see DB#currentTransaction()
   * @see Database#currentTransaction()
   */
  static Transaction current() {
    return DB.currentTransaction();
  }

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
   * EXPERIMENTAL - turn on automatic persistence of dirty beans and batchMode true.
   * <p>
   * With this turned on beans that are dirty in the persistence context
   * are automatically persisted on flush() and commit().
   */
  void setAutoPersistUpdates(boolean autoPersistUpdates);

  /**
   * Set a label on the transaction.
   * <p>
   * This label is used to group transaction execution times for performance metrics reporting.
   * </p>
   */
  void setLabel(String label);

  /**
   * Return true if this transaction is read only.
   */
  boolean isReadOnly();

  /**
   * Set whether this transaction should be readOnly.
   */
  void setReadOnly(boolean readOnly);

  /**
   * Commits the transaction at this point with the expectation that another
   * commit (or rollback or end) will occur later to complete the transaction.
   * <p>
   * This is similar to commit() but leaves the transaction "Active".
   * </p>
   * <h3>Functions</h3>
   * <ul>
   * <li>Flush the JDBC batch buffer</li>
   * <li>Call commit on the underlying JDBC connection</li>
   * <li>Trigger any registered TransactionCallbacks</li>
   * <li>Perform post-commit processing updating L2 cache, ElasticSearch etc</li>
   * </ul>
   */
  void commitAndContinue();

  /**
   * Commit the transaction.
   * <p>
   * This performs commit and completes the transaction closing underlying resources and
   * marking the transaction as "In active".
   * </p>
   * <h3>Functions</h3>
   * <ul>
   * <li>Flush the JDBC batch buffer</li>
   * <li>Call commit on the underlying JDBC connection</li>
   * <li>Trigger any registered TransactionCallbacks</li>
   * <li>Perform post-commit processing updating L2 cache, ElasticSearch etc</li>
   * <li>Close any underlying resources, closing the underlying JDBC connection</li>
   * <li>Mark the transaction as "Inactive"</li>
   * </ul>
   */
  void commit();

  /**
   * Rollback the transaction.
   * <p>
   * This performs rollback, closes underlying resources and marks the transaction as "In active".
   * </p>
   * <h3>Functions</h3>
   * <ul>
   * <li>Call rollback on the underlying JDBC connection</li>
   * <li>Trigger any registered TransactionCallbacks</li>
   * <li>Close any underlying resources, closing the underlying JDBC connection</li>
   * <li>Mark the transaction as "Inactive"</li>
   * </ul>
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
   * Performs a rollback on the underlying JDBC connection with the intention of
   * continuing to use this same transaction and performing a commit or rollback
   * later to complete the transaction.
   * <p>
   * Typically used when catching {@link DuplicateKeyException} where we wish to
   * rollback work done at that point but carry on processing using the transaction.
   *
   * <pre>{@code
   *
   *   try (Transaction txn = database.beginTransaction()) {
   *
   *     try {
   *       ...
   *       database.save(bean);
   *       database.flush();
   *     } catch (DuplicateKeyException e) {
   *       // carry on processing using the transaction
   *       txn.rollbackAndContinue();
   *       ...
   *     }
   *
   *     txn.commit();
   *   }
   *
   * }</pre>
   */
  void rollbackAndContinue();

  /**
   * Set when we want nested transactions to use Savepoint's.
   * <p>
   * This means that for a nested transaction:
   * <ul>
   * <li>begin transaction maps to creating a savepoint</li>
   * <li>commit transaction maps to releasing a savepoint</li>
   * <li>rollback transaction maps to rollback a savepoint</li>
   * </ul>
   */
  void setNestedUseSavepoint();

  /**
   * Mark the transaction for rollback only.
   */
  void setRollbackOnly();

  /**
   * Return true if the transaction is marked as rollback only.
   */
  boolean isRollbackOnly();

  /**
   * If the transaction is active then perform rollback. Otherwise do nothing.
   */
  void end();

  /**
   * Synonym for end() to support AutoClosable.
   */
  @Override
  void close();

  /**
   * Return true if the transaction is active.
   */
  boolean isActive();

  /**
   * Set the behavior for document store updates on this transaction.
   * <p>
   * For example, set the mode to DocStoreEvent.IGNORE for this transaction and
   * then any changes via this transaction are not sent to the doc store. This
   * would be used when doing large bulk inserts into the database and we want
   * to control how that is sent to the document store.
   * </p>
   */
  void setDocStoreMode(DocStoreMode mode);

  /**
   * Set the batch size to use for sending messages to the document store.
   * <p>
   * You might set this if you know the changes in this transaction result in especially large or
   * especially small payloads and want to adjust the batch size to match.
   * </p>
   * <p>
   * Setting this overrides the default of {@link DocStoreConfig#getBulkBatchSize()}
   * </p>
   */
  void setDocStoreBatchSize(int batchSize);

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
   * You might set this when using JDBC batch in order to get multiple updates
   * with slightly different sets of changed properties into the same statement
   * and hence better JDBC batch performance.
   * </p>
   */
  void setUpdateAllLoadedProperties(boolean updateAllLoadedProperties);

  /**
   * If set to false (default is true) generated propertes are only set, if it is the version property or have a null value.
   * This may be useful in backup & restore scenarios, if you want set WhenCreated/WhenModified.
   */
  void setOverwriteGeneratedProperties(boolean overwriteGeneratedProperties);

  /**
   * Set if the L2 cache should be skipped for "find by id" and "find by natural key" queries.
   * <p>
   * By default {@link DatabaseConfig#isSkipCacheAfterWrite()} is true and that means that for
   * "find by id" and "find by natural key" queries which normally hit L2 bean cache automatically
   * - will not do so after a persist/write on the transaction.
   * </p>
   * <p>
   * This method provides explicit control over whether "find by id" and "find by natural key"
   * will skip the L2 bean cache or not (regardless of whether the transaction is considered "read only").
   * </p>
   * <p>
   * Refer to {@link DatabaseConfig#setSkipCacheAfterWrite(boolean)} for configuring the default behavior
   * for using the L2 bean cache in transactions spanning multiple query/persist requests.
   * </p>
   *
   * <pre>{@code
   *
   *   // assume Customer has L2 bean caching enabled ...
   *
   *   try (Transaction transaction = DB.beginTransaction()) {
   *
   *     // this uses L2 bean cache as the transaction
   *     // ... is considered "query only" at this point
   *     Customer.find.byId(42);
   *
   *     // transaction no longer "query only" once
   *     // ... a bean has been saved etc
   *     someBean.save();
   *
   *     // will NOT use L2 bean cache as the transaction
   *     // ... is no longer considered "query only"
   *     Customer.find.byId(55);
   *
   *
   *
   *     // explicit control - please use L2 bean cache
   *
   *     transaction.setSkipCache(false);
   *     Customer.find.byId(77); // hit the l2 bean cache
   *
   *
   *     // explicit control - please don't use L2 bean cache
   *
   *     transaction.setSkipCache(true);
   *     Customer.find.byId(99); // skips l2 bean cache
   *
   *
   *     transaction.commit();
   *   }
   *
   * }</pre>
   *
   * @see DatabaseConfig#isSkipCacheAfterWrite()
   */
  void setSkipCache(boolean skipCache);

  /**
   * Return true if the L2 cache should be skipped. More accurately if true then find by id
   * and find by natural key queries should NOT automatically use the L2 bean cache.
   */
  boolean isSkipCache();

  /**
   * Turn on or off use of JDBC statement batching.
   * <p>
   * Calls to save(), delete(), insert() and execute() all support batch
   * processing. This includes normal beans, CallableSql and UpdateSql.
   * </p>
   *
   * <pre>{@code
   *
   * try (Transaction transaction = database.beginTransaction()) {
   *
   *   // turn on JDBC batch
   *   transaction.setBatchMode(true);
   *
   *   // tune the batch size
   *   transaction.setBatchSize(50);
   *
   *   ...
   *
   *   transaction.commit();
   * }
   *
   * }</pre>
   *
   * <h3>getGeneratedKeys</h3>
   * <p>
   * Often with large batch inserts we want to turn off getGeneratedKeys. We do
   * this via {@link #setGetGeneratedKeys(boolean)}.
   * Also note that some JDBC drivers do not support getGeneratedKeys in JDBC batch mode.
   * </p>
   * <pre>{@code
   *
   * try (Transaction transaction = database.beginTransaction()) {
   *
   *   transaction.setBatchMode(true);
   *   transaction.setBatchSize(100);
   *   // insert but don't bother getting back the generated keys
   *   transaction.setBatchGetGeneratedKeys(false);
   *
   *
   *   // perform lots of inserts ...
   *   ...
   *
   *   transaction.commit();
   * }
   *
   * }</pre>
   *
   * <h3>Flush</h3>
   * <p>
   * The batch is automatically flushed when it hits the batch size and also when we
   * execute queries or when we mix UpdateSql and CallableSql with save and delete of
   * beans.
   * <p>
   * We use {@link #flush()} to explicitly flush the batch and we can use
   * {@link #setFlushOnQuery(boolean)} and {@link #setFlushOnMixed(boolean)}
   * to control the automatic flushing behaviour.
   * <p>
   * Example: batch processing of CallableSql executing every 10 rows
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
   * // (optional) inform Ebean this stored procedure
   * // inserts into a table called sp_test
   * cs.addModification("sp_test", true, false, false);
   *
   * try (Transaction txn = DB.beginTransaction()) {
   *   txn.setBatchMode(true);
   *   txn.setBatchSize(10);
   *
   *   for (int i = 0; i < da.length;) {
   *     cs.setParameter(1, da[i]);
   *     DB.execute(cs);
   *   }
   *
   *   // Note: commit implicitly flushes
   *   txn.commit();
   * }
   *
   * }</pre>
   */
  void setBatchMode(boolean useBatch);

  /**
   * Return the batch mode at the transaction level.
   */
  boolean isBatchMode();

  /**
   * Set the JDBC batch mode to use for a save() or delete() when cascading to children.
   * <p>
   * This only takes effect when batch mode on the transaction has not already meant that
   * JDBC batch mode is being used.
   * <p>
   * This is useful when the single save() or delete() cascades. For example, inserting a 'master' cascades
   * and inserts a collection of 'detail' beans. The detail beans can be inserted using JDBC batch.
   * <p>
   * This is effectively already turned on for all platforms apart from older Sql Server.
   *
   * @param batchMode the batch mode to use per save(), insert(), update() or delete()
   * @see io.ebean.config.DatabaseConfig#setPersistBatchOnCascade(PersistBatch)
   */
  void setBatchOnCascade(boolean batchMode);

  /**
   * Return the batch mode at the request level.
   */
  boolean isBatchOnCascade();

  /**
   * Specify the number of statements before a batch is flushed automatically.
   */
  void setBatchSize(int batchSize);

  /**
   * Return the current batch size.
   */
  int getBatchSize();

  /**
   * Specify if we want batched inserts to use getGeneratedKeys.
   * <p>
   * By default batched inserts will try to use getGeneratedKeys if it is
   * supported by the underlying jdbc driver and database.
   * <p>
   * We want to turn off getGeneratedKeys when we are inserting a large
   * number of objects and we don't care about getting back the ids. In this
   * way we avoid the extra cost of getting back the generated id values
   * from the database.
   * <p>
   * Note that when we do turn off getGeneratedKeys then we have the limitation
   * that after a bean has been inserted we are unable to then mutate the bean
   * and update it in the same transaction as we have not obtained it's id value.
   */
  void setGetGeneratedKeys(boolean getGeneratedKeys);

  /**
   * By default when mixing UpdateSql (or CallableSql) with Beans the batch is
   * automatically flushed when you change (between persisting beans and
   * executing UpdateSql or CallableSql).
   * <p>
   * If you want to execute both WITHOUT having the batch automatically flush
   * you need to call this with batchFlushOnMixed = false.
   * <p>
   * Note that UpdateSql and CallableSql are ALWAYS executed first (before the
   * beans are executed). This is because the UpdateSql and CallableSql have
   * already been bound to their PreparedStatements. The beans on the other hand
   * have a 2 step process (delayed binding).
   */
  void setFlushOnMixed(boolean batchFlushOnMixed);

  /**
   * By default executing a query will automatically flush any batched
   * statements (persisted beans, executed UpdateSql etc).
   * <p>
   * Calling this method with batchFlushOnQuery = false means that you can
   * execute a query and the batch will not be automatically flushed.
   */
  void setFlushOnQuery(boolean batchFlushOnQuery);

  /**
   * Return true if the batch (of persisted beans or executed UpdateSql etc)
   * should be flushed prior to executing a query.
   * <p>
   * The default is for this to be true.
   */
  boolean isFlushOnQuery();

  /**
   * The batch will be flushing automatically but you can use this to explicitly
   * flush the batch if you like.
   * <p>
   * Flushing occurs automatically when:
   * <ul>
   * <li>the batch size is reached</li>
   * <li>A query is executed on the same transaction</li>
   * <li>UpdateSql or CallableSql are mixed with bean save and delete</li>
   * <li>Transaction commit occurs</li>
   * <li>A getter method is called on a batched bean</li>
   * </ul>
   */
  void flush() throws PersistenceException;

  /**
   * Return the underlying Connection object.
   * <p>
   * Useful where a Developer wishes to use the JDBC API directly. Note that the
   * commit() rollback() and end() methods on the Transaction should still be
   * used. Calling these methods on the Connection would be a big no no unless
   * you know what you are doing.
   * <p>
   * Examples of when a developer may wish to use the connection directly are:
   * Savepoints, advanced CLOB BLOB use and advanced stored procedure calls.
   */
  Connection connection();

  /**
   * Add table modification information to the TransactionEvent.
   * <p>
   * Use this in conjunction with getConnection() and raw JDBC.
   * <p>
   * This effectively informs Ebean of the data that has been changed by the
   * transaction and this information is normally automatically handled by Ebean
   * when you save entity beans or use UpdateSql etc.
   * <p>
   * If you use raw JDBC then you can use this method to inform Ebean for the
   * tables that have been modified. Ebean uses this information to keep its
   * caches in synch and maintain text indexes.
   */
  void addModification(String tableName, boolean inserts, boolean updates, boolean deletes);

  /**
   * Add an arbitrary user object to the transaction. The objects added have no
   * impact on any internals of ebean and are solely meant as a convenient
   * method push user information (although somewhat replaced by TransactionCallback).
   */
  void putUserObject(String name, Object value);

  /**
   * Get an object added with {@link #putUserObject(String, Object)}.
   */
  Object getUserObject(String name);

  /**
   * In case of nested transaction, this returns the root transaction.
   * @return
   */
  default Transaction root() {
    return this;
  }

  /**
   * Return the persistence context associated with this transaction.
   * <p>
   * You may wish to hold onto this and set it against another transaction
   * later. This is along the lines of 'extended persistence context'
   * behaviour.
   */
  PersistenceContext persistenceContext();

}

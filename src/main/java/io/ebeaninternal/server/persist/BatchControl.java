package io.ebeaninternal.server.persist;

import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.core.PersistRequest;
import io.ebeaninternal.server.core.PersistRequestBean;
import io.ebeaninternal.server.core.PersistRequestUpdateSql;
import io.ebeaninternal.server.deploy.BeanDescriptor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * Controls the batch ordering of persist requests.
 * <p>
 * Persist requests include bean inserts updates deletes and UpdateSql and
 * CallableSql requests.
 * </p>
 * <p>
 * This object queues up the requests into appropriate entries according to the
 * 'depth' and the 'type' of the requests. The depth relates to how saves and
 * deletes cascade following the associations of a bean. For saving Associated
 * One cascades reduce the depth (-1) and associated many's increase the depth.
 * The initial depth of a request is 0.
 * </p>
 */
public final class BatchControl {

  private static final Object DUMMY = new Object();

  /**
   * Used to sort queue entries by depth.
   */
  private static final BatchDepthComparator depthComparator = new BatchDepthComparator();

  /**
   * Controls batching of the PreparedStatements. This should be flushed after
   * each 'depth'.
   */
  private final BatchedPstmtHolder pstmtHolder = new BatchedPstmtHolder();

  /**
   * Map of the BatchedBeanHolder objects. They each have a depth and are later
   * sorted by their depth to get the execution order.
   */
  private final HashMap<String, BatchedBeanHolder> beanHoldMap = new HashMap<>();

  /**
   * Set of beans in this batch. This is used to ensure that a single bean instance is not included
   * in the batch twice (two separate insert requests etc).
   */
  private final IdentityHashMap<Object, Object> persistedBeans = new IdentityHashMap<>();

  /**
   * Helper to determine statement ordering based on depth (and type).
   */
  private final BatchDepthOrder depthOrder = new BatchDepthOrder();

  private final SpiTransaction transaction;

  /**
   * The size at which the batch queue will flush. This should be close to the
   * number of statements that are batched into a single PreparedStatement. This
   * size relates to the size of a list in a BatchQueueEntry and not the total
   * number of request which could be more than that.
   */
  private int batchSize;

  /**
   * If true try to get generated keys from inserts.
   */
  private boolean getGeneratedKeys;

  private boolean batchFlushOnMixed = true;

  /**
   * Size of the largest buffer.
   */
  private int bufferMax;

  private Queue earlyQueue;
  private Queue lateQueue;

  /**
   * Create for a given transaction, PersistExecute, default size and getGeneratedKeys.
   */
  public BatchControl(SpiTransaction t, int batchSize, boolean getGenKeys) {
    this.transaction = t;
    this.batchSize = batchSize;
    this.getGeneratedKeys = getGenKeys;
    transaction.setBatchControl(this);
  }

  /**
   * Set this flag to false to allow batching of a mix of Beans and UpdateSql
   * (or CallableSql). Normally if you mix the two this will result in an
   * automatic flush.
   * <p>
   * Note that UpdateSql and CallableSql will ALWAYS flush first. This is due to
   * it already having been bound to a PreparedStatement where as the Beans go
   * through a 2 step process when they are flushed (delayed binding).
   * </p>
   */
  public void setBatchFlushOnMixed(boolean flushBatchOnMixed) {
    this.batchFlushOnMixed = flushBatchOnMixed;
  }

  /**
   * Set the size of batch execution.
   * <p>
   * The user can set this via the Transaction.
   * </p>
   */
  public void setBatchSize(int batchSize) {
    if (batchSize > 1) {
      this.batchSize = batchSize;
    }
  }

  /**
   * Set whether or not to use getGeneratedKeys for this batch execution.
   * <p>
   * The user can set this via the transaction
   * </p>
   */
  public void setGetGeneratedKeys(Boolean getGeneratedKeys) {
    if (getGeneratedKeys != null) {
      this.getGeneratedKeys = getGeneratedKeys;
    }
  }

  /**
   * Execute a Orm Update, SqlUpdate or CallableSql.
   * <p>
   * These all go straight to jdbc and use addBatch(). Entity beans goto a queue
   * and wait there so that the jdbc is executed in the correct order according
   * to the depth.
   * </p>
   */
  public int executeStatementOrBatch(PersistRequest request, boolean batch) throws BatchedSqlException {
    if (!batch || (batchFlushOnMixed && !isBeansEmpty())) {
      // flush when mixing beans and updateSql
      flush();
    }
    if (!batch) {
      // execute the request immediately without batching
      return request.executeNow();
    }

    if (pstmtHolder.getMaxSize() >= batchSize) {
      flush();
    }
    // for OrmUpdate, SqlUpdate, CallableSql there is no queue...
    // so straight to jdbc prepared statement and use addBatch().
    // aka executeNow() may use addBatch().
    request.executeNow();
    return -1;
  }

  /**
   * Entity Bean insert, update or delete. This will either execute the request
   * immediately or queue it for batch processing later. The queue is flushedIntercept
   * according to the depth (object graph depth).
   */
  public int executeOrQueue(PersistRequestBean<?> request, boolean batch) throws BatchedSqlException {

    if (!batch || (batchFlushOnMixed && !pstmtHolder.isEmpty())) {
      // flush when mixing beans and updateSql
      flush();
    }
    if (!batch) {
      return request.executeNow();
    }
    if (addToBatch(request)) {
      // flush as the top level has hit the batch size
      flush();
    }
    return -1;
  }

  /**
   * Add the request to the batch and return true if we should flush.
   */
  private boolean addToBatch(PersistRequestBean<?> request) {

    Object alreadyInBatch = persistedBeans.put(request.getEntityBean(), DUMMY);
    if (alreadyInBatch != null) {
      // special case where the same bean instance has already been
      // added to the batch (doesn't really occur with non-batching
      // as the bean gets changed from dirty to loaded earlier)
      return false;
    }

    BatchedBeanHolder beanHolder = getBeanHolder(request);
    int bufferSize = beanHolder.append(request);

    bufferMax = Math.max(bufferMax, bufferSize);
    // flush if any buffer hits 10 times batch size
    return (bufferMax >= batchSize * 10);
  }

  /**
   * Return the actual batch of PreparedStatements.
   */
  public BatchedPstmtHolder getPstmtHolder() {
    return pstmtHolder;
  }

  /**
   * Return true if the queue is empty.
   */
  public boolean isEmpty() {
    return (isBeansEmpty() && pstmtHolder.isEmpty());
  }

  /**
   * Flush any batched PreparedStatements.
   */
  private void flushPstmtHolder() throws BatchedSqlException {
    pstmtHolder.flush(getGeneratedKeys);
  }

  /**
   * Execute all the requests contained in the list.
   */
  void executeNow(ArrayList<PersistRequest> list) throws BatchedSqlException {
    for (int i = 0; i < list.size(); i++) {
      if (i % batchSize == 0) {
        // hit the batch size so flush
        flushPstmtHolder();
      }
      list.get(i).executeNow();
    }
    flushPstmtHolder();
  }

  /**
   * Flush without resetting the depth info.
   */
  public void flush() throws BatchedSqlException {
    flushBuffer(false);
  }

  /**
   * Flush with a reset of the depth info.
   */
  public void flushReset() throws BatchedSqlException {
    flushBuffer(true);
  }

  /**
   * Clears the batch, discarding all batched statements.
   */
  public void clear() {
    pstmtHolder.clear();
    beanHoldMap.clear();
    depthOrder.clear();
    persistedBeans.clear();
  }

  private void flushBuffer(boolean resetTop) throws BatchedSqlException {
    flushInternal(resetTop);
    flushQueue(earlyQueue);
    flushQueue(lateQueue);
  }

  private void flushQueue(Queue queue) throws BatchedSqlException {
    if (queue != null && queue.flush() && !pstmtHolder.isEmpty()) {
      flushPstmtHolder();
    }
  }

  /**
   * execute all the requests currently queued or batched.
   */
  private void flushInternal(boolean resetTop) throws BatchedSqlException {

    try {
      bufferMax = 0;
      if (!pstmtHolder.isEmpty()) {
        // Flush existing pstmts (updateSql or callableSql)
        flushPstmtHolder();
      }
      if (isEmpty()) {
        // Nothing in queue to flush
        return;
      }

      // convert entry map to array for sorting
      BatchedBeanHolder[] bsArray = getBeanHolderArray();
      // sort the entries by depth
      Arrays.sort(bsArray, depthComparator);

      if (transaction.isLogSummary()) {
        transaction.logSummary("BatchControl flush " + Arrays.toString(bsArray));
      }
      for (BatchedBeanHolder beanHolder : bsArray) {
        beanHolder.executeNow();
      }
      persistedBeans.clear();
      if (resetTop) {
        beanHoldMap.clear();
        depthOrder.clear();
      }
    } catch (BatchedSqlException e) {
      // clear the batch on error in case we want to
      // catch, rollback and continue processing
      clear();
      throw e;
    }
  }

  /**
   * Return an entry for the given type description. The type description is
   * typically the bean class name (or table name for MapBeans).
   */
  private BatchedBeanHolder getBeanHolder(PersistRequestBean<?> request) {

    int depth = transaction.depth();
    BeanDescriptor<?> desc = request.getBeanDescriptor();

    // batching by bean type AND depth
    String key = desc.rootName() + ":" + depth;

    BatchedBeanHolder batchBeanHolder = beanHoldMap.get(key);
    if (batchBeanHolder == null) {
      int ordering = depthOrder.orderingFor(depth);
      batchBeanHolder = new BatchedBeanHolder(this, desc, ordering);
      beanHoldMap.put(key, batchBeanHolder);
    }
    return batchBeanHolder;
  }

  /**
   * Return true if this holds no persist requests.
   */
  private boolean isBeansEmpty() {
    return persistedBeans.isEmpty();
  }

  /**
   * Return the BatchedBeanHolder's ready for sorting and executing.
   */
  private BatchedBeanHolder[] getBeanHolderArray() {
    return beanHoldMap.values().toArray(new BatchedBeanHolder[0]);
  }

  /**
   * Execute a batched statement.
   */
  public int[] execute(String key, boolean getGeneratedKeys) throws SQLException {
    return pstmtHolder.execute(key, getGeneratedKeys);
  }

  /**
   * Add a SqlUpdate request to execute after flush.
   */
  public void addToFlushQueue(PersistRequestUpdateSql request, boolean early) {
    if (early) {
      // add it to the early queue
      if (earlyQueue == null) {
        earlyQueue = new Queue();
      }
      earlyQueue.add(request);
    } else {
      // add it to the late queue
      if (lateQueue == null) {
        lateQueue = new Queue();
      }
      lateQueue.add(request);
    }
  }

  private static class Queue {

    private final List<PersistRequestUpdateSql> queue = new ArrayList<>();

    boolean flush() {
      if (queue.isEmpty()) {
        return false;
      }
      for (PersistRequestUpdateSql request : queue) {
        request.executeAddBatch();
      }
      queue.clear();
      return true;
    }

    void add(PersistRequestUpdateSql request) {
      queue.add(request);
    }
  }
}

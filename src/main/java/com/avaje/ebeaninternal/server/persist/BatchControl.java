package com.avaje.ebeaninternal.server.persist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.persistence.PersistenceException;

import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.PersistRequest;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

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
  private final HashMap<String, BatchedBeanHolder> beanHoldMap = new HashMap<String, BatchedBeanHolder>();

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
   * Return the batchSize.
   */
  public int getBatchSize() {
    return batchSize;
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
  public int executeStatementOrBatch(PersistRequest request, boolean batch) {
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
   * immediately or queue it for batch processing later. The queue is flushed
   * according to the depth (object graph depth).
   */
  public int executeOrQueue(PersistRequestBean<?> request, boolean batch) {

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

    BatchedBeanHolder beanHolder = getBeanHolder(request);
    int bufferSize = beanHolder.append(request);

    // return true if top level has hit batch size
    return bufferSize == batchSize && beanHolder.getOrder() == 100;
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
  protected void flushPstmtHolder() {
    pstmtHolder.flush(getGeneratedKeys);
  }

  /**
   * Execute all the requests contained in the list.
   */
  protected void executeNow(ArrayList<PersistRequest> list) {
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
   * Flush without resetting the topOrder (maintains the depth info).
   */
  public void flush() throws PersistenceException {
    flush(false);
  }

  /**
   * Flush with a reset the topOrder (fully empty the batch).
   */
  public void flushReset() throws PersistenceException {
    flush(true);
  }

  /**
   * execute all the requests currently queued or batched.
   */
  private void flush(boolean resetTop) throws PersistenceException {

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
    for (int i = 0; i < bsArray.length; i++) {
      bsArray[i].executeNow();
    }

    if (resetTop) {
      beanHoldMap.clear();
    }
  }

  /**
   * Return an entry for the given type description. The type description is
   * typically the bean class name (or table name for MapBeans).
   */
  private BatchedBeanHolder getBeanHolder(PersistRequestBean<?> request) {

    BeanDescriptor<?> beanDescriptor = request.getBeanDescriptor();
    BatchedBeanHolder batchBeanHolder = beanHoldMap.get(beanDescriptor.getFullName());
    if (batchBeanHolder == null) {
      int relativeDepth = transaction.depth();
      if (relativeDepth == 0 && !beanHoldMap.isEmpty()) {
        // flush and reset the batch as we are changing the type of our top level
        // bean so just keep it simple and flush and reset the top
        flushReset();
      }

      batchBeanHolder = new BatchedBeanHolder(this, beanDescriptor, 100 + relativeDepth);
      beanHoldMap.put(beanDescriptor.getFullName(), batchBeanHolder);
    }
    return batchBeanHolder;
  }

  /**
   * Return true if this holds no persist requests.
   */
  private boolean isBeansEmpty() {
    if (beanHoldMap.isEmpty()) {
      return true;
    }
    for (BatchedBeanHolder beanHolder : beanHoldMap.values()) {
      if (!beanHolder.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Return the BatchedBeanHolder's ready for sorting and executing.
   */
  private BatchedBeanHolder[] getBeanHolderArray() {
    return beanHoldMap.values().toArray(new BatchedBeanHolder[beanHoldMap.size()]);
  }
}

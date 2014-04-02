package com.avaje.ebeaninternal.server.persist;

import java.util.ArrayList;
import java.util.Arrays;

import javax.persistence.PersistenceException;

import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.PersistRequest;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static final Logger logger = LoggerFactory.getLogger(BatchControl.class);
    
  /**
   * Used to sort queue entries by depth.
   */
  private static final BatchDepthComparator depthComparator = new BatchDepthComparator();

  /**
   * The associated transaction.
   */
  private final SpiTransaction transaction;

  /**
   * Controls batching of the PreparedStatements. This should be flushed after
   * each 'depth'.
   */
  private final BatchedPstmtHolder pstmtHolder = new BatchedPstmtHolder();

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

  private final BatchedBeanControl beanControl;

  /**
   * Create for a given transaction, PersistExecute, default size and
   * getGeneratedKeys.
   */
  public BatchControl(SpiTransaction t, int batchSize, boolean getGenKeys) {
    this.transaction = t;
    this.batchSize = batchSize;
    this.getGeneratedKeys = getGenKeys;
    this.beanControl = new BatchedBeanControl(t, this);
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
    if (!batch || (batchFlushOnMixed && !beanControl.isEmpty())) {
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

    // get the list we will add this request to
    ArrayList<PersistRequest> persistList = beanControl.getPersistList(request);
    if (persistList == null) {
      // special case where the same bean instance has been added
      // to the batch more than once
      if (logger.isDebugEnabled()) {
        logger.debug("Bean instance already in this batch: " + request.getEntityBean());
      }
      return -1;
    }

    if (persistList.size() >= batchSize) {
      // flush everything that has been batched
      flush();

      // we need to get the persistList again after the
      // flush as the flush clears out the bean holders
      persistList = beanControl.getPersistList(request);
    }

    persistList.add(request);
    return -1;
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
    return (beanControl.isEmpty() && pstmtHolder.isEmpty());
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
      list.get(i).executeNow();
    }
  }

  /**
   * execute all the requests currently queued or batched.
   */
  public void flush() throws PersistenceException {

    if (!pstmtHolder.isEmpty()) {
      // Flush existing pstmts (updateSql or callableSql)
      flushPstmtHolder();
    }
    if (beanControl.isEmpty()) {
      // Nothing in queue to flush
      return;
    }

    // convert entry map to array for sorting
    BatchedBeanHolder[] bsArray = beanControl.getArray();

    // sort the entries by depth
    Arrays.sort(bsArray, depthComparator);

    if (transaction.isLogSummary()) {
      transaction.logSummary("BatchControl flush " + Arrays.toString(bsArray));
    }
    for (int i = 0; i < bsArray.length; i++) {
      BatchedBeanHolder bs = bsArray[i];
      bs.executeNow();
      // flush all the batched Pstmts
      flushPstmtHolder();
    }
  }

}

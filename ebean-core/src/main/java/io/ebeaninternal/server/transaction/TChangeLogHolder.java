package io.ebeaninternal.server.transaction;

import io.ebean.event.changelog.BeanChange;
import io.ebean.event.changelog.ChangeSet;
import io.ebean.event.changelog.TxnState;
import io.ebeaninternal.api.SpiTransaction;

import java.util.UUID;

/**
 * Holder of the changes handling the case when we send the changes
 * prior to commit or rollback as we hit the allowed 'batch size'.
 */
public class TChangeLogHolder {

  /**
   * The owning transaction.
   */
  private final SpiTransaction owner;

  /**
   * A transaction id that can be used to join many changeSets when
   * we send multiple for a large transaction.
   */
  private final String transactionId;

  /**
   * When we hit batch size then send the changeSet even when the tranaction
   * has not yet completed.
   */
  private final int batchSize;

  /**
   * The changes we collect to send to the listener.
   */
  private ChangeSet changes;

  private long batchId;

  /**
   * Counter to check when we hit the batch size.
   */
  private int count;

  /**
   * Construct with the owning transaction and batch size to use.
   */
  public TChangeLogHolder(SpiTransaction owner, int batchSize) {
    this.owner = owner;
    this.transactionId = UUID.randomUUID().toString();
    this.batchSize = batchSize;
    this.changes = new ChangeSet(transactionId, 0);
  }

  /**
   * Add a bean change to the change set.
   */
  public void addBeanChange(BeanChange change) {

    changes.addBeanChange(change);
    if (++count >= batchSize) {
      // we hit the batch size so send what we have knowing
      // that the transaction has not completed yet and
      // reset the changes and count
      owner.sendChangeLog(changes);
      changes = new ChangeSet(transactionId, ++batchId);
      count = 0;
    }
  }

  /**
   * On post commit send the changes we have collected.
   */
  public void postCommit() {
    changes.setTxnState(TxnState.COMMITTED);
    owner.sendChangeLog(changes);
  }

  /**
   * On post rollback send the changes we have collected and
   * leave it up to the listener to decide what to do.
   */
  public void postRollback() {
    changes.setTxnState(TxnState.ROLLBACK);
    owner.sendChangeLog(changes);
  }

}

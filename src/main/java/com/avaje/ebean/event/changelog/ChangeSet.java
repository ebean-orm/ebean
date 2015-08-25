package com.avaje.ebean.event.changelog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds a set of changes.
 */
public class ChangeSet {

  /**
   * A UUID transaction id specifically created for the change set.
   */
  String txnId;

  /**
   * For large transactions with many change sets this is an incrementing counter.
   */
  long txnBatch;

  /**
   * The state of the transaction (change sets can be sent prior to commit or rollback
   * with large transactions).
   */
  TxnState txnState;

  /**
   * User defined 'source' such as the application name.
   */
  String source;

  /**
   * Application user id expected to be optionally populated by ChangeLogPrepare.
   */
  String userId;

  /**
   * Application user ip address expected to be optionally populated by ChangeLogPrepare.
   */
  String userIpAddress;

  /**
   * Arbitrary user context information expected to be optionally populated by ChangeLogPrepare.
   */
  Map<String,String> userContext;

  /**
   * The bean changes.
   */
  List<BeanChange> changes = new ArrayList<BeanChange>();

  /**
   * Construct with a txnId.
   */
  public ChangeSet(String txnId, long txnBatch) {
    this.txnId = txnId;
    this.txnBatch = txnBatch;
    this.txnState = TxnState.IN_PROGRESS;
  }

  /**
   * Default constructor for JSON tools.
   */
  public ChangeSet() {
  }

  public String toString() {
    return "txnId:" + txnId + " txnState:" + txnState + " txnBatch:" + txnBatch;
  }

  /**
   * Return the number of changes in the change set.
   */
  public int size() {
    return changes.size();
  }

  /**
   * Add a bean change to the change set.
   */
  public void addBeanChange(BeanChange beanChange) {
    changes.add(beanChange);
  }

  /**
   * Return the txnId.
   */
  public String getTxnId() {
    return txnId;
  }

  /**
   * Set the txnId (used by JSON tools).
   */
  public void setTxnId(String txnId) {
    this.txnId = txnId;
  }

  /**
   * Returns the batch id.
   */
  public long getTxnBatch() {
    return txnBatch;
  }

  /**
   * Sets the batch id (used by JSON tools).
   */
  public void setTxnBatch(long txnBatch) {
    this.txnBatch = txnBatch;
  }

  /**
   * Return the transaction state. This will be IN_PROGRESS for many changeSets in large transactions
   * as the changeSets are sent in batches before the transaction has completed.
   */
  public TxnState getTxnState() {
    return txnState;
  }

  /**
   * Set the state (used by JSON tools).
   */
  public void setTxnState(TxnState txnState) {
    this.txnState = txnState;
  }

  /**
   * Return a code that identifies the source of the change (like the name of the application).
   */
  public String getSource() {
    return source;
  }

  /**
   * Set the source of the change (like the name of the application).
   */
  public void setSource(String source) {
    this.source = source;
  }

  /**
   * Return the application user Id.
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Set the application user Id.
   * <p>
   * This can be set by the ChangeLogListener in the prepare() method which is called
   * in the foreground thread.
   * </p>
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Return the application users ip address.
   */
  public String getUserIpAddress() {
    return userIpAddress;
  }

  /**
   * Set the application users ip address.
   * <p>
   * This can be set by the ChangeLogListener in the prepare() method which is called
   * in the foreground thread.
   * </p>
   */
  public void setUserIpAddress(String userIpAddress) {
    this.userIpAddress = userIpAddress;
  }

  /**
   * Return a user context value - anything you set yourself in ChangeLogListener prepare().
   */
  public Map<String,String> getUserContext() {
    if (userContext == null) {
      userContext = new LinkedHashMap<String, String>();
    }
    return userContext;
  }

  /**
   * Set a user context value (anything you like).
   * <p>
   * This can be set by the ChangeLogListener in the prepare() method which is called
   * in the foreground thread.
   * </p>
   */
  public void setUserContext(Map<String,String> userContext) {
    this.userContext = userContext;
  }

  /**
   * Return the bean changes.
   */
  public List<BeanChange> getChanges() {
    return changes;
  }

  /**
   * Set the bean changes (used by JSON tools).
   */
  public void setChanges(List<BeanChange> changes) {
    this.changes = changes;
  }
}

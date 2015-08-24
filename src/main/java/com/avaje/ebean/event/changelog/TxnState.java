package com.avaje.ebean.event.changelog;

/**
 * Transaction state when ChangeSets are sent to the ChangeSetListener.
 * <p>
 * For large transactions multiple ChangeSets can be sent in a batch fashion and in this
 * case all but the last changeSet with have IN_PROGRESS state and the last changeSet will
 * have the COMMITTED or ROLLBACK state.
 * </p>
 */
public enum TxnState {

  /**
   * The Transaction is still in progress.
   * <p>
   * Used when the transaction is large/long and Ebean wants to send out the changeSets
   * in batches and a changeSet is send before the transaction has completed.
   */
  IN_PROGRESS("I"),

  /**
   * The Transaction was committed.
   */
  COMMITTED("C"),

  /**
   * The Transaction was rolled back.
   */
  ROLLBACK("R");

  private final String code;

  TxnState(String code) {
    this.code = code;
  }

  /**
   * Return the short code for the transaction state.
   * <p>
   * C - Committed, R - Rollback and I for In progress.
   * </p>
   */
  public String getCode() {
    return code;
  }
}

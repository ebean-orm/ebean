package com.avaje.ebean;

/**
 * Adapter that can be extended for easier implementation of TransactionCallback.
 * <p/>
 * Provides 'no operation' implementation for each of the TransactionCallback methods. It is expected that this
 * class is extended and override the methods you need to.
 */
public abstract class TransactionCallbackAdapter implements TransactionCallback {

  /**
   * Perform processing just prior to the transaction commit.
   */
  @Override
  public void preCommit() {
    // do nothing - override as necessary
  }

  /**
   * Perform processing just after the transaction commit.
   */
  @Override
  public void postCommit() {
    // do nothing - override as necessary
  }

  /**
   * Perform processing just prior to the transaction rollback.
   */
  @Override
  public void preRollback() {
    // do nothing - override as necessary
  }

  /**
   * Perform processing just after the transaction rollback.
   */
  @Override
  public void postRollback() {
    // do nothing - override as necessary
  }
}

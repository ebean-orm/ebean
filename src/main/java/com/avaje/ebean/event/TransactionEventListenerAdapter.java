package com.avaje.ebean.event;

import com.avaje.ebean.Transaction;

/**
 * A no operation implementation of TransactionEventListener. Objects extending
 * this need to only override the methods they want to.
 */
public abstract class TransactionEventListenerAdapter implements TransactionEventListener {

  public void postTransactionCommit(Transaction tx) {
    // do nothing by default
  }

  public void postTransactionRollback(Transaction tx, Throwable cause) {
    // do nothing by default
  }
}

package com.avaje.tests.basic.event;

import com.avaje.ebean.Transaction;
import com.avaje.ebean.event.TransactionEventListener;

public class MyTestTransactionEventListener implements TransactionEventListener {
  private volatile static boolean doTest = false;

  private static Transaction lastCommitted;
  private static Transaction lastRollbacked;

  public void postTransactionCommit(Transaction tx) {
    if (!doTest) {
      return;
    }

    lastCommitted = tx;
  }

  public void postTransactionRollback(Transaction tx, Throwable cause) {
    if (!doTest) {
      return;
    }

    lastRollbacked = tx;
  }

  public static void setDoTest(boolean doTest) {
    MyTestTransactionEventListener.doTest = doTest;

    // reset what we've recorded so far
    lastCommitted = null;
    lastRollbacked = null;
  }

  public static Transaction getLastCommitted() {
    return lastCommitted;
  }

  public static Transaction getLastRollbacked() {
    return lastRollbacked;
  }
}

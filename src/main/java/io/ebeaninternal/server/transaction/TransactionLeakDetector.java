package io.ebeaninternal.server.transaction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.ebeaninternal.api.ScopedTransaction;
import io.ebeaninternal.api.SpiTransaction;

/**
 * Detects inproper closed transactions (= code that leaves a transaction
 * open, because transaction is not properly closed in finally block).
 *
 * This is requred to avoid class loader / memory leaks in an application
 * server.
 *
 * Internally, there is a concurrentHashMap that maintains all leakInfos when
 * detection is enabeld. It stores the HashMap per thread name of all
 * transactions per server. This may cause some overhead, but gives us the
 * ability to clear the dangling HashMaps on shutdown, so that the GC can
 * perform a proper cleanup.
 *
 * If detection is disabled, a sinple counter is maintained, to keep the
 * overhead low.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
class TransactionLeakDetector {

  private static final Logger logger = LoggerFactory.getLogger(TransactionLeakDetector.class);

  private boolean detectTransactionLeaks = Boolean.getBoolean("ebean.detectTransactionLeaks");

  private final Map<String, TransactionLeakInfo> txnLeakInfos = new ConcurrentHashMap<>();

  private final LongAdder counter = new LongAdder();

  private boolean foundLeak;

  private static class TransactionLeakInfo {
    private Object[] holder;
    private Exception origin = new Exception();
    private String threadName;

    private TransactionLeakInfo(String threadName, Object[] holder) {
      this.threadName = threadName;
      this.holder = holder;
    }

    private void process() {
      logger.error("transaction leak: {}, created by thread {}", holder[0], threadName, origin);
      holder[0] = null;
    }
  }

  void remove() {
    if (detectTransactionLeaks) {
      txnLeakInfos.remove(Thread.currentThread().getName());
    } else {
      counter.decrement();
    }
  }

  void set(Object[] holder) {
    if (detectTransactionLeaks) {
      String threadName = Thread.currentThread().getName();
      txnLeakInfos.put(threadName, new TransactionLeakInfo(threadName, holder));
    } else {
      counter.increment();
    }
  }

  /**
   * Try to clean all stuck transactions and log them.
   *
   * Returns true, if transaction leaks were found.
   */
  boolean detectLeaks() {
    if (!txnLeakInfos.isEmpty()) {
      logger.error("There are transaction leaks. Trying to clean them up");
      txnLeakInfos.values().forEach(TransactionLeakInfo::process);
    } else if (counter.sum() > 0) {
      logger.error("There are {} transaction leaks, but detection is disabled", counter);
    } else if (foundLeak) {
      logger.error("Threre was at least one transaction leak found. Check the log.");
    } else {
      logger.info("No transaction leaks detected");
      return false;
    }
    txnLeakInfos.clear();
    counter.reset();
    foundLeak = false;
    return true;
  }

  /**
   * Detects possible transaction leaks when replacing an existing transaction.
   *
   * If an existing inactive transaction (which is not the NoTransaction) should
   * be replaced by a new transaction, this means the existing transaction was not
   * ended correctly.
   */
  void replaceTransaction(SpiTransaction existing, SpiTransaction newTransaction) {
    if (newTransaction == null) {
      return; // replacing with empty is always allowed
    }
    if (existing instanceof ScopedTransaction) {
      if (((ScopedTransaction)existing).current() instanceof NoTransaction) {
        // the NoTransaction was never active. So this would be OK, if we
        // replace that completely
        return;
      }
    }
    foundLeak = true;
    if (detectTransactionLeaks) {
      TransactionLeakInfo leak = txnLeakInfos.get(Thread.currentThread().getName());
      logger.error("Found a transaction leak: {} Origin: ", existing, leak == null ? null : leak.origin);
    } else {
      logger.error("Found a transaction leak: {}, enable detection for more infos.", existing);
    }
  }
}

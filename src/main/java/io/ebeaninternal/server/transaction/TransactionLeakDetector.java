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
public class TransactionLeakDetector {

  private static final Logger logger = LoggerFactory.getLogger(TransactionLeakDetector.class);

  private final boolean details;

  private final Map<String, TransactionLeakInfo> txnLeakInfos = new ConcurrentHashMap<>();

  private final LongAdder counter = new LongAdder();

  private boolean foundLeak;

  public TransactionLeakDetector(boolean details) {
    this.details = details;
  }

  private static class TransactionLeakInfo {
    private Object[] holder;
    private Throwable origin;
    private String threadName;

    private TransactionLeakInfo(String threadName, Object[] holder) {
      this.threadName = threadName;
      this.holder = holder;
      this.origin = new Throwable("Origin of transaction leak:");
    }

    private void process() {
      logger.error("transaction leak: '{}', created by '{}' ", holder[0], threadName, origin);
      holder[0] = null;
    }
  }

  void remove() {
    if (details) {
      txnLeakInfos.remove(Thread.currentThread().getName());
    } else {
      counter.decrement();
    }
  }

  void set(Object[] old, Object[] holder) {
    if (old != null) {
      checkLeak((SpiTransaction)old[0], (SpiTransaction)holder[0]);
    }
    if (details) {
      String threadName = Thread.currentThread().getName();
      txnLeakInfos.put(threadName, new TransactionLeakInfo(threadName, holder));
    } else {
      counter.increment();
    }
  }

  /**
   * Check if we replace an old transaction (which is not the NoTransaction)
   */
  private void checkLeak(SpiTransaction oldTrans, SpiTransaction newTrans) {
    if (oldTrans == null) {
      return;
    } else if (oldTrans instanceof NoTransaction) {
      return;
    } else if (oldTrans instanceof ScopedTransaction) {
      ScopedTransaction scopedTrans = (ScopedTransaction) oldTrans;
      if (scopedTrans.current() instanceof NoTransaction) {
        return;
      }
    }
    logger.error("Possible transaction leak: Replacing {} with {}", oldTrans, newTrans);
    foundLeak = true;
    if (details) {
      TransactionLeakInfo leakInfo = txnLeakInfos.get(Thread.currentThread().getName());
      if (leakInfo != null) {
        leakInfo.process();
      }
    }
  }

  /**
   * Try to clean all stuck transactions and log them.
   *
   * Returns true, if transaction leaks were found.
   */
  boolean detectLeaks(String servername) {
    if (!txnLeakInfos.isEmpty()) {
      logger.error("There are transaction leaks in '{}'. Trying to clean them up", servername);
      txnLeakInfos.values().forEach(TransactionLeakInfo::process);
    } else if (counter.sum() != 0) {
      // if the counter is negative, this means we have an error in the code
      logger.error("{} transaction leak(s) found in '{}'. Enable TransactionLeakDetection.DETAIL for more infos", counter, servername);
    } else if (foundLeak) {
      logger.error("Threre was at least one transaction leak found in '{}'. Check the log.", servername);
    } else {
      logger.info("No transaction leaks detected in '{}'", servername);
      return false;
    }
    txnLeakInfos.clear();
    counter.reset();
    foundLeak = false;
    return true;
  }

}

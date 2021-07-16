package io.ebeaninternal.server.querydefn;

import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.PersistenceException;

import io.ebean.CancelableQuery;
import io.ebeaninternal.api.SpiCancelableQuery;

/**
 * Common code for Dto/Orm/RelationalQuery
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public class AbstractQuery implements SpiCancelableQuery {

  private boolean cancelled;

  private CancelableQuery cancelableQuery;

  private final ReentrantLock lock = new ReentrantLock();
  
  @Override
  public void cancel() {
    lock.lock();
    try {
      if (!cancelled) {
        cancelled = true;
        if (cancelableQuery != null) {
          cancelableQuery.cancel();
        }
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void checkCancelled() {
    if (cancelled) {
      throw new PersistenceException("Query was cancelled");
    }
  }

  @Override
  public void setCancelableQuery(CancelableQuery cancelableQuery) {
    lock.lock();
    try {
      checkCancelled();
      this.cancelableQuery = cancelableQuery;
    } finally {
      lock.unlock();
    }
  }
}

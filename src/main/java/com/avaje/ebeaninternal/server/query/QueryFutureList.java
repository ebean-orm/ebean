package com.avaje.ebeaninternal.server.query;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.avaje.ebean.FutureList;
import com.avaje.ebean.Query;
import com.avaje.ebean.Transaction;

import javax.persistence.PersistenceException;

/**
 * Default implementation for FutureList.
 */
public class QueryFutureList<T> extends BaseFuture<List<T>> implements FutureList<T> {

	private final CallableQueryList<T> call;
	
	public QueryFutureList(CallableQueryList<T> call) {
		super(new FutureTask<List<T>>(call));
		this.call = call;
	}
	
	public FutureTask<List<T>> getFutureTask() {
	  return futureTask;
	}
	
	public Transaction getTransaction() {
	  return call.transaction;
	}

  @Override
	public Query<T> getQuery() {
		return call.query;
	}

  @Override
	public boolean cancel(boolean mayInterruptIfRunning) {
	  call.query.cancel();
		return super.cancel(mayInterruptIfRunning);
	}

  @Override
  public List<T> getUnchecked()  {
    try {
      return get();
    } catch (InterruptedException e) {
      throw new PersistenceException(e);
    } catch (ExecutionException e) {
      throw new PersistenceException(e);
    }
  }

  @Override
  public List<T> getUnchecked(long timeout, TimeUnit unit) throws TimeoutException {
    try {
      return get(timeout, unit);
    } catch (InterruptedException e) {
      throw new PersistenceException(e);
    } catch (ExecutionException e) {
      throw new PersistenceException(e);
    }
  }
	
}

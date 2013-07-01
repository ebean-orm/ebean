package com.avaje.ebeaninternal.server.query;

import java.util.List;
import java.util.concurrent.FutureTask;

import com.avaje.ebean.FutureList;
import com.avaje.ebean.Query;
import com.avaje.ebean.Transaction;

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
	
	public Query<T> getQuery() {
		return call.query;
	}

	public boolean cancel(boolean mayInterruptIfRunning) {
	  call.query.cancel();
		return super.cancel(mayInterruptIfRunning);
	}

	
}

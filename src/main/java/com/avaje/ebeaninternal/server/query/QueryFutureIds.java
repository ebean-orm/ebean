package com.avaje.ebeaninternal.server.query;

import java.util.List;
import java.util.concurrent.FutureTask;

import com.avaje.ebean.FutureIds;
import com.avaje.ebean.Query;
import com.avaje.ebean.Transaction;

/**
 * Default implementation of FutureIds.
 */
public class QueryFutureIds<T> extends BaseFuture<List<Object>> implements FutureIds<T> {

	private final CallableQueryIds<T> call;
	
	public QueryFutureIds(CallableQueryIds<T> call ) {
		super(new FutureTask<List<Object>>(call));
		this.call = call;
	}
	
	public FutureTask<List<Object>> getFutureTask() {
	  return futureTask;
	}
	
	public Transaction getTransaction() {
	  return call.transaction;
	}
	
	public Query<T> getQuery() {
		return call.query;
	}
	
	public List<Object> getPartialIds() {
		return call.query.getIdList();
	}

	public boolean cancel(boolean mayInterruptIfRunning) {
	  call.query.cancel();
		return super.cancel(mayInterruptIfRunning);
	}

}

package com.avaje.ebeaninternal.server.query;

import java.util.List;
import java.util.concurrent.FutureTask;

import com.avaje.ebean.FutureList;
import com.avaje.ebean.Query;

/**
 * Default implementation for FutureList.
 */
public class QueryFutureList<T> extends BaseFuture<List<T>> implements FutureList<T> {

	private final Query<T> query;
	
	
	public QueryFutureList(Query<T> query, FutureTask<List<T>> futureTask) {
		super(futureTask);
		this.query = query;
	}
	
	public Query<T> getQuery() {
		return query;
	}

	public boolean cancel(boolean mayInterruptIfRunning) {
		query.cancel();
		return super.cancel(mayInterruptIfRunning);
	}

	
}

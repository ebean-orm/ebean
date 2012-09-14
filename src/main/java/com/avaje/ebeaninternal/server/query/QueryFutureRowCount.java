package com.avaje.ebeaninternal.server.query;

import java.util.concurrent.FutureTask;

import com.avaje.ebean.FutureRowCount;
import com.avaje.ebean.Query;

/**
 * Future implementation for the row count query.
 */
public class QueryFutureRowCount<T> extends BaseFuture<Integer> implements FutureRowCount<T> {

	private final Query<T> query;
	
	public QueryFutureRowCount(Query<T> query, FutureTask<Integer> futureTask) {
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

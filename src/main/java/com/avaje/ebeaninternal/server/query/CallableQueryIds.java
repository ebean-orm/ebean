package com.avaje.ebeaninternal.server.query;

import java.util.List;
import java.util.concurrent.Callable;

import com.avaje.ebean.Query;
import com.avaje.ebean.Transaction;
import com.avaje.ebeaninternal.api.SpiEbeanServer;

/**
 * Represent the fetch Id's query as a Callable.
 *
 * @param <T> the entity bean type
 */
public class CallableQueryIds<T> extends CallableQuery<T> implements Callable<List<Object>> {

	
	public CallableQueryIds(SpiEbeanServer server, Query<T> query, Transaction t) {
		super(server, query, t);
	}
	
	/**
	 * Execute the find Id's query returning the list of Id's.
	 */
	public List<Object> call() throws Exception {
		// we have already made a copy of the query
		// this way the same query instance is available to the
		// QueryFutureIds (as so has access to the List before it is done)
		return server.findIdsWithCopy(query, t);
	}

}

package com.avaje.ebeaninternal.server.query;

import java.util.concurrent.Callable;

import com.avaje.ebean.Query;
import com.avaje.ebean.Transaction;
import com.avaje.ebeaninternal.api.SpiEbeanServer;

/**
 * Represent the findRowCount query as a Callable.
 *
 * @param <T> the entity bean type
 */
public class CallableQueryRowCount<T> extends CallableQuery<T> implements Callable<Integer> {

	
	public CallableQueryRowCount(SpiEbeanServer server, Query<T> query, Transaction t) {
		super(server, query, t);
	}
	
	/**
	 * Execute the query returning the row count.
	 */
	public Integer call() throws Exception {
		return server.findRowCountWithCopy(query, t);
	}

	
	
}

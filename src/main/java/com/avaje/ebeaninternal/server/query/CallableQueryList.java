package com.avaje.ebeaninternal.server.query;

import java.util.List;
import java.util.concurrent.Callable;

import com.avaje.ebean.Query;
import com.avaje.ebean.Transaction;
import com.avaje.ebeaninternal.api.SpiEbeanServer;

/**
 * Represent the findList query as a Callable.
 *
 * @param <T> the entity bean type
 */
public class CallableQueryList<T> extends CallableQuery<T> implements Callable<List<T>> {

	
	public CallableQueryList(SpiEbeanServer server, Query<T> query, Transaction t) {
		super(server, query, t);
	}
	
	/**
	 * Execute the query returning the resulting List.
	 */
	public List<T> call() throws Exception {
		return server.findList(query, t);
	}

	
	
}

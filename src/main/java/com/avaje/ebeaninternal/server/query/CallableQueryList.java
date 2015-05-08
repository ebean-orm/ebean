package com.avaje.ebeaninternal.server.query;

import java.util.List;
import java.util.concurrent.Callable;

import com.avaje.ebean.Transaction;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiQuery;

/**
 * Represent the findList query as a Callable.
 *
 * @param <T> the entity bean type
 */
public class CallableQueryList<T> extends CallableQuery<T> implements Callable<List<T>> {

	
	public CallableQueryList(SpiEbeanServer server, SpiQuery<T> query, Transaction t) {
		super(server, query, t);
	}
	
	/**
	 * Execute the query returning the resulting List.
	 */
	public List<T> call() throws Exception {
	  try {
	    return server.findList(query, transaction);
	  } finally {
	    // cleanup the underlying connection
	    transaction.end();
	  }
	}

	
	
}

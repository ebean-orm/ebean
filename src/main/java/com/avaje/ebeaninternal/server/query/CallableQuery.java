package com.avaje.ebeaninternal.server.query;

import com.avaje.ebean.Query;
import com.avaje.ebean.Transaction;
import com.avaje.ebeaninternal.api.SpiEbeanServer;

/**
 * Base object for making query execution into Callable's.
 * 
 * @author rbygrave
 *
 * @param <T> the entity bean type
 */
public abstract class CallableQuery<T> {

	protected final Query<T> query;
	
	protected final SpiEbeanServer server;
	
	protected final Transaction t;
	
	public CallableQuery(SpiEbeanServer server, Query<T> query, Transaction t) {
		this.server = server;
		this.query = query;
		this.t = t;
	}
	
}

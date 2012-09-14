package com.avaje.ebeaninternal.server.query;

import java.util.List;
import java.util.concurrent.Callable;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.Transaction;

/**
 * Represent the SQL query findList as a Callable.
 */
public class CallableSqlQueryList implements Callable<List<SqlRow>> {

	private final SqlQuery query;
	
	private final EbeanServer server;
	
	private final Transaction t;
	
	public CallableSqlQueryList(EbeanServer server, SqlQuery query, Transaction t) {
		this.server = server;
		this.query = query;
		this.t = t;
	}

	/**
	 * Execute the query returning the resulting list.
	 */
	public List<SqlRow> call() throws Exception {
		return server.findList(query, t);
	}

	
	
}

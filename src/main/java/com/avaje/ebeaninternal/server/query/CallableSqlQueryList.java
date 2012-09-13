/**
 * Copyright (C) 2009  Robin Bygrave
 * 
 * This file is part of Ebean.
 * 
 * Ebean is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *  
 * Ebean is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Ebean; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA  
 */
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

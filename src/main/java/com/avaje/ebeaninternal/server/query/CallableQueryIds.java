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

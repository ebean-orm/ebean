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

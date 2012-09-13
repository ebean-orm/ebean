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
import java.util.concurrent.FutureTask;

import com.avaje.ebean.FutureList;
import com.avaje.ebean.Query;

/**
 * Default implementation for FutureList.
 */
public class QueryFutureList<T> extends BaseFuture<List<T>> implements FutureList<T> {

	private final Query<T> query;
	
	
	public QueryFutureList(Query<T> query, FutureTask<List<T>> futureTask) {
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

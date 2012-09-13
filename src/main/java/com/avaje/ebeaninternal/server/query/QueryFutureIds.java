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

import com.avaje.ebean.FutureIds;
import com.avaje.ebean.Query;
import com.avaje.ebeaninternal.api.SpiQuery;

/**
 * Default implementation of FutureIds.
 */
public class QueryFutureIds<T> extends BaseFuture<List<Object>> implements FutureIds<T> {

	private final SpiQuery<T> query;
	
	public QueryFutureIds(SpiQuery<T> query, FutureTask<List<Object>> futureTask) {
		super(futureTask);
		this.query = query;
	}
	
	public Query<T> getQuery() {
		return query;
	}
	
	public List<Object> getPartialIds() {
		return query.getIdList();
	}

	public boolean cancel(boolean mayInterruptIfRunning) {
		query.cancel();
		return super.cancel(mayInterruptIfRunning);
	}

}

/**
 * Copyright (C) 2009 Authors
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
package com.avaje.ebeaninternal.api;

import com.avaje.ebeaninternal.server.core.OrmQueryRequest;

/**
 * Defines the method for executing secondary queries.
 * <p>
 * That is +query nodes in a orm query get executed after
 * the initial query as 'secondary' queries.
 * </p>
 */
public interface LoadSecondaryQuery {

	/**
	 * Execute the secondary query with a given batch size.
	 * 
	 * @param parentRequest
	 *            the originating query request
	 */
	public void loadSecondaryQuery(OrmQueryRequest<?> parentRequest, int requestedBatchSize, boolean all);
}

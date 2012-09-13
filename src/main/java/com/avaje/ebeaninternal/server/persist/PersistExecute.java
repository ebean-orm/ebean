/**
 * Copyright (C) 2006  Robin Bygrave
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
package com.avaje.ebeaninternal.server.persist;

import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.core.PersistRequestCallableSql;
import com.avaje.ebeaninternal.server.core.PersistRequestOrmUpdate;
import com.avaje.ebeaninternal.server.core.PersistRequestUpdateSql;

/**
 * The actual execution of persist requests.
 * <p>
 * A Persister 'front-ends' this object and handles the
 * batching, cascading, concurrency mode detection etc.
 * </p>
 *
 */
public interface PersistExecute {
	
	/**
	 * Create a BatchControl for the current transaction.
	 */
	public BatchControl createBatchControl(SpiTransaction t);
	
	/**
	 * Execute a Bean (or MapBean) insert.
	 */
	public <T> void executeInsertBean(PersistRequestBean<T> request);

	/**
	 * Execute a Bean (or MapBean) update.
	 */
	public <T> void executeUpdateBean(PersistRequestBean<T> request);

	/**
	 * Execute a Bean (or MapBean) delete.
	 */
	public <T> void executeDeleteBean(PersistRequestBean<T> request);

	/**
	 * Execute a Update.
	 */
	public int executeOrmUpdate(PersistRequestOrmUpdate request);
	
	/**
	 * Execute a CallableSql.
	 */
	public int executeSqlCallable(PersistRequestCallableSql request);

	/**
	 * Execute a UpdateSql.
	 */
	public int executeSqlUpdate(PersistRequestUpdateSql request);

}

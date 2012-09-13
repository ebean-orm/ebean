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
package com.avaje.ebeaninternal.server.core;

import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.ebeaninternal.server.persist.BatchControl;
import com.avaje.ebeaninternal.server.persist.BatchPostExecute;
import com.avaje.ebeaninternal.server.persist.PersistExecute;

/**
 * Wraps all the objects used to persist a bean.
 */
public abstract class PersistRequest extends BeanRequest implements BatchPostExecute {

	public enum Type {
		INSERT, UPDATE, DELETE, ORMUPDATE, UPDATESQL, CALLABLESQL
	};

	boolean persistCascade;
	
	/**
	 * One of INSERT, UPDATE, DELETE, UPDATESQL or CALLABLESQL.
	 */
	Type type;
	
	final PersistExecute persistExecute;

	/**
	 * Used by CallableSqlRequest and UpdateSqlRequest.
	 */
	public PersistRequest(SpiEbeanServer server, SpiTransaction t, PersistExecute persistExecute) {
		super(server, t);
		this.persistExecute = persistExecute;
	}

	/**
	 * Execute a the request or queue/batch it for later execution.
	 */
	public abstract int executeOrQueue();

	/**
	 * Execute the request right now.
	 */
	public abstract int executeNow();
		   
    public PstmtBatch getPstmtBatch() {
    	return ebeanServer.getPstmtBatch();
    }
 
    public boolean isLogSql() {
        return transaction.isLogSql();
    }
    
    public boolean isLogSummary() {
        return transaction.isLogSummary();
    }
    
	/**
	 * Execute the Callable statement.
	 */
	public int executeStatement() {
		
		boolean batch = transaction.isBatchThisRequest();

		int rows;
		BatchControl control = transaction.getBatchControl();
		if (control != null) {
			rows = control.executeStatementOrBatch(this, batch);
		
		} else if (batch) {
			// need to create the BatchControl
			control = persistExecute.createBatchControl(transaction);
			rows = control.executeStatementOrBatch(this, batch);
		} else {
			rows = executeNow();
		}
				
		return rows;
	}
	
	public void initTransIfRequired() {
		createImplicitTransIfRequired(false);
		persistCascade = transaction.isPersistCascade();
	}

	/**
	 * Return the type of this request. One of INSERT, UPDATE, DELETE, UPDATESQL
	 * or CALLABLESQL.
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Set the type of this request. One of INSERT, UPDATE, DELETE, UPDATESQL or
	 * CALLABLESQL.
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * Return true if save and delete should cascade.
	 */
	public boolean isPersistCascade() {
		return persistCascade;
	}
	
}

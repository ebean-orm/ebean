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

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.SQLException;

import com.avaje.ebean.CallableSql;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebeaninternal.api.BindParams;
import com.avaje.ebeaninternal.api.SpiCallableSql;
import com.avaje.ebeaninternal.api.TransactionEventTable;
import com.avaje.ebeaninternal.api.BindParams.Param;


public class DefaultCallableSql implements Serializable, SpiCallableSql {

	private static final long serialVersionUID = 8984272253185424701L;

	private transient final EbeanServer server;
	
	/**
	 * The callable sql.
	 */
	private String sql;

	/**
	 * To display in the transaction log to help identify the procedure.
	 */
	private String label;

	private int timeout;
	
	/**
	 * Holds the table modification information. On commit this information is
	 * used to manage the cache etc.
	 */
	private TransactionEventTable transactionEvent = new TransactionEventTable();

	private BindParams bindParameters = new BindParams();

	/**
	 * Create with callable sql.
	 */
	public DefaultCallableSql(EbeanServer server, String sql) {
		this.server = server;
		this.sql = sql;
	}

	public void execute() {
		server.execute(this, null);
	}

	public String getLabel() {
		return label;
	}

	public CallableSql setLabel(String label) {
		this.label = label;
		return this;
	}

	public int getTimeout() {
		return timeout;
	}

	public String getSql() {
		return sql;
	}

	public CallableSql setTimeout(int secs) {
		this.timeout = secs;
		return this;
	}

	public CallableSql setSql(String sql) {
		this.sql = sql;
		return this;
	}

	public CallableSql bind(int position, Object value) {
		bindParameters.setParameter(position, value);
		return this;
	}

	public CallableSql setParameter(int position, Object value) {
		bindParameters.setParameter(position, value);
		return this;
	}

	public CallableSql registerOut(int position, int type) {
		bindParameters.registerOut(position, type);
		return this;
	}

	public Object getObject(int position) {
		Param p = bindParameters.getParameter(position);
		return p.getOutValue();
	}
	
	public boolean executeOverride(CallableStatement cstmt) throws SQLException {
		return false;
	}

	public CallableSql addModification(String tableName, boolean inserts, boolean updates,
			boolean deletes) {

		transactionEvent.add(tableName, inserts, updates, deletes);
		return this;
	}

	/**
	 * Return the TransactionEvent which holds the table modification
	 * information for this CallableSql. This information is merged into the
	 * transaction after the transaction is commited.
	 */
	public TransactionEventTable getTransactionEventTable() {
		return transactionEvent;
	}

	public BindParams getBindParams() {
		return bindParameters;
	}

}

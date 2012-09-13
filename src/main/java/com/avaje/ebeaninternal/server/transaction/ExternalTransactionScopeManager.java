/**
 * Copyright (C) 2009 the original author or authors
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
package com.avaje.ebeaninternal.server.transaction;

import com.avaje.ebean.config.ExternalTransactionManager;
import com.avaje.ebeaninternal.api.SpiTransaction;

/**
 * A TransactionScopeManager aware of external transaction managers.
 */
public class ExternalTransactionScopeManager extends TransactionScopeManager {

	final ExternalTransactionManager externalManager;
	
	/**
	 * Instantiates  transaction scope manager.
	 *
	 * @param transactionManager the transaction manager
	 */
	public ExternalTransactionScopeManager(TransactionManager transactionManager, ExternalTransactionManager externalManager) {
		super(transactionManager);
		this.externalManager = externalManager;
	}

	public void commit() {
		DefaultTransactionThreadLocal.commit(serverName);
	}


	public void end() {
		DefaultTransactionThreadLocal.end(serverName);
	}

	public SpiTransaction get() {
		
		return (SpiTransaction)externalManager.getCurrentTransaction();		
	}

	public void replace(SpiTransaction trans) {
		DefaultTransactionThreadLocal.replace(serverName, trans);
	}

	public void rollback() {
		DefaultTransactionThreadLocal.rollback(serverName);
	}

	public void set(SpiTransaction trans) {
		DefaultTransactionThreadLocal.set(serverName, trans);
	}
}

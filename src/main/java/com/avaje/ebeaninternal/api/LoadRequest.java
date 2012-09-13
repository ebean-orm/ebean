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

import com.avaje.ebean.Transaction;

/**
 * Request for loading Associated One Beans.
 */
public abstract class LoadRequest {

	protected final boolean lazy;

	protected final int batchSize;

	protected final Transaction transaction;

	public LoadRequest(Transaction transaction, int batchSize, boolean lazy) {

		this.transaction = transaction;
		this.batchSize = batchSize;
		this.lazy = lazy;
	}


	/**
	 * Return true if this is a lazy load and false if it is a secondary query.
	 */
	public boolean isLazy() {
		return lazy;
	}

	/**
	 * Return the requested batch size.
	 */
	public int getBatchSize() {
		return batchSize;
	}

	/**
	 * Return the transaction to use if this is a secondary query.
	 * <p>
	 * Lazy loading queries run in their own transaction.
	 * </p>
	 */
	public Transaction getTransaction() {
		return transaction;
	}

}

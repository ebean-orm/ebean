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
package com.avaje.ebeaninternal.api;

import java.util.ArrayList;
import java.util.List;

import com.avaje.ebeaninternal.server.core.PersistRequestBean;

/**
 * Lists of inserted updated and deleted beans that have a BeanPersistListener.
 * <p>
 * These beans will be sent to the appropriate BeanListeners after a successful
 * commit of the transaction.
 * </p>
 */
public class TransactionEventBeans {

	ArrayList<PersistRequestBean<?>> requests = new ArrayList<PersistRequestBean<?>>();

	/**
	 * Return the list of PersistRequests that BeanListeners are interested in.
	 */
	public List<PersistRequestBean<?>> getRequests() {
		return requests;
	}

	/**
	 * Add a bean for BeanListener notification.
	 */
	public void add(PersistRequestBean<?> request) {

		requests.add(request);
	}
	
	public void notifyCache() {
		for (int i = 0; i < requests.size(); i++) {
			requests.get(i).notifyCache();
		}
	}

}

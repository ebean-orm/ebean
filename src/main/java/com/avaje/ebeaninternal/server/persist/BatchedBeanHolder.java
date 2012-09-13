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

import java.util.ArrayList;
import java.util.HashSet;

import com.avaje.ebeaninternal.server.core.PersistRequest;
import com.avaje.ebeaninternal.server.core.PersistRequestBean;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Holds lists of persist requests for beans of a given typeDescription.
 * <p>
 * This is used to delay the actual binding of the bean to PreparedStatements.
 * The reason is that don't have all the bind values yet in the case of inserts
 * with getGeneratedKeys.
 * </p>
 * <p>
 * Has a depth which is used to determine the order in which it should be
 * executed. The lowest depth is executed first.
 * </p>
 */
public class BatchedBeanHolder {

	/**
	 * The owning queue.
	 */
	private final BatchControl control;

	private final String shortDesc;

	/**
	 * The 'depth' which is used to determine the execution order.
	 */
	private final int order;

	/**
	 * The list of bean insert requests.
	 */
	private ArrayList<PersistRequest> inserts;

	/**
	 * The list of bean update requests.
	 */
	private ArrayList<PersistRequest> updates;

	/**
	 * The list of bean delete requests.
	 */
	private ArrayList<PersistRequest> deletes;

	private HashSet<Integer> beanHashCodes = new HashSet<Integer>();
	
	/**
	 * Create a new entry with a given type and depth.
	 */
	public BatchedBeanHolder(BatchControl control, BeanDescriptor<?> beanDescriptor, int order) {
		this.control = control;
		this.shortDesc = beanDescriptor.getName() + ":" + order;
		this.order = order;
	}

	/**
	 * Return the depth.
	 */
	public int getOrder() {
		return order;
	}
	
	/**
	 * Execute all the persist requests in this entry.
	 * <p>
	 * This will Batch all the similar requests into one or more BatchStatements
	 * and then execute them.
	 * </p>
	 */
	public void executeNow() {
		// process the requests. Creates one or more PreparedStatements
		// with binding addBatch() for each request.

		// Note updates and deletes can result in many PreparedStatements
		// if their where clauses differ via use of IS NOT NULL.
		if (inserts != null && !inserts.isEmpty()) {
			control.executeNow(inserts);
			inserts.clear();
		}
		if (updates != null && !updates.isEmpty()) {
			control.executeNow(updates);
			updates.clear();
		}
		if (deletes != null && !deletes.isEmpty()) {
			control.executeNow(deletes);
			deletes.clear();
		}
		beanHashCodes.clear();
	}

	public String toString() {
		return shortDesc;
	}

	/**
	 * Return the list for the typeCode.
	 */
	public ArrayList<PersistRequest> getList(PersistRequestBean<?> request) {
	    
	    Integer objHashCode = Integer.valueOf(System.identityHashCode(request.getBean()));
	    
	    if (!beanHashCodes.add(objHashCode)) {
	        // special case where the same bean instance has already been
	        // added to the batch (doesn't really occur with non-batching
	        // as the bean gets changed from dirty to loaded earlier)
	        return null;
	    }
	    
		switch (request.getType()) {
		case INSERT:
			if (inserts == null) {
				inserts = new ArrayList<PersistRequest>();
			}
			return inserts;

		case UPDATE:
			if (updates == null) {
				updates = new ArrayList<PersistRequest>();
			}
			return updates;

		case DELETE:
			if (deletes == null) {
				deletes = new ArrayList<PersistRequest>();
			}
			return deletes;

		default:
			throw new RuntimeException("Invalid type code " + request.getType());
		}
	}
}

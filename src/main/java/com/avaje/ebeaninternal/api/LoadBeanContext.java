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

import com.avaje.ebean.bean.PersistenceContext;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;

/**
 * Controls the loading of ManyToOne and OneToOne relationships.
 *  
 * @author rbygrave
 */
public interface LoadBeanContext extends LoadSecondaryQuery {
	
	/**
	 * Configure the query to load beans for this node/path.
	 */
	public void configureQuery(SpiQuery<?> query, String lazyLoadProperty);
	
	/**
	 * Return the full path of this node from the root object.
	 */
	public String getFullPath();
	
	/**
	 * Return the persistence context used for all queries 
	 * related to this object graph.
	 */
	public PersistenceContext getPersistenceContext();

	/**
	 * Return the BeanDescriptor for beans for this node.
	 */
	public BeanDescriptor<?> getBeanDescriptor();

	/**
	 * Return the batchSize used for lazy loading beans.
	 */
	public int getBatchSize();
	
}

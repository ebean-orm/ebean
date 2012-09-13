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

import javax.persistence.PersistenceException;

import com.avaje.ebeaninternal.server.core.PersistRequestBean;

/**
 * Defines bean insert update and delete implementation.
 */
public interface BeanPersister {

	/**
	 * execute the insert bean request.
	 */
	public void insert(PersistRequestBean<?>  request) throws PersistenceException;

	/**
	 * execute the update bean request.
	 */
	public void update(PersistRequestBean<?>  request) throws PersistenceException;

	/**
	 * execute the delete bean request.
	 */
	public void delete(PersistRequestBean<?> request) throws PersistenceException;

}

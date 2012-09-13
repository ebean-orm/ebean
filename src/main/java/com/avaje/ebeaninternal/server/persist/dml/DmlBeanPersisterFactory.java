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
package com.avaje.ebeaninternal.server.persist.dml;

import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.persist.BeanPersister;
import com.avaje.ebeaninternal.server.persist.BeanPersisterFactory;

/**
 * Factory for creating a DmlBeanPersister for a bean type.
 */
public class DmlBeanPersisterFactory implements BeanPersisterFactory {
	
	private final MetaFactory metaFactory;
	
	public DmlBeanPersisterFactory(DatabasePlatform dbPlatform) {
		this.metaFactory = new MetaFactory(dbPlatform);
	}
	
	
	/**
	 * Create a DmlBeanPersister for the given bean type.
	 */
	public BeanPersister create(BeanDescriptor<?> desc) {
		
		UpdateMeta updMeta = metaFactory.createUpdate(desc);
		DeleteMeta delMeta = metaFactory.createDelete(desc);
		InsertMeta insMeta = metaFactory.createInsert(desc);
		
		return new DmlBeanPersister(updMeta, insMeta, delMeta);
		
	}
	
}

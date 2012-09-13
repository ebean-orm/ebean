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
package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.config.dbplatform.DatabasePlatform;
import com.avaje.ebeaninternal.server.persist.BeanPersister;
import com.avaje.ebeaninternal.server.persist.BeanPersisterFactory;
import com.avaje.ebeaninternal.server.persist.dml.DmlBeanPersisterFactory;

/**
 * Creates BeanManagers.
 */
public class BeanManagerFactory {

	final BeanPersisterFactory peristerFactory;
	
	public BeanManagerFactory(ServerConfig config, DatabasePlatform dbPlatform) {
		peristerFactory = new DmlBeanPersisterFactory(dbPlatform);
	}
	
	public <T> BeanManager<T> create(BeanDescriptor<T> desc) {
		
		BeanPersister persister = peristerFactory.create(desc);

		return new BeanManager<T>(desc, persister);
	}

}

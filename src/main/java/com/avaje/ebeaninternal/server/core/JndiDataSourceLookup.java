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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import com.avaje.ebean.config.GlobalProperties;


/**
 * Helper to lookup a DataSource from JNDI.
 */
public class JndiDataSourceLookup {

	private static final String DEFAULT_PREFIX = "java:comp/env/jdbc/";

	String jndiPrefix = GlobalProperties.get("ebean.datasource.jndi.prefix", DEFAULT_PREFIX);
	
	public JndiDataSourceLookup() {
	}
	
	/**
	 * Return the DataSource by JNDI lookup.
	 * <p>
	 * If name is null the 'default' dataSource is returned.
	 * </p>
	 */
	public DataSource lookup(String jndiName) {

		try {
			
			if (!jndiName.startsWith("java:")){
				jndiName = jndiPrefix + jndiName;
			}
			
			Context ctx = new InitialContext();
			DataSource ds = (DataSource) ctx.lookup(jndiName);
			if (ds == null) {
				throw new PersistenceException("JNDI DataSource [" + jndiName + "] not found?");
			}
			return ds;

		} catch (NamingException ex) {
			throw new PersistenceException(ex);
		}
	}
}

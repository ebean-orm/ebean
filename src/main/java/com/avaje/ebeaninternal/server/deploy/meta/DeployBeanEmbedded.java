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
package com.avaje.ebeaninternal.server.deploy.meta;

import java.util.HashMap;
import java.util.Map;

/**
 * Collects Deployment information on Embedded beans.
 * <p>
 * Typically collects the overridden column names mapped
 * to the Embedded bean. 
 * </p>
 */
public class DeployBeanEmbedded {

	/**
	 * A map of property names to dbColumns.
	 */
	Map<String,String> propMap = new HashMap<String, String>();
	
	/**
	 * Set a property name to use a specific dbColumn.
	 */
	public void put(String propertyName, String dbCoumn){
		propMap.put(propertyName, dbCoumn);
	}
	
	/**
	 * Set a Map of property names to dbColumns.
	 */
	public void putAll(Map<String,String> propertyColumnMap){
		propMap.putAll(propertyColumnMap);
	}

	/**
	 * Return a map of property names to dbColumns.
	 */
	public Map<String, String> getPropertyColumnMap() {
		return propMap;
	}

	
}

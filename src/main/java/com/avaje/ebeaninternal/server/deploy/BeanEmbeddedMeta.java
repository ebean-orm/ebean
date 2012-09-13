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

public class BeanEmbeddedMeta {

	
	final BeanProperty[] properties;
	
	public BeanEmbeddedMeta(BeanProperty[] properties) {
		this.properties = properties;
	}

	/**
	 * Return the properties with over ridden mapping information.
	 */
	public BeanProperty[] getProperties() {
		return properties;
	}
	
	/**
	 * Return true if at least one property is a version property.
	 */
	public boolean isEmbeddedVersion() {
		for (int i = 0; i < properties.length; i++) {
			if (properties[i].isVersion()){
				return true;
			}
		}
		return false;
	}
	
}

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
package com.avaje.ebeaninternal.server.deploy.generatedproperty;

import java.sql.Timestamp;

import javax.persistence.PersistenceException;

import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;

/**
 * Helper for creating Insert timestamp GeneratedProperty objects.
 */
public class InsertTimestampFactory {

	final GeneratedInsertTimestamp timestamp = new GeneratedInsertTimestamp();

	final GeneratedInsertDate utilDate = new GeneratedInsertDate();

	final GeneratedInsertLong longTime = new GeneratedInsertLong();

	public void setInsertTimestamp(DeployBeanProperty property) {

		property.setGeneratedProperty(createInsertTimestamp(property));
	}
	
	/**
	 * Create the insert GeneratedProperty depending on the property type.
	 */
	public GeneratedProperty createInsertTimestamp(DeployBeanProperty property) {
		
		Class<?> propType = property.getPropertyType();
		if (propType.equals(Timestamp.class)) {
			return timestamp;
		}
		if (propType.equals(java.util.Date.class)) {
			return utilDate;
		}
		if (propType.equals(Long.class) || propType.equals(long.class)) {
			return longTime;
		}
		
		//TODO: Support JODA Time objects ... perhaps others?
		
		String msg = "Generated Insert Timestamp not supported on "+propType.getName();
		throw new PersistenceException(msg);
	}
	
}

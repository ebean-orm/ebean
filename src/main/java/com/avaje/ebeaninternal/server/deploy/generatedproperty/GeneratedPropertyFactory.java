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

import java.math.BigDecimal;
import java.util.HashSet;

import com.avaje.ebeaninternal.server.deploy.meta.DeployBeanProperty;

/**
 * Default implementation of GeneratedPropertyFactory.
 */
public class GeneratedPropertyFactory {

	CounterFactory counterFactory;

	InsertTimestampFactory insertFactory;

	UpdateTimestampFactory updateFactory;

	HashSet<String> numberTypes = new HashSet<String>();

	public GeneratedPropertyFactory() {
		counterFactory = new CounterFactory();
		insertFactory = new InsertTimestampFactory();
		updateFactory = new UpdateTimestampFactory();
		

		numberTypes.add(Integer.class.getName());
		numberTypes.add(int.class.getName());
		numberTypes.add(Long.class.getName());
		numberTypes.add(long.class.getName());
		numberTypes.add(Short.class.getName());
		numberTypes.add(short.class.getName());
		numberTypes.add(Double.class.getName());
		numberTypes.add(double.class.getName());
		numberTypes.add(BigDecimal.class.getName());
	}

	private boolean isNumberType(String typeClassName) {
		return numberTypes.contains(typeClassName);
	}
	
	public void setVersion(DeployBeanProperty property) {
		if (isNumberType(property.getPropertyType().getName())) {
			setCounter(property);
		} else {
			setUpdateTimestamp(property);
		}
	}
	
	public void setCounter(DeployBeanProperty property) {
		
		counterFactory.setCounter(property);
	}

	public void setInsertTimestamp(DeployBeanProperty property) {
		
		insertFactory.setInsertTimestamp(property);
	}

	public void setUpdateTimestamp(DeployBeanProperty property) {
		
		updateFactory.setUpdateTimestamp(property);
	}

}

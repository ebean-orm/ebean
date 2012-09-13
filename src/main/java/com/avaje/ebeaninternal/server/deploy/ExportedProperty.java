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

import com.avaje.ebeaninternal.server.core.InternString;

/**
 * The Exported foreign key and property.
 * <p>
 * Used to for Assoc Manys to create references etc.
 * </p>
 */
public class ExportedProperty {

	private final String foreignDbColumn;
	
	private final BeanProperty property;

	private final boolean embedded;
	
	public ExportedProperty(boolean embedded, String foreignDbColumn, BeanProperty property) {
		this.embedded = embedded;
		this.foreignDbColumn = InternString.intern(foreignDbColumn);
		this.property = property;
	}
	
	/**
	 * Return true if this is part of an embedded concatinated key.
	 */
	public boolean isEmbedded() {
		return embedded;
	}
	
	/**
	 * Return the property value from the bean.
	 */
	public Object getValue(Object bean){
		return property.getValue(bean);
	}
	
	/**
	 * Return the foreign database column matching this property.
	 * <p>
	 * We use this foreign database column in the query predicates
	 * in preference to a parentProperty.idProperty = value.
	 * Just using the foreign database column avoids triggering
	 * a join to the 'parent' table.
	 * </p>
	 */
	public String getForeignDbColumn() {
		return foreignDbColumn;
	}

}

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
 * Represents a database foreign key which can map to an object relationship.
 */
public class BeanForeignKey {

	private final String dbColumn;

	private final int dbType;

	/**
	 * Construct the BeanForeignKey.
	 */
	public BeanForeignKey(String dbColumn, int dbType) {
		this.dbColumn = InternString.intern(dbColumn);
		this.dbType = dbType;
	}

	/**
	 * Return the database column.
	 */
	public String getDbColumn() {
		return dbColumn;
	}

	/**
	 * Return the JDBC datatype of the database column.
	 */
	public int getDbType() {
		return dbType;
	}

	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof BeanForeignKey) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}

	public int hashCode() {
		int hc = getClass().hashCode();
		hc = hc * 31 + (dbColumn != null ? dbColumn.hashCode() : 0);
		return hc;
	}

	public String toString() {
		return dbColumn;
	}

}

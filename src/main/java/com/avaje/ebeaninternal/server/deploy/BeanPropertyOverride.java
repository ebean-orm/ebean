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
import com.avaje.ebeaninternal.server.lib.util.StringHelper;

/**
 * Used hold meta data when a bean property is overridden.
 * <p>
 * Typically this is for Embedded Beans.
 * </p>
 */
public class BeanPropertyOverride {

	private final String dbColumn;
		
	private final String sqlFormulaSelect;

	private final String sqlFormulaJoin;

	public BeanPropertyOverride(String dbColumn) {
		this(dbColumn, null, null);
	}
	
	public BeanPropertyOverride(String dbColumn, String sqlFormulaSelect, String sqlFormulaJoin) {
		this.dbColumn = InternString.intern(dbColumn);
		this.sqlFormulaSelect = InternString.intern(sqlFormulaSelect);
		this.sqlFormulaJoin = InternString.intern(sqlFormulaJoin);
	}

	public String getDbColumn() {
		return dbColumn;
	}

	public String getSqlFormulaSelect() {
		return sqlFormulaSelect;
	}
	
	public String getSqlFormulaJoin() {
		return sqlFormulaJoin;
	}
	
	public String replace(String src, String srcDbColumn){
	    return StringHelper.replaceString(src, srcDbColumn, dbColumn);
	}
}

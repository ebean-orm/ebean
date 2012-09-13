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
package com.avaje.ebeaninternal.server.type;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

/**
 * ScalarType for java.sql.Date.
 */
public class ScalarTypeDate extends ScalarTypeBaseDate<java.sql.Date> {
	
	public ScalarTypeDate() {
		super(Date.class, true, Types.DATE);
	}
	
	@Override
    public Date convertFromDate(Date date) {
        return date;
    }

    @Override
    public Date convertToDate(Date t) {
        return t;
    }

    public void bind(DataBind b, java.sql.Date value) throws SQLException {
		if (value == null){
			b.setNull(Types.DATE);
		} else {
			b.setDate(value);
		}
	}

	public java.sql.Date read(DataReader dataReader) throws SQLException {	
		return dataReader.getDate();
	}
	
	public Object toJdbcType(Object value) {
		return BasicTypeConverter.toDate(value);
	}

	public java.sql.Date toBeanType(Object value) {
		return BasicTypeConverter.toDate(value);
	}

}

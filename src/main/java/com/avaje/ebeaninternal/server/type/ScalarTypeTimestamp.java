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

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

/**
 * ScalarType for java.sql.Timestamp.
 */
public class ScalarTypeTimestamp extends ScalarTypeBaseDateTime<Timestamp> {

	public ScalarTypeTimestamp() {
		super(Timestamp.class, true, Types.TIMESTAMP);
	}
	
	@Override
    public Timestamp convertFromTimestamp(Timestamp ts) {
        return ts;
    }

    @Override
    public Timestamp convertToTimestamp(Timestamp t) {
        return t;
    }

    public void bind(DataBind b, Timestamp value) throws SQLException {
		if (value == null){
			b.setNull(Types.TIMESTAMP);
		} else {
			b.setTimestamp(value);
		}
	}

	public Timestamp read(DataReader dataReader) throws SQLException {
		
		return dataReader.getTimestamp();
	}
	
	public Object toJdbcType(Object value) {
		return BasicTypeConverter.toTimestamp(value);
	}

	public Timestamp toBeanType(Object value) {
		return BasicTypeConverter.toTimestamp(value);
	}	
}

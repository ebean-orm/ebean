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
import java.sql.Types;

import com.avaje.ebean.text.json.JsonValueAdapter;
import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

/**
 * ScalarType for String.
 */
public class ScalarTypeClob extends ScalarTypeBaseVarchar<String> {

	static final int clobBufferSize = 512;
	
	static final int stringInitialSize = 512;
	
	protected ScalarTypeClob(boolean jdbcNative, int jdbcType) {
		super(String.class, jdbcNative, jdbcType);
	}
	
	public ScalarTypeClob() {
		super(String.class, true, Types.CLOB);
	}
	
	@Override
    public String convertFromDbString(String dbValue) {
        return dbValue;
    }

    @Override
    public String convertToDbString(String beanValue) {
        return beanValue;
    }

    public void bind(DataBind b, String value) throws SQLException {
		if (value == null) {
			b.setNull(Types.VARCHAR);
		} else {
			b.setString(value);
		}
	}

	public String read(DataReader dataReader) throws SQLException {

	    return dataReader.getStringClob();
	}

	public Object toJdbcType(Object value) {
		return BasicTypeConverter.toString(value);
	}

	public String toBeanType(Object value) {
		return BasicTypeConverter.toString(value);
	}

	
	public String formatValue(String t) {
        return t;
    }

    public String parse(String value) {
		return value;
	}
	
    @Override
    public String jsonFromString(String value, JsonValueAdapter ctx) {
        return value;
    }

    @Override
    public String jsonToString(String value, JsonValueAdapter ctx) {
        return EscapeJson.escapeQuote(value);
    }
    
}

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
 * ScalarType for char[].
 */
public class ScalarTypeCharArray extends ScalarTypeBaseVarchar<char[]>{

	public ScalarTypeCharArray() {
		super(char[].class, false, Types.VARCHAR);
	}
	
	@Override
    public char[] convertFromDbString(String dbValue) {
        return dbValue.toCharArray();
    }

    @Override
    public String convertToDbString(char[] beanValue) {
        return new String(beanValue);
    }

    public void bind(DataBind b, char[] value) throws SQLException {
		if (value == null){
			b.setNull(Types.VARCHAR);
		} else {
			String s = BasicTypeConverter.toString(value);
			b.setString(s);
		}
	}

	public char[] read(DataReader dataReader) throws SQLException {
		String string = dataReader.getString();
		if (string == null){
			return null;
		} else {
			return string.toCharArray();
		}
	}
	
	public Object toJdbcType(Object value) {
		return BasicTypeConverter.toString(value);
	}

	public char[] toBeanType(Object value) {
		String s = BasicTypeConverter.toString(value);
		return s.toCharArray();
	}
	
	public String formatValue(char[] t) {
        return String.valueOf(t);
    }

    public char[] parse(String value) {
		return value.toCharArray();
	}
	
    @Override
    public char[] jsonFromString(String value, JsonValueAdapter ctx) {
        return value.toCharArray();
    }

    @Override
    public String jsonToString(char[] value, JsonValueAdapter ctx) {
        return EscapeJson.escapeQuote(String.valueOf(value));
    }
    
}

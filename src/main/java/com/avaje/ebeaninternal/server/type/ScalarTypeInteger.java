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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

import com.avaje.ebean.text.TextException;
import com.avaje.ebean.text.json.JsonValueAdapter;
import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

/**
 * ScalarType for Integer and int.
 */
public class ScalarTypeInteger extends ScalarTypeBase<Integer> {

	public ScalarTypeInteger() {
		super(Integer.class, true, Types.INTEGER);
	}
	
	public void bind(DataBind b, Integer value) throws SQLException {
		if (value == null){
			b.setNull(Types.INTEGER);
		} else {
			b.setInt(value.intValue());
		}
	}

	public Integer read(DataReader dataReader) throws SQLException {
		
		return dataReader.getInt();
	}

    public Object readData(DataInput dataInput) throws IOException {
        return Integer.valueOf(dataInput.readInt());
    }

    public void writeData(DataOutput dataOutput, Object v) throws IOException {
        dataOutput.writeInt((Integer) v);
    }
	
	public Object toJdbcType(Object value) {
		return BasicTypeConverter.toInteger(value);
	}

	public Integer toBeanType(Object value) {
		return BasicTypeConverter.toInteger(value);
	}

    public String formatValue(Integer v) {
        return v.toString();
    }

	public Integer parse(String value) {
		return Integer.valueOf(value);
	}

	public Integer parseDateTime(long systemTimeMillis) {
		throw new TextException("Not Supported");
	}

	public boolean isDateTimeCapable() {
		return false;
	}

    public String jsonToString(Integer value, JsonValueAdapter ctx) {
        return value.toString();
    }
    
    public Integer jsonFromString(String value, JsonValueAdapter ctx) {
        return Integer.valueOf(value);
    }
}

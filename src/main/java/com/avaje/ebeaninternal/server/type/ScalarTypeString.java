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

import com.avaje.ebean.text.json.JsonValueAdapter;
import com.avaje.ebeaninternal.server.core.BasicTypeConverter;
import com.avaje.ebeaninternal.server.text.json.WriteJsonBuffer;

/**
 * ScalarType for String.
 */
public class ScalarTypeString extends ScalarTypeBase<String> {

	public ScalarTypeString() {
		super(String.class, true, Types.VARCHAR);
	}
	
	public void bind(DataBind b, String value) throws SQLException {
		if (value == null){
			b.setNull(Types.VARCHAR);
		} else {
			b.setString(value);
		}
	}

	public String read(DataReader dataReader) throws SQLException {
		
		return dataReader.getString();
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
	
	public String parseDateTime(long systemTimeMillis) {
		return String.valueOf(systemTimeMillis);
	}

	public boolean isDateTimeCapable() {
		return true;
	}

	
    @Override
    public void jsonWrite(WriteJsonBuffer buffer, String value, JsonValueAdapter ctx) {
	    String s = format(value);
    	EscapeJson.escapeQuote(s, buffer);
    }

	@Override
    public String jsonFromString(String value, JsonValueAdapter ctx) {
        return value;
    }

    @Override
    public String jsonToString(String value, JsonValueAdapter ctx) {
        return EscapeJson.escapeQuote(value);
    }

    public Object readData(DataInput dataInput) throws IOException {
        if (!dataInput.readBoolean()) {
            return null;
        } else {
            return dataInput.readUTF();
        }
    }

    public void writeData(DataOutput dataOutput, Object v) throws IOException {
        
        String value = (String)v;
        if (value == null){
            dataOutput.writeBoolean(false);
        } else {
            dataOutput.writeBoolean(true);
            dataOutput.writeUTF(value);            
        }
    }
	
    
}

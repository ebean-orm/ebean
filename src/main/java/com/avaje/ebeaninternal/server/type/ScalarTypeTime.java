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
import java.sql.Time;
import java.sql.Types;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

/**
 * ScalarType for java.sql.Time.
 */
public class ScalarTypeTime extends ScalarTypeBase<Time> {

	public ScalarTypeTime() {
		super(Time.class, true, Types.TIME);
	}
	
	public void bind(DataBind b, Time value) throws SQLException {
		if (value == null){
			b.setNull(Types.TIME);
		} else {
			b.setTime(value);
		}
	}

	public Time read(DataReader dataReader) throws SQLException {
		
		return dataReader.getTime();
	}
	
	public Object toJdbcType(Object value) {
		return BasicTypeConverter.toTime(value);
	}

	public Time toBeanType(Object value) {
		return BasicTypeConverter.toTime(value);
	}

	
	public String formatValue(Time v) {
        return v.toString();
    }

    public Time parse(String value) {
		return Time.valueOf(value);
	}
	
	public Time parseDateTime(long systemTimeMillis) {
		return new Time(systemTimeMillis);
	}

	public boolean isDateTimeCapable() {
		return true;
	}
    
    public Object readData(DataInput dataInput) throws IOException {
        if (!dataInput.readBoolean()) {
            return null;
        } else {
            String val = dataInput.readUTF();
            return parse(val);
        }
    }

    public void writeData(DataOutput dataOutput, Object v) throws IOException {
        
        Time value = (Time)v;
        if (value == null){
            dataOutput.writeBoolean(false);
        } else {
            dataOutput.writeBoolean(true);
            dataOutput.writeUTF(format(value));            
        }
    }
	
}

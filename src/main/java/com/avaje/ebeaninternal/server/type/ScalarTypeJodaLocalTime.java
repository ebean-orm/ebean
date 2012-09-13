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

import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

/**
 * ScalarType for Joda LocalTime. This maps to a JDBC Time.
 */
public class ScalarTypeJodaLocalTime extends ScalarTypeBase<LocalTime> {

	public ScalarTypeJodaLocalTime() {
		super(LocalTime.class, false, Types.TIME);
	}
	
	public void bind(DataBind b, LocalTime value) throws SQLException {
		if (value == null){
			b.setNull(Types.TIME);
		} else {
			Time sqlTime = new Time(value.getMillisOfDay());
			b.setTime(sqlTime);
		}
	}

	public LocalTime read(DataReader dataReader) throws SQLException {
		
		Time sqlTime = dataReader.getTime();
		if (sqlTime == null){
			return null;
		} else {
			return new LocalTime(sqlTime, DateTimeZone.UTC);
		}
	}
	
	public Object toJdbcType(Object value) {
		if (value instanceof LocalTime){
			return new Time(((LocalTime)value).getMillisOfDay());
		}
		return BasicTypeConverter.toTime(value);
	}

	public LocalTime toBeanType(Object value) {
		if (value instanceof java.util.Date){
			return new LocalTime(value, DateTimeZone.UTC);
		}
		return (LocalTime)value;
	}
	
	public String formatValue(LocalTime v) {
	    return v.toString();
    }

    public LocalTime parse(String value) {
        return new LocalTime(value);
	}
	
	public LocalTime parseDateTime(long systemTimeMillis) {
		return new LocalTime(systemTimeMillis);
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

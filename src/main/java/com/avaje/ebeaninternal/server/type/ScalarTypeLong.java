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

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

/**
 * ScalarType for Long and long.
 */
public class ScalarTypeLong extends ScalarTypeBase<Long> {

	public ScalarTypeLong() {
		super(Long.class, true, Types.BIGINT);
	}
	
	public void bind(DataBind b, Long value) throws SQLException {
		if (value == null){
			b.setNull(Types.BIGINT);
		} else {
			b.setLong(value.longValue());
		}
	}
    
	public Long read(DataReader dataReader) throws SQLException {
		
		return dataReader.getLong();
	}
	
	public Object toJdbcType(Object value) {
		return BasicTypeConverter.toLong(value);
	}

	public Long toBeanType(Object value) {
		return BasicTypeConverter.toLong(value);
	}
	
	public String formatValue(Long t) {
        return t.toString();
    }

    public Long parse(String value) {
		return Long.valueOf(value);
	}
	
	public Long parseDateTime(long systemTimeMillis) {
		return Long.valueOf(systemTimeMillis);
	}

	public boolean isDateTimeCapable() {
		return true;
	}

    public Object readData(DataInput dataInput) throws IOException {
        if (!dataInput.readBoolean()) {
            return null;
        } else {
            long val = dataInput.readLong();
            return Long.valueOf(val);
        }
    }

    public void writeData(DataOutput dataOutput, Object v) throws IOException {
        
        Long value = (Long)v;
        if (value == null){
            dataOutput.writeBoolean(false);
        } else {
            dataOutput.writeBoolean(true);
            dataOutput.writeLong(value.longValue());            
        }
    }
}

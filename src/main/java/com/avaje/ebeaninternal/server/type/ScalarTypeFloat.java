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
 * ScalarType for Float and float.
 */
public class ScalarTypeFloat extends ScalarTypeBase<Float> {

	public ScalarTypeFloat() {
		super(Float.class, true, Types.REAL);
	}
	
	public void bind(DataBind b, Float value) throws SQLException {
		if (value == null){
			b.setNull(Types.REAL);
		} else {
			b.setFloat(value.floatValue());
		}
	}

	public Float read(DataReader dataReader) throws SQLException {
		
		return dataReader.getFloat();
	}
	
	public Object toJdbcType(Object value) {
		return BasicTypeConverter.toFloat(value);
	}

	public Float toBeanType(Object value) {
		return BasicTypeConverter.toFloat(value);
	}

    public String formatValue(Float t) {
        return t.toString();
    }
	
	public Float parse(String value) {
		return Float.valueOf(value);
	}
	
	public Float parseDateTime(long systemTimeMillis) {
		return Float.valueOf(systemTimeMillis);
	}

	public boolean isDateTimeCapable() {
		return true;
	}

    public String toJsonString(Float value) {
        if(value.isInfinite() || value.isNaN()) {
            return "null";
        } else {
            return value.toString();
        }
    }
    
    public Object readData(DataInput dataInput) throws IOException {
        if (!dataInput.readBoolean()) {
            return null;
        } else {
            float val = dataInput.readFloat();
            return Float.valueOf(val);
        }
    }

    public void writeData(DataOutput dataOutput, Object v) throws IOException {
        
        Float value = (Float)v;
        if (value == null){
            dataOutput.writeBoolean(false);
        } else {
            dataOutput.writeBoolean(true);
            dataOutput.writeFloat(value.floatValue());            
        }
    }
}

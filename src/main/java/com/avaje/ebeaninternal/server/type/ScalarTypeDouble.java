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
 * ScalarType for Double and double.
 */
public class ScalarTypeDouble extends ScalarTypeBase<Double> {
	
	public ScalarTypeDouble() {
		super(Double.class, true, Types.DOUBLE);
	}
	
	public void bind(DataBind b, Double value) throws SQLException {
		if (value == null){
			b.setNull(Types.DOUBLE);
		} else {
			b.setDouble(value.doubleValue());
		}
	}

	public Double read(DataReader dataReader) throws SQLException {
		
		return dataReader.getDouble();
	}
	
	public Object toJdbcType(Object value) {
		return BasicTypeConverter.toDouble(value);
	}

	public Double toBeanType(Object value) {
		return BasicTypeConverter.toDouble(value);
	}

	
	public String formatValue(Double t) {
        return t.toString();
    }

    public Double parse(String value) {
		return Double.valueOf(value);
	}

	public Double parseDateTime(long systemTimeMillis) {
		return Double.valueOf(systemTimeMillis);
	}

	public boolean isDateTimeCapable() {
		return true;
	}

    public String toJsonString(Double value) {
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
            double val = dataInput.readDouble();
            return Double.valueOf(val);
        }
    }

    public void writeData(DataOutput dataOutput, Object v) throws IOException {
        
        Double value = (Double)v;
        if (value == null){
            dataOutput.writeBoolean(false);
        } else {
            dataOutput.writeBoolean(true);
            dataOutput.writeDouble(value.doubleValue());            
        }
    }
}

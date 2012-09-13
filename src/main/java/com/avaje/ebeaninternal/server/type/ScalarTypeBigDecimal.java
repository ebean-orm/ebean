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
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

/**
 * ScalarType for BigDecimal.
 */
public class ScalarTypeBigDecimal extends ScalarTypeBase<BigDecimal> {

	public ScalarTypeBigDecimal() {
		super(BigDecimal.class, true, Types.DECIMAL);
	}
	
    public Object readData(DataInput dataInput) throws IOException {
        if (!dataInput.readBoolean()) {
            return null;
        } else {
            double val = dataInput.readDouble();
            return new BigDecimal(val);
        }
    }

    public void writeData(DataOutput dataOutput, Object v) throws IOException {
        
        BigDecimal b = (BigDecimal)v;
        if (b == null){
            dataOutput.writeBoolean(false);
        } else {
            dataOutput.writeBoolean(true);
            dataOutput.writeDouble(b.doubleValue());            
        }
    }
    
	public void bind(DataBind b, BigDecimal value) throws SQLException {
		if (value == null){
			b.setNull(Types.DECIMAL);
		} else {
			b.setBigDecimal(value);
		}
	}

	public BigDecimal read(DataReader dataReader) throws SQLException {
		
		return dataReader.getBigDecimal();
	}
	
	public Object toJdbcType(Object value) {
		return BasicTypeConverter.toBigDecimal(value);
	}

	public BigDecimal toBeanType(Object value) {
		return BasicTypeConverter.toBigDecimal(value);
	}

    public String formatValue(BigDecimal t) {
        return t.toPlainString();
    }

    public BigDecimal parse(String value) {
		return new BigDecimal(value);
	}

	public BigDecimal parseDateTime(long systemTimeMillis) {
		return BigDecimal.valueOf(systemTimeMillis);
	}

	public boolean isDateTimeCapable() {
		return true;
	}
	
}

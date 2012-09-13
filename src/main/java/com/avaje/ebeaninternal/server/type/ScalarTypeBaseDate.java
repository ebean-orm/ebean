/**
 * Copyright (C) 2009 Authors
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
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;

import com.avaje.ebean.text.json.JsonValueAdapter;
import com.avaje.ebeaninternal.server.text.json.WriteJsonBuffer;

/**
 * Base class for Date types.
 */
public abstract class ScalarTypeBaseDate<T> extends ScalarTypeBase<T> {

    public ScalarTypeBaseDate(Class<T> type, boolean jdbcNative, int jdbcType) {
        super(type, jdbcNative, jdbcType);
    }
    
    public abstract java.sql.Date convertToDate(T t);
    
    public abstract T convertFromDate(java.sql.Date ts);
    
    public void bind(DataBind b, T value) throws SQLException {
        if (value == null){
            b.setNull(Types.DATE);
        } else {
            Date date = convertToDate(value);
            b.setDate(date);
        }
    }

    public T read(DataReader dataReader) throws SQLException {
        
        Date ts = dataReader.getDate();
        if (ts == null){
            return null;
        } else {
            return convertFromDate(ts);
        }
    }
    
    public String formatValue(T t) {
        Date date = convertToDate(t);
        return date.toString();
    }

    public T parse(String value) {
        Date date = Date.valueOf(value);
        return convertFromDate(date);
    }
    
    public T parseDateTime(long systemTimeMillis) {
        Date ts = new Date(systemTimeMillis);
        return convertFromDate(ts);
    }

    public boolean isDateTimeCapable() {
        return true;
    }
    
    @Override
    public String jsonToString(T value, JsonValueAdapter ctx) {
        Date date = convertToDate(value);
        return ctx.jsonFromDate(date);
    }
    
    @Override
    public void jsonWrite(WriteJsonBuffer buffer, T value, JsonValueAdapter ctx) {
    	String s = jsonToString(value, ctx);
    	buffer.append(s);
    }

	@Override
    public T jsonFromString(String value, JsonValueAdapter ctx) {
        Date ts = ctx.jsonToDate(value);
        return convertFromDate(ts);
    }

    public Object readData(DataInput dataInput) throws IOException {
        if (!dataInput.readBoolean()) {
            return null;
        } else {
            long val = dataInput.readLong();
            Date date = new Date(val);
            return convertFromDate(date);
        }
    }

    @SuppressWarnings("unchecked")
    public void writeData(DataOutput dataOutput, Object v) throws IOException {
        
        T value = (T)v;
        if (value == null){
            dataOutput.writeBoolean(false);
        } else {
            dataOutput.writeBoolean(true);
            Date date = convertToDate(value);
            dataOutput.writeLong(date.getTime());            
        }
    }
    
}

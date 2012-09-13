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
import java.sql.SQLException;

import com.avaje.ebean.text.json.JsonValueAdapter;
import com.avaje.ebeaninternal.server.text.json.WriteJsonBuffer;

public class ScalarTypeEncryptedWrapper<T> implements ScalarType<T> {

    private final ScalarType<T> wrapped;
    
    private final DataEncryptSupport dataEncryptSupport;
    
    private final ScalarTypeBytesBase byteArrayType;
    
    public ScalarTypeEncryptedWrapper(ScalarType<T> wrapped, ScalarTypeBytesBase byteArrayType, DataEncryptSupport dataEncryptSupport) {
        this.wrapped = wrapped;
        this.byteArrayType = byteArrayType;
        this.dataEncryptSupport = dataEncryptSupport;
    }

    public Object readData(DataInput dataInput) throws IOException {
        return wrapped.readData(dataInput);
    }

    public void writeData(DataOutput dataOutput, Object v) throws IOException {
        wrapped.writeData(dataOutput, v);
    }

    public T read(DataReader dataReader) throws SQLException {

        byte[] data = dataReader.getBytes();
        String formattedValue = dataEncryptSupport.decryptObject(data);
        if (formattedValue == null){
            return null;
        }
        return wrapped.parse(formattedValue);
    }

    private byte[] encrypt(T value){
        String formatValue = wrapped.formatValue(value);
        return dataEncryptSupport.encryptObject(formatValue);
    }
    
    public void bind(DataBind b, T value) throws SQLException {
        
        byte[] encryptedValue = encrypt(value);
        byteArrayType.bind(b, encryptedValue);
    }

    public int getJdbcType() {
        return byteArrayType.getJdbcType();
    }

    public int getLength() {
        return byteArrayType.getLength();
    }

    public Class<T> getType() {
        return wrapped.getType();
    }

    public boolean isDateTimeCapable() {
        return wrapped.isDateTimeCapable();
    }

    public boolean isJdbcNative() {
        return false;
    }

    public void loadIgnore(DataReader dataReader) {
        wrapped.loadIgnore(dataReader);
    }
    
    @SuppressWarnings("unchecked")
    public String format(Object v) {
        return formatValue((T)v);
    }

    public String formatValue(T v) {
        return wrapped.formatValue(v);
    }

    public T parse(String value) {
        return wrapped.parse(value);
    }

    public T parseDateTime(long systemTimeMillis) {
        return wrapped.parseDateTime(systemTimeMillis);
    }

    public T toBeanType(Object value) {
        return wrapped.toBeanType(value);
    }

    public Object toJdbcType(Object value) {
        return wrapped.toJdbcType(value);
    }

    public void accumulateScalarTypes(String propName, CtCompoundTypeScalarList list) {
        wrapped.accumulateScalarTypes(propName, list);
    }

    public String jsonToString(T value, JsonValueAdapter ctx) {
        return wrapped.jsonToString(value, ctx);
    }
    
    public void jsonWrite(WriteJsonBuffer buffer, T value, JsonValueAdapter ctx) {
	    wrapped.jsonWrite(buffer, value, ctx);
    }

	public T jsonFromString(String value, JsonValueAdapter ctx) {
        return wrapped.jsonFromString(value, ctx);
    }
    
}

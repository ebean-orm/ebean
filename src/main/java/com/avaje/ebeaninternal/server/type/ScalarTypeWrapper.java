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

import com.avaje.ebean.config.ScalarTypeConverter;
import com.avaje.ebean.text.json.JsonValueAdapter;
import com.avaje.ebeaninternal.server.text.json.WriteJsonBuffer;

/**
 * A ScalarType that uses a ScalarTypeConverter to convert to and from another
 * underlying ScalarType.
 * <p>
 * Enables the use of a simple interface to add additional scalarTypes.
 * </p>
 * 
 * @author rbygrave
 * 
 * @param <B>
 *            the logical type
 * @param <S>
 *            the underlying scalar type this is converted to
 */
public class ScalarTypeWrapper<B, S> implements ScalarType<B> {

    private final ScalarType<S> scalarType;
    private final ScalarTypeConverter<B, S> converter;
    private final Class<B> wrapperType;

    private final B nullValue;
    
    public ScalarTypeWrapper(Class<B> wrapperType, ScalarType<S> scalarType, ScalarTypeConverter<B, S> converter) {
        this.scalarType = scalarType;
        this.converter = converter;
        this.nullValue = converter.getNullValue();
        this.wrapperType = wrapperType;
    }

    public String toString() {
        return "ScalarTypeWrapper " + wrapperType + " to " + scalarType.getType();
    }
    
    @SuppressWarnings("unchecked")
    public Object readData(DataInput dataInput) throws IOException {
        Object v = scalarType.readData(dataInput);
        return converter.wrapValue((S)v);
    }

    @SuppressWarnings("unchecked")
    public void writeData(DataOutput dataOutput, Object v) throws IOException {
        S sv = converter.unwrapValue((B)v);
        scalarType.writeData(dataOutput, sv);
    }

    public void bind(DataBind b, B value) throws SQLException {
        if (value == null) {
            scalarType.bind(b, null);
        } else {
            S sv = converter.unwrapValue(value);
            scalarType.bind(b, sv);
        }
    }

    public int getJdbcType() {
        return scalarType.getJdbcType();
    }

    public int getLength() {
        return scalarType.getLength();
    }

    public Class<B> getType() {
        return wrapperType;
    }

    public boolean isDateTimeCapable() {
        return scalarType.isDateTimeCapable();
    }

    public boolean isJdbcNative() {
        return false;
    }

    @SuppressWarnings("unchecked")
    public String format(Object v) {
        return formatValue((B)v);
    }

    public String formatValue(B v) {
        S sv = converter.unwrapValue(v);
        return scalarType.formatValue(sv);
    }

    public B parse(String value) {
        S sv = scalarType.parse(value);
        if (sv == null) {
            return nullValue;
        }
        return converter.wrapValue(sv);
    }

    public B parseDateTime(long systemTimeMillis) {
        S sv = scalarType.parseDateTime(systemTimeMillis);
        if (sv == null) {
            return nullValue;
        }
        return converter.wrapValue(sv);
    }

    public void loadIgnore(DataReader dataReader) {
        dataReader.incrementPos(1);
    }

    public B read(DataReader dataReader) throws SQLException {

        S sv = scalarType.read(dataReader);
        if (sv == null) {
            return nullValue;
        }
        return converter.wrapValue(sv);
    }

    @SuppressWarnings("unchecked")
    public B toBeanType(Object value) {
        if (value == null) {
            return nullValue;
        }
        if (getType().isAssignableFrom(value.getClass())) {
            return (B) value;
        }
        if (value instanceof String) {
            return parse((String) value);
        }
        S sv = scalarType.toBeanType(value);
        return converter.wrapValue(sv);
    }

    @SuppressWarnings("unchecked")
    public Object toJdbcType(Object value) {

        Object sv = converter.unwrapValue((B) value);
        if (sv == null) {
            return nullValue;
        }
        return scalarType.toJdbcType(sv);
    }

    public void accumulateScalarTypes(String propName, CtCompoundTypeScalarList list) {
        list.addScalarType(propName, this);
    }

    public ScalarType<?> getScalarType() {
        return this;
    }

    public String jsonToString(B value, JsonValueAdapter ctx) {
        
        S sv = converter.unwrapValue(value);
        return scalarType.jsonToString(sv, ctx);
    }

    public void jsonWrite(WriteJsonBuffer buffer, B value, JsonValueAdapter ctx) {
    	S sv = converter.unwrapValue(value);
        scalarType.jsonWrite(buffer, sv, ctx);
    }

	public B jsonFromString(String value, JsonValueAdapter ctx) {
        S s = scalarType.jsonFromString(value, ctx);
        return converter.wrapValue(s);
    }
    
}

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

import com.avaje.ebean.text.json.JsonValueAdapter;
import com.avaje.ebeaninternal.server.text.json.WriteJsonBuffer;



/**
 * Base ScalarType object.
 */
public abstract class ScalarTypeBase<T> implements ScalarType<T> {

	protected final Class<T> type;
	protected final boolean jdbcNative;
	protected final int jdbcType;
	
	public ScalarTypeBase(Class<T> type, boolean jdbcNative, int jdbcType) {
		this.type = type;
		this.jdbcNative = jdbcNative;
		this.jdbcType = jdbcType;
	}
	
	/**
	 * Just return 0.
	 */
	public int getLength() {
		return 0;
	}

	public boolean isJdbcNative() {
		return jdbcNative;
	}
	
	public int getJdbcType() {
		return jdbcType;
	}
	
	public Class<T> getType() {
		return type;
	}

	@SuppressWarnings("unchecked")
    public String format(Object v) {
        return formatValue((T)v);
    }

    /**
	 * Return true if the value is null.
	 */
	public boolean isDbNull(Object value) {
		return value == null;
	}

	/**
	 * Returns the value that was passed in.
	 */
	public Object getDbNullValue(Object value) {
		return value;
	}

    public void loadIgnore(DataReader dataReader) {  
        dataReader.incrementPos(1);
    }
	
    public void accumulateScalarTypes(String propName, CtCompoundTypeScalarList list) {
        list.addScalarType(propName, this);
    }

    public void jsonWrite(WriteJsonBuffer buffer, T value, JsonValueAdapter ctx) {
    	String v = jsonToString(value, ctx);
    	buffer.append(v);
    }
    
    public String jsonToString(T value, JsonValueAdapter ctx) {
        return formatValue(value);
    }
    
    public T jsonFromString(String value, JsonValueAdapter ctx) {
        return parse(value);
    }
    
}

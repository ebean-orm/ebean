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

import java.util.UUID;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

/**
 * ScalarType for java.util.UUID which converts to and from a VARCHAR database column.
 */
public class ScalarTypeUUID extends ScalarTypeBaseVarchar<UUID> {

	public ScalarTypeUUID() {
		super(UUID.class);
	}
		
	@Override
	public int getLength() {
		return 40;
	}

	@Override
    public UUID convertFromDbString(String dbValue) {
	    return UUID.fromString(dbValue);
    }

    @Override
    public String convertToDbString(UUID beanValue) {
        return formatValue(beanValue);
    }

	public UUID toBeanType(Object value) {
		return BasicTypeConverter.toUUID(value);
	}
	
	public Object toJdbcType(Object value) {
		return BasicTypeConverter.convert(value, jdbcType);
	}
	
	public String formatValue(UUID v) {
        return v.toString();
    }

    public UUID parse(String value) {
		return UUID.fromString(value);
	}

}

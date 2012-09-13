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

import java.util.TimeZone;

/**
 * ScalarType for java.util.TimeZone which converts to and from a VARCHAR database column.
 */
public class ScalarTypeTimeZone extends ScalarTypeBaseVarchar<TimeZone> {

	public ScalarTypeTimeZone() {
		super(TimeZone.class);
	}
	
    @Override
    public int getLength() {
        return 20;
    }
    
	@Override
    public TimeZone convertFromDbString(String dbValue) {
        return TimeZone.getTimeZone(dbValue);
    }

    @Override
    public String convertToDbString(TimeZone beanValue) {
        return ((TimeZone)beanValue).getID();
    }

	public String formatValue(TimeZone v) {
        return v.toString();
    }

    public TimeZone parse(String value) {
		return TimeZone.getTimeZone(value);
	}
		
}

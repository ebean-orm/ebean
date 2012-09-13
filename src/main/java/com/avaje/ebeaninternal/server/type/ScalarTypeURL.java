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

import java.net.MalformedURLException;
import java.net.URL;

import com.avaje.ebean.text.TextException;

/**
 * ScalarType for java.net.URL which converts to and from a VARCHAR database column.
 */
public class ScalarTypeURL extends ScalarTypeBaseVarchar<URL> {

	public ScalarTypeURL() {
		super(URL.class);
	}
	
	
	@Override
    public URL convertFromDbString(String dbValue) {
	    try {
            return new URL(dbValue);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error with URL ["+dbValue+"] "+e);
        }
    }

    @Override
    public String convertToDbString(URL beanValue) {
        return formatValue(beanValue);
    }

	public String formatValue(URL v) {
        return v.toString();
    }

    public URL parse(String value) {
		try {
			return new URL(value);
		} catch (MalformedURLException e) {
			throw new TextException(e);
		}
	}
}

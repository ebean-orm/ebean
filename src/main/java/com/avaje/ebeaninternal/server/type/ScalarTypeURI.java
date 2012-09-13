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

import java.net.URI;
import java.net.URISyntaxException;

import com.avaje.ebean.text.TextException;

/**
 * ScalarType for java.net.URI which converts to and from a VARCHAR database column.
 */
public class ScalarTypeURI extends ScalarTypeBaseVarchar<URI> {

	public ScalarTypeURI() {
		super(URI.class);
	}
	
	@Override
    public URI convertFromDbString(String dbValue) {
	    try {
            return new URI(dbValue);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Error with URI ["+dbValue+"] "+e);
        }
	}

    @Override
    public String convertToDbString(URI beanValue) {
        return beanValue.toString();
    }

	public String formatValue(URI v) {
        return v.toString();
    }

    public URI parse(String value) {
		try {
			return new URI(value);
		} catch (URISyntaxException e) {
			throw new TextException("Error with URI ["+value+"] ", e);
		}
	}
}

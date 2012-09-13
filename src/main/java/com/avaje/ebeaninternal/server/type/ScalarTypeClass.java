/**
 * Copyright (C) 2010  Authors
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

import javax.persistence.PersistenceException;

/**
 * ScalarType for Class that persists it to VARCHAR column.
 * 
 * @author emcgreal
 * @author rbygrave
 */
@SuppressWarnings({ "rawtypes" })
public class ScalarTypeClass extends ScalarTypeBaseVarchar<Class> {
	
    public ScalarTypeClass() {
        super(Class.class);
    }
    
    @Override
    public int getLength() {
        return 255;
    }

    @Override
    public Class<?> convertFromDbString(String dbValue) {
        return parse(dbValue);
    }
    
    @Override
    public String convertToDbString(Class beanValue) {
        return beanValue.getCanonicalName();
    }

    public String formatValue(Class v) {
        return v.getCanonicalName();
    }

    public Class<?> parse(String value) {
        try {
            return Class.forName(value);
        } catch (Exception e) {
            String msg = "Unable to find Class "+value;
            throw new PersistenceException(msg, e);
        }
    }
	
	
}

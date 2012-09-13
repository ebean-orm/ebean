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
package com.avaje.ebeaninternal.server.type.reflect;

import java.lang.reflect.Method;

import com.avaje.ebean.config.CompoundTypeProperty;

@SuppressWarnings({ "rawtypes" })
public class ReflectionBasedCompoundTypeProperty implements CompoundTypeProperty {

    private static final Object[] NO_ARGS = new Object[0];
    
    private final Method reader;
    
    private final String name;
    
    private final Class<?> propertyType;
    
    public ReflectionBasedCompoundTypeProperty(String name, Method reader, Class<?> propertyType) {
        this.name = name;
        this.reader = reader;
        this.propertyType = propertyType;
    }
    
    public String toString() {
        return name;
    }
    
    public int getDbType() {
        return 0;
    }

    public String getName() {
        return name;
    }

    public Object getValue(Object valueObject) {
        
        try {
            return reader.invoke(valueObject, NO_ARGS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } 
    }

    public Class<?> getPropertyType(){
        return propertyType;
    }
    
    
}

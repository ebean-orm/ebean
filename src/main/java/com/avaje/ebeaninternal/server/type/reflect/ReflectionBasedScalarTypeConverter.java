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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import com.avaje.ebean.config.ScalarTypeConverter;

@SuppressWarnings({ "rawtypes" })
public class ReflectionBasedScalarTypeConverter implements ScalarTypeConverter {

    private static final Object[] NO_ARGS = new Object[0];
    
    private final Constructor<?> constructor;
    
    private final Method reader;
    
    public ReflectionBasedScalarTypeConverter(Constructor<?> constructor, Method reader) {
        this.constructor = constructor;
        this.reader = reader;
    }
    
    public Object getNullValue() {
        return null;
    }

    public Object unwrapValue(Object beanType) {
        if (beanType == null){
            return null;
        }
        try {
            return reader.invoke(beanType, NO_ARGS);
        } catch (Exception e) {
            String msg = "Error invoking read method "+reader.getName()
                        +" on "+beanType.getClass().getName();
            throw new RuntimeException(msg);
        } 
    }

    public Object wrapValue(Object scalarType) {
        try {
            return constructor.newInstance(scalarType);
        } catch (Exception e) {
            String msg = "Error invoking constructor "+constructor+" with "+scalarType;
            throw new RuntimeException(msg);
        } 
    }

    
}

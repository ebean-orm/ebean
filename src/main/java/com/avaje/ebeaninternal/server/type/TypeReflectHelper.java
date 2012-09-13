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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeReflectHelper {

    public static Class<?>[] getParams(Class<?> cls, Class<?> matchRawType) {

        Type[] types = getParamType(cls, matchRawType);
        Class<?>[] result = new Class<?>[types.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = getClass(types[i]);
        }
        return result;
    }
    
    public static Class<?> getClass(Type type){
        
        if (type instanceof ParameterizedType){
            return getClass(((ParameterizedType)type).getRawType());
        }
        
        return (Class<?>)type;
    }
    
    private static Type[] getParamType(Class<?> cls, Class<?> matchRawType) {
        
        Type[] gis = cls.getGenericInterfaces();
        for (int i = 0; i < gis.length; i++) {
            Type type = gis[i];
            if (type instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) type;
                Type rawType = paramType.getRawType();
                if (rawType.equals(matchRawType)) {
                    
                    return paramType.getActualTypeArguments();                    
                }
            }
        }
        return null;
    }
}

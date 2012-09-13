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
import java.util.Arrays;

import com.avaje.ebean.config.CompoundType;
import com.avaje.ebean.config.CompoundTypeProperty;

@SuppressWarnings({ "rawtypes" })
public class ReflectionBasedCompoundType implements CompoundType {

    private final Constructor<?> constructor;
    
    private final ReflectionBasedCompoundTypeProperty[] props;
    
    public ReflectionBasedCompoundType(Constructor<?> constructor, ReflectionBasedCompoundTypeProperty[] props) {
        this.constructor = constructor;
        this.props = props;
    }
    
    public String toString() {
        return "ReflectionBasedCompoundType "+constructor+" "+Arrays.toString(props);
    }
    
    public Object create(Object[] propertyValues) {
        
        try {
            return constructor.newInstance(propertyValues);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } 
    }

    public CompoundTypeProperty[] getProperties() {
        return props;
    }

    public Class<?> getPropertyType(int i){
        return props[i].getPropertyType();
    }
    
    public Class<?> getCompoundType() {
        return constructor.getDeclaringClass();
    }
    
}

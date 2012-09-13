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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.avaje.ebean.config.CompoundTypeProperty;
import com.avaje.ebeaninternal.server.query.SplitName;

/**
 * Used to build a flat list of all the scalar types nested in a compound type.
 * 
 * @author rbygrave
 */
public final class CtCompoundTypeScalarList {

    private final LinkedHashMap<String,ScalarType<?>> scalarProps = new LinkedHashMap<String,ScalarType<?>>();

    private final LinkedHashMap<String,CtCompoundProperty> compoundProperties = new LinkedHashMap<String, CtCompoundProperty>();
    
    /**
     * Return the list of non-scalar properties. These occur when compound types are nested.
     */
    public List<CtCompoundProperty> getNonScalarProperties() {
        
        List<CtCompoundProperty> nonScalarProps = new ArrayList<CtCompoundProperty>();
        
        for (String propKey: compoundProperties.keySet()) {
            if (!scalarProps.containsKey(propKey)){
                nonScalarProps.add(compoundProperties.get(propKey));
            }
        }
        
        return nonScalarProps;
    }
    
    /**
     * Register a property with it's associated compound type and relative name.
     */
    public void addCompoundProperty(String propName, CtCompoundType<?> t, CompoundTypeProperty<?,?> prop) {
        
        CtCompoundProperty parent = null;
        String[] split = SplitName.split(propName);
        if (split[0] != null){
            parent = compoundProperties.get(split[0]);
        }
                
        CtCompoundProperty p = new CtCompoundProperty(propName, parent, t, prop);
        compoundProperties.put(propName, p);
    }
    
    /**
     * Register a scalarType used in the compound type with its given property name.
     */
    public void addScalarType(String propName, ScalarType<?> scalar){
        scalarProps.put(propName, scalar);
    }
    
    public CtCompoundProperty getCompoundType(String propName) {
        return compoundProperties.get(propName);
    }
    
    public Set<Entry<String, ScalarType<?>>> entries() {
         return scalarProps.entrySet();
    }
    
}

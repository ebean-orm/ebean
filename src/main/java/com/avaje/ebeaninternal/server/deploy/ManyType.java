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
package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebeaninternal.api.SpiQuery;

/**
 * Represents the type of a OneToMany or ManyToMany property.
 */
public class ManyType {

    public static final ManyType JAVA_LIST = new ManyType(Underlying.LIST);
    public static final ManyType JAVA_SET = new ManyType(Underlying.SET);
    public static final ManyType JAVA_MAP = new ManyType(Underlying.MAP);
    
    public enum Underlying {
        LIST,
        SET,
        MAP
    }
    
    private final SpiQuery.Type queryType;
    
    private final Underlying underlying;
    
    private final CollectionTypeConverter typeConverter;

    private ManyType(Underlying underlying) {
        this(underlying, null);
    }

    public ManyType(Underlying underlying, CollectionTypeConverter typeConverter) {
        this.underlying = underlying;
        this.typeConverter = typeConverter;
        switch (underlying) {
        case LIST:
            queryType = SpiQuery.Type.LIST;
            break;
        case SET:
            queryType = SpiQuery.Type.SET;
            break;

        default:
            queryType = SpiQuery.Type.MAP;
            break;
        }        
    }

    /**
     * Return the matching Query type.
     */
    public SpiQuery.Type getQueryType() {
        return queryType;
    }
    
    /**
     * Return the underlying type.
     */
    public Underlying getUnderlying() {
        return underlying;
    }

    /**
     * Return the type converter if there is one.
     */
    public CollectionTypeConverter getTypeConverter() {
        return typeConverter;
    }
    
}

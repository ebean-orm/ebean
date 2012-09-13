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
package com.avaje.ebeaninternal.server.deploy.generatedproperty;

import java.util.Date;

import com.avaje.ebeaninternal.server.deploy.BeanProperty;

/**
 * Generate a (java.util.Date) Timestamp whenever the bean is inserted or
 * updated.
 */
public class GeneratedUpdateDate implements GeneratedProperty {

    /**
     * Return now as a Timestamp.
     */
    public Object getInsertValue(BeanProperty prop, Object bean) {
        return new Date(System.currentTimeMillis());
    }

    /**
     * Return now as a Timestamp.
     */
    public Object getUpdateValue(BeanProperty prop, Object bean) {
        return new Date(System.currentTimeMillis());
    }

    /**
     * For dynamic table updates make sure this is included.
     */
    public boolean includeInUpdate() {
        return true;
    }

    /**
     * Include this in every insert.
     */
    public boolean includeInInsert() {
        return true;
    }

    public boolean isDDLNotNullable() {
        return true;
    }

}

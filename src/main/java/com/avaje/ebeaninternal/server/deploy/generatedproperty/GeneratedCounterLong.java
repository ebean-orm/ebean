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

import com.avaje.ebeaninternal.server.deploy.BeanProperty;

/**
 * Used to create a counter version column for Long.
 */
public class GeneratedCounterLong implements GeneratedProperty {

    public GeneratedCounterLong() {

    }

    /**
     * Always returns a 1.
     */
    public Object getInsertValue(BeanProperty prop, Object bean) {
        return Long.valueOf(1);
    }

    /**
     * Increments the current value by one.
     */
    public Object getUpdateValue(BeanProperty prop, Object bean) {
        Long i = (Long) prop.getValue(bean);
        return Long.valueOf(i.longValue() + 1);
    }

    /**
     * Include this in every update.
     */
    public boolean includeInUpdate() {
        return true;
    }

    /**
     * Include this in every insert setting initial counter value to 1.
     */
    public boolean includeInInsert() {
        return true;
    }

    public boolean isDDLNotNullable() {
        return true;
    }

}

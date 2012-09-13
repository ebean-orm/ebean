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

import java.sql.Timestamp;

import com.avaje.ebeaninternal.server.deploy.BeanProperty;

/**
 * Used to generate a timestamp when a bean is inserted.
 */
public class GeneratedInsertTimestamp implements GeneratedProperty {

    /**
     * Return the current time as a Timestamp.
     */
    public Object getInsertValue(BeanProperty prop, Object bean) {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * Just returns the beans original insert timestamp value.
     */
    public Object getUpdateValue(BeanProperty prop, Object bean) {
        return prop.getValue(bean);
    }

    /**
     * Return false.
     */
    public boolean includeInUpdate() {
        return false;
    }

    /**
     * Return true.
     */
    public boolean includeInInsert() {
        return true;
    }

    public boolean isDDLNotNullable() {
        return true;
    }

}

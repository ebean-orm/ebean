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
 * Used to generate values for a property rather than have then set by the user.
 * For example generate the update timestamp when a bean is updated.
 */
public interface GeneratedProperty {

    /**
     * Get the generated insert value for a specific property of a bean.
     */
    public Object getInsertValue(BeanProperty prop, Object bean);

    /**
     * Get the generated update value for a specific property of a bean.
     */
    public Object getUpdateValue(BeanProperty prop, Object bean);

    /**
     * Return true if this should always be includes in an update statement.
     * <p>
     * Used to include GeneratedUpdateTimestamp in dynamic table updates.
     * </p>
     */
    public boolean includeInUpdate();

    /**
     * Return true if this should be included in insert statements.
     */
    public boolean includeInInsert();

    /**
     * Return true if the GeneratedProperty implies the DDL to create the DB
     * column should have a not null constraint.
     */
    public boolean isDDLNotNullable();

}

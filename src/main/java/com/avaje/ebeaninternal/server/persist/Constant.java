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
package com.avaje.ebeaninternal.server.persist;

/**
 * Contants used in persist.
 */
public interface Constant {

    /**
     * An INSERT clause.
     */
    public static final int IN_INSERT = 1;
    
    /**
     * An UPDATE SET clause.
     */
    public static final int IN_UPDATE_SET = 2;
    
    /**
     * An UPDATE WHERE clause.
     */
    public static final int IN_UPDATE_WHERE = 3;
    
    /**
     * A DELETE WHERE clause.
     */
    public static final int IN_DELETE_WHERE = 4;
}

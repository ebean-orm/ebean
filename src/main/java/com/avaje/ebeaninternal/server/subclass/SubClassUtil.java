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
package com.avaje.ebeaninternal.server.subclass;


/**
 * Helper methods for generated sub classes.
 */
public class SubClassUtil implements GenSuffix {

    /**
     * Return true if this is a generated class.
     */
    public static boolean isSubClass(String className) {
        
        return (className.lastIndexOf(SUFFIX) != -1);
    }
    
    /**
     * Return the super class name given the generated className.
     */
    public static String getSuperClassName(String className){
        int dPos = className.lastIndexOf(SUFFIX);
        if (dPos > -1){
            return className.substring(0, dPos);
        }
        return className;
    }
    
}

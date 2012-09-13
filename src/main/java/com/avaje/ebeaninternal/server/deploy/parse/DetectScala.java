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
package com.avaje.ebeaninternal.server.deploy.parse;

import java.util.logging.Logger;

import com.avaje.ebeaninternal.api.ClassUtil;

/**
 * Used to detected if Scala support is required.
 * 
 * @author rbygrave
 */
public class DetectScala {

    private static final Logger logger = Logger.getLogger(DetectScala.class.getName());
    
    private static Class<?> scalaOptionClass = initScalaOptionClass();
    
    private static boolean hasScalaSupport = scalaOptionClass != null;
    
    private static Class<?> initScalaOptionClass() {
        try {
            return ClassUtil.forName("scala.Option");
        } catch (ClassNotFoundException e) {
            // scala not in the classpath...
            logger.fine("Scala type 'scala.Option' not found. Scala Support disabled.");
            return null;
        }
    }
    
    /**
     * Return true if scala is in the classpath.
     */
    public static boolean hasScalaSupport() {
        return hasScalaSupport;
    }

    /**
     * Return the scala.Option class or null if scala is not in the classpath.
     */
    public static Class<?> getScalaOptionClass() {
        return scalaOptionClass;
    }
}

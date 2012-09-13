/**
 * Copyright (C) 2010  Authors
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
package com.avaje.ebeaninternal.api;

import java.util.logging.Logger;


/**
 * Helper to find classes taking into account the context class loader.
 * 
 * @author rbygrave
 */
public class ClassUtil {
    
    private static final Logger logger = Logger.getLogger(ClassUtil.class.getName());

    private static boolean preferContext = true;

    /**
     * Load a class taking into account a context class loader (if present).
     */
    public static Class<?> forName(String name) throws ClassNotFoundException {
        return forName(name, null);
    }
    
    /**
     * Load a class taking into account a context class loader (if present).
     */
    public static Class<?> forName(String name, Class<?> caller) throws ClassNotFoundException {
        
        if (caller == null){
            caller = ClassUtil.class;
        }
        ClassLoadContext ctx = ClassLoadContext.of(caller, preferContext);
        
        return ctx.forName(name);
    }
    

    public static ClassLoader getClassLoader(Class<?> caller, boolean preferContext) {
        
        if (caller == null){
            caller = ClassUtil.class;
        }
        ClassLoadContext ctx = ClassLoadContext.of(caller, preferContext);
        ClassLoader classLoader = ctx.getDefault(preferContext);
        if (ctx.isAmbiguous()){
            logger.info("Ambigous ClassLoader (Context vs Caller) chosen "+classLoader);
        }
        return classLoader;
    }

    /**
     * Return true if the given class is present.
     */
    public static boolean isPresent(String className) {
        return isPresent(className, null);
    }
    
    /**
     * Return true if the given class is present.
     */
    public static boolean isPresent(String className, Class<?> caller) {
        try {
            forName(className, caller);
            return true;
        } catch (Throwable ex) {
            // Class or one of its dependencies is not present...
            return false;
        }
    }

    /**
     * Return a new instance of the class using the default constructor.
     */
    public static Object newInstance(String className) {
        return newInstance(className,null);
    }
    
    /**
     * Return a new instance of the class using the default constructor.
     */
    public static Object newInstance(String className, Class<?> caller) {
        
        try {
            Class<?> cls = forName(className, caller);
            return cls.newInstance();
        } catch (Exception e){
            String msg = "Error constructing "+className;
            throw new IllegalArgumentException(msg, e);
        }
    }
}


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
package com.avaje.ebeaninternal.server.core;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility object used for internationalising log messages.
 */
public class Message {

    private static final String bundle = "com.avaje.ebeaninternal.api.message";

    /**
     * Return a message that has a single argument.
     */
    public static String msg(String key, Object arg) {
        Object[] args = new Object[1];
        args[0] = arg;
        return MessageFormat.format(getPattern(key), args);
    }

    /**
     * Return a message that has a two arguments.
     */
    public static String msg(String key, Object arg, Object arg2) {
        Object[] args = new Object[2];
        args[0] = arg;
        args[1] = arg2;
        return MessageFormat.format(getPattern(key), args);
    }

    public static String msg(String key, Object arg, Object arg2, Object arg3) {
        Object[] args = new Object[3];
        args[0] = arg;
        args[1] = arg2;
        args[2] = arg3;
        return MessageFormat.format(getPattern(key), args);
    }

    /**
     * Return a message that has an array of arguments.
     */
    public static String msg(String key, Object[] args) {
        return MessageFormat.format(getPattern(key), args);
    }

    /**
     * Return a message that has a no arguments.
     */
    public static String msg(String key) {
        return MessageFormat.format(getPattern(key), new Object[0]);
    }

    private static String getPattern(String key) {
        try {
            ResourceBundle myResources = ResourceBundle.getBundle(bundle);
            return myResources.getString(key);
        } catch (MissingResourceException e) {
            return "MissingResource " + bundle + ":" + key;
        }
    }
   
}

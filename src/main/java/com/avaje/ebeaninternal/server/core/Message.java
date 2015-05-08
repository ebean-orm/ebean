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

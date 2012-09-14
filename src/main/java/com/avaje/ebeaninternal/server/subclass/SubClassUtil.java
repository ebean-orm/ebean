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

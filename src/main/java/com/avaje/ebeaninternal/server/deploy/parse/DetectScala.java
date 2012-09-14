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

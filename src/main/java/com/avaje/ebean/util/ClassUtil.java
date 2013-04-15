package com.avaje.ebean.util;

/**
 * Helper to find classes taking into account the context class loader.
 * 
 * @author rbygrave
 */
public class ClassUtil {

  /**
   * Return a new instance of the class using the default constructor.
   */
  public static Object newInstance(String className) {
    try {
      Class<?> cls = Class.forName(className);
      return cls.newInstance();
    } catch (Exception e) {
      String msg = "Error constructing " + className;
      throw new IllegalArgumentException(msg, e);
    }
  }

}

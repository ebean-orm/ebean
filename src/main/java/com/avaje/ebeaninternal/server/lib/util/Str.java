package com.avaje.ebeaninternal.server.lib.util;

/**
 * String utility for adding strings together.
 * <p>
 * Predicts a decent buffer size to append the strings into.
 */
public class Str {

  /**
   * Append strings together.
   */
  public static String add(String s0, String s1, String ... args) {
    
    // determine a decent buffer size
    int len = 16 + s0.length() + s1.length();
    for (int i = 0; i < args.length; i++) {
      len += args[i].length();
    }
    
    // append all the strings into the buffer 
    StringBuilder sb = new StringBuilder(len);
    sb.append(s0).append(s1);
    for (int i = 0; i < args.length; i++) {
      sb.append(args[i]);
    }
    return sb.toString();
  }
  
  /**
   * Append two strings together.
   */
  public static String add(String s0, String s1) {
    StringBuilder sb = new StringBuilder(s0.length() + s1.length() + 5);
    return sb.append(s0).append(s1).toString();
  }
  
}

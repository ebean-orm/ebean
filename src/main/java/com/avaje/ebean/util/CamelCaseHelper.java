package com.avaje.ebean.util;

public class CamelCaseHelper {

  /**
   * To camel from underscore.
   * 
   * @param underscore
   *          the underscore
   * 
   * @return the string
   */
  public static String toCamelFromUnderscore(String underscore) {

    StringBuilder result = new StringBuilder();
    String[] vals = underscore.split("_");

    for (int i = 0; i < vals.length; i++) {
      String lower = vals[i].toLowerCase();
      if (i > 0) {
        char c = Character.toUpperCase(lower.charAt(0));
        result.append(c);
        result.append(lower.substring(1));
      } else {
        result.append(lower);
      }
    }

    return result.toString();
  }
}

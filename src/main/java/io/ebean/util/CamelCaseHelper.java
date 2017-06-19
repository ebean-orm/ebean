package io.ebean.util;

public class CamelCaseHelper {

  /**
   * To camel from underscore.
   *
   * @param underscore the underscore
   * @return the string
   */
  public static String toCamelFromUnderscore(String underscore) {

    String[] vals = underscore.split("_");
    if (vals.length == 1) {
      return isUpperCase(underscore) ? underscore.toLowerCase() : underscore;
    }

    StringBuilder result = new StringBuilder();
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

  private static boolean isUpperCase(String underscore) {
    for (int i = 0; i < underscore.length(); i++) {
      if (Character.isLowerCase(underscore.charAt(i))) {
        return false;
      }
    }
    return true;
  }
}

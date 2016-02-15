package com.avaje.ebeaninternal.server.expression;

/**
 * Utility to help isSame methods.
 */
public class Same {

  /**
   * Return true if both values are null or both an not null.
   */
  public static boolean sameByNull(Object v1, Object v2) {
    return v1 == null ? v2 == null : v2 != null;
  }

  /**
   * Null safe equals check.
   */
  public static boolean sameByValue(Object v1, Object v2) {
    return v1 == null ? v2 == null : v1.equals(v2);
  }

  /**
   * Null safe check by sameByValue or sameByNull based on byValue.
   */
  public static boolean sameBy(boolean byValue, Object value, Object value1) {

    if (byValue) {
      return sameByValue(value, value1);
    } else {
      return sameByNull(value, value1);
    }
  }
}

package com.avaje.ebeaninternal.server.core;

/**
 * Helper for determining type casting for JSON and ARRAY expressions.
 */
public class PostgresCast {

  /**
   * Postgres CAST the type if necessary.
   * <p>
   * This is generally necessary for JSON expressions as text values always returned from the json operators used.
   * </p>
   */
  protected static String cast(Object value) {
    return cast(value, false);
  }

  /**
   * Postgres CAST the type if necessary additionally specify if DB ARRAY is used.
   */
  protected static String cast(Object value, boolean asArray) {

    if (value == null) {
      // for exists and not-exists expressions
      return "";
    }

    if (isIntegerType(value)) {
      return asArray ? "::integer[]" : "::integer";
    }
    if (isNumberType(value)) {
      return asArray ? "::decimal[]" : "::decimal";
    }
    if (isBooleanType(value)) {
      return asArray ? "::boolean[]" : "::boolean";
    }

    return "";
  }

  private static boolean isBooleanType(Object value) {
    return (value instanceof Boolean);
  }

  private static boolean isIntegerType(Object value) {
    return (value instanceof Integer) || (value instanceof Long);
  }

  private static boolean isNumberType(Object value) {
    return (value instanceof Number);
  }
}

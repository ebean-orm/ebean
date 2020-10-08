package io.ebeaninternal.server.expression.platform;

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
    if (value instanceof Integer) {
      return asArray ? "::integer[]" : "::integer";
    }
    if (value instanceof Long) {
      return asArray ? "::bigint[]" : "::bigint";
    }
    if (value instanceof Number) {
      return asArray ? "::decimal[]" : "::decimal";
    }
    if (value instanceof Boolean) {
      return asArray ? "::boolean[]" : "::boolean";
    }
    return "";
  }
}

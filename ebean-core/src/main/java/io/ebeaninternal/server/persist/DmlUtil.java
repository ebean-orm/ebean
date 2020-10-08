package io.ebeaninternal.server.persist;


/**
 * Utility object with helper methods for DML.
 */
public class DmlUtil {

  /**
   * Return true if the value is null or a Numeric 0 (for primitive int's and long's) or Option empty.
   */
  public static boolean isNullOrZero(Object value) {
    return value == null || value instanceof Number && ((Number) value).longValue() == 0L;
  }
}

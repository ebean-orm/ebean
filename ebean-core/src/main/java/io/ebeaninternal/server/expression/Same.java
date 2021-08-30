package io.ebeaninternal.server.expression;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * Utility to help isSame methods.
 */
final class Same {

  /**
   * Return true if both values are null or both an not null.
   */
  static boolean sameByNull(Object v1, Object v2) {
    return (v1 == null) == (v2 == null);
  }

  /**
   * Null safe equals check.
   */
  static boolean sameByValue(Object v1, Object v2) {
    return Objects.equals(v1, v2);
  }

  /**
   * Return true if both collections are the same by value and order is taken into account.
   */
  static boolean sameByValue(Collection<?> v1, Collection<?> v2) {
    if (v1 == null) {
      return v2 == null;
    }
    if (v2 == null || v1.size() != v2.size()) {
      return false;
    }
    Iterator<?> thisIt = v1.iterator();
    Iterator<?> thatIt = v2.iterator();
    while (thisIt.hasNext() && thatIt.hasNext()) {
      if (!thisIt.next().equals(thatIt.next())) {
        return false;
      }
    }
    return true;
  }

  /**
   * Null safe check by sameByValue or sameByNull based on byValue.
   */
  static boolean sameBy(boolean byValue, Object value, Object value1) {
    if (byValue) {
      return sameByValue(value, value1);
    } else {
      return sameByNull(value, value1);
    }
  }
}

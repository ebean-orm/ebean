package io.ebeaninternal.server.el;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator for use with the expression objects.
 */
public interface ElComparator<T> extends Comparator<T>, Serializable {

  /**
   * Compare given 2 beans.
   */
  @Override
  int compare(T o1, T o2);

  /**
   * Compare with a fixed value to a given bean.
   */
  int compareValue(Object value, T o2);

}

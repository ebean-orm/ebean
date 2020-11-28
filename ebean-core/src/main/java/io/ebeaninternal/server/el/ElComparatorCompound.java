package io.ebeaninternal.server.el;

import java.util.Comparator;

/**
 * Comparator based on multiple ordered comparators.
 * <p>
 * eg.  "name, orderDate desc, id"
 * </p>
 */
public final class ElComparatorCompound<T> implements Comparator<T>, ElComparator<T> {

  private static final long serialVersionUID = -1523163475050929750L;

  private final ElComparator<T>[] array;

  public ElComparatorCompound(ElComparator<T>[] array) {
    this.array = array;
  }

  @Override
  public int compare(T o1, T o2) {

    for (ElComparator<T> anArray : array) {
      int ret = anArray.compare(o1, o2);
      if (ret != 0) {
        return ret;
      }
    }

    return 0;
  }

  @Override
  public int compareValue(Object value, T o2) {

    for (ElComparator<T> anArray : array) {
      int ret = anArray.compareValue(value, o2);
      if (ret != 0) {
        return ret;
      }
    }

    return 0;
  }


}

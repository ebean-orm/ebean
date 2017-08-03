package io.ebeaninternal.server.el;

import java.util.Comparator;

/**
 * Comparator based on a ElGetValue.
 */
public final class ElComparatorProperty<T> implements Comparator<T>, ElComparator<T> {

  private static final long serialVersionUID = -2735738237263956073L;

  private final ElPropertyValue elGetValue;

  private final int nullOrder;

  private final int asc;

  public ElComparatorProperty(ElPropertyValue elGetValue, boolean ascending, boolean nullsHigh) {
    this.elGetValue = elGetValue;
    this.asc = ascending ? 1 : -1;
    this.nullOrder = asc * (nullsHigh ? 1 : -1);
  }

  @Override
  public int compare(T o1, T o2) {

    Object val1 = elGetValue.pathGet(o1);
    Object val2 = elGetValue.pathGet(o2);
    return compareValues(val1, val2);
  }

  @Override
  public int compareValue(Object value, T o2) {

    Object val2 = elGetValue.pathGet(o2);
    return compareValues(value, val2);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public int compareValues(Object val1, Object val2) {

    if (val1 == null) {
      return val2 == null ? 0 : nullOrder;
    }
    if (val2 == null) {
      return -1 * nullOrder;
    }
    Comparable c = (Comparable) val1;
    return asc * c.compareTo(val2);
  }


}

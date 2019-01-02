package io.ebeaninternal.server.el;

/**
 * Comparator with no operation for unsortable properties.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public class ElComparatorNoop<T> implements ElComparator<T> {

  private static final long serialVersionUID = -9060871822183687180L;

  @Override
  public int compare(T o1, T o2) {
    return 0;
  }

  @Override
  public int compareValue(Object value, T o2) {
    return 0;
  }

}

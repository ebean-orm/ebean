package io.ebean.meta;

import java.util.Comparator;

/**
 * Comparator for timed metrics sorted by name and then count.
 */
public class SortMetric {

  public static final Comparator<MetaCountMetric> COUNT_NAME = new CountName();

  public static final Comparator<MetaTimedMetric> NAME = new Name();
  public static final Comparator<MetaTimedMetric> COUNT = new Count();
  public static final Comparator<MetaTimedMetric> TOTAL = new Total();
  public static final Comparator<MetaTimedMetric> MEAN = new Mean();
  public static final Comparator<MetaTimedMetric> MAX = new Max();

  private static int stringCompare(String name, String name2) {
    if (name == null) {
      return name2 == null ? 0 : -1;
    }
    if (name2 == null) {
      return 1;
    }
    return name.compareTo(name2);
  }

  /**
   * Sort MetaCountMetric's by name.
   */
  public static class CountName implements Comparator<MetaCountMetric> {

    @Override
    public int compare(MetaCountMetric o1, MetaCountMetric o2) {
      return stringCompare(o1.getName(), o2.getName());
    }
  }

  /**
   * Sort by name.
   */
  public static class Name implements Comparator<MetaTimedMetric> {

    @Override
    public int compare(MetaTimedMetric o1, MetaTimedMetric o2) {
      int i = stringCompare(o1.getName(), o2.getName());
      return i != 0 ? i : Long.compare(o1.getCount(), o2.getCount());
    }
  }

  /**
   * Sort by count desc.
   */
  public static class Count implements Comparator<MetaTimedMetric> {

    @Override
    public int compare(MetaTimedMetric o1, MetaTimedMetric o2) {
      return Long.compare(o2.getCount(), o1.getCount());
    }
  }

  /**
   * Sort by total time desc.
   */
  public static class Total implements Comparator<MetaTimedMetric> {

    @Override
    public int compare(MetaTimedMetric o1, MetaTimedMetric o2) {
      return Long.compare(o2.getTotal(), o1.getTotal());
    }
  }

  /**
   * Sort by mean desc.
   */
  public static class Mean implements Comparator<MetaTimedMetric> {

    @Override
    public int compare(MetaTimedMetric o1, MetaTimedMetric o2) {
      return Long.compare(o2.getMean(), o1.getMean());
    }
  }

  /**
   * Sort by max desc.
   */
  public static class Max implements Comparator<MetaTimedMetric> {

    @Override
    public int compare(MetaTimedMetric o1, MetaTimedMetric o2) {
      return Long.compare(o2.getMax(), o1.getMax());
    }
  }
}

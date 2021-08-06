package io.ebean.meta;


/**
 * Timed execution statistics.
 */
public interface MetaTimedMetric extends MetaMetric {

  /**
   * Return the metric location if defined.
   */
  String location();

  /**
   * Migrate to location()
   */
  @Deprecated
  default String getLocation() {
    return location();
  }

  /**
   * Return the total count.
   */
  long count();

  /**
   * Migrate to count()
   */
  @Deprecated
  default long getCount() {
    return count();
  }

  /**
   * Return the total execution time in micros.
   */
  long total();

  /**
   * Migrate to total()
   */
  @Deprecated
  default long getTotal() {
    return total();
  }

  /**
   * Return the max execution time in micros.
   */
  long max();

  /**
   * Migrate to max()
   */
  @Deprecated
  default long getMax() {
    return max();
  }

  /**
   * Return the mean execution time in micros.
   */
  long mean();


  /**
   * Migrate to mean()
   */
  @Deprecated
  default long getMean() {
    return mean();
  }

  /**
   * Return true if this is the first metrics collection for this query.
   * <p>
   * This can be used to suppress including the SQL and location from metrics
   * content.
   * </p>
   */
  boolean initialCollection();
}

package io.ebean.meta;

import java.util.Comparator;

/**
 * Collect the metrics in raw JSON form.
 */
public interface ServerMetricsAsJson {

  /**
   * Set to false to exclude profile location.
   */
  ServerMetricsAsJson withLocation(boolean withLocation);

  /**
   * Set to false to exclude SQL for query metrics.
   */
  ServerMetricsAsJson withSql(boolean withSql);

  /**
   * Set to false to exclude SQL hash.
   */
  ServerMetricsAsJson withHash(boolean withHash);

  /**
   * Set the sort property - see SortMetric
   * @see SortMetric
   */
  ServerMetricsAsJson withSort(Comparator<MetaTimedMetric> sortBy);

  /**
   * Set the new line character to use.
   */
  ServerMetricsAsJson withNewLine(boolean withNewLine);

  /**
   * Return the metrics in raw JSON.
   */
  String json();
}

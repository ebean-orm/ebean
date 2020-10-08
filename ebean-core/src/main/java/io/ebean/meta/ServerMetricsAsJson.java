package io.ebean.meta;

import java.util.Comparator;

/**
 * Collect the metrics in raw JSON form.
 */
public interface ServerMetricsAsJson {

  /**
   * Set to false to exclude profile location and sql.
   */
  ServerMetricsAsJson withExtraAttributes(boolean withLocation);

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
   * Set to include a heading of the database name.
   * <p>
   * When this is false the metrics are written without json array start or array end.
   */
  ServerMetricsAsJson withHeader(boolean withHeader);

    /**
     * Collect and write metrics as JSON to the given buffer.
     */
  void write(Appendable buffer);

  /**
   * Return the metrics in raw JSON.
   */
  String json();
}

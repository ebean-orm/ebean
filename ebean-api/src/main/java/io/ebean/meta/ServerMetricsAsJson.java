package io.ebean.meta;

import java.util.Comparator;

/**
 * Collect the metrics in raw JSON form.
 */
public interface ServerMetricsAsJson {

  /**
   * Set to false in order to exclude profile location and sql.
   */
  ServerMetricsAsJson withExtraAttributes(boolean withLocation);

  /**
   * Set to false in order to exclude SQL hash.
   */
  ServerMetricsAsJson withHash(boolean withHash);

  /**
   * Set the sort property - see SortMetric
   *
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
   * Collect and write metrics as "v2" JSON to the given buffer.
   * <p>
   * The v2 form uses the canonical label-tag convention: each metric is written with
   * a family {@code name} (e.g. {@code ebean.query}, {@code ebean.dml}) plus a
   * {@code tags} string of sorted {@code key:value} pairs (e.g.
   * {@code "kind:orm,label:Customer.findList,type:Customer"}) rather than the flat
   * prefixed name. Timing, hash, location and sql attributes are unchanged.
   */
  void writeV2(Appendable buffer);

  /**
   * Return the metrics in raw JSON.
   */
  String json();
}

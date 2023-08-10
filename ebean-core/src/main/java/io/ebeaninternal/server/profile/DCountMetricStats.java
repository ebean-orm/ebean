package io.ebeaninternal.server.profile;

import io.ebean.metric.CountMetricStats;

/**
 * Holder for count metric values.
 */
class DCountMetricStats implements CountMetricStats {

  private final String name;
  private final long count;

  DCountMetricStats(String name, long count) {
    this.name = name;
    this.count = count;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public long count() {
    return count;
  }
}

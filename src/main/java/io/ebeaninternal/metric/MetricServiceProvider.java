package io.ebeaninternal.metric;

import io.ebeaninternal.server.profile.DMetricFactory;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Lookup MetricFactory service.
 */
class MetricServiceProvider {

  private static MetricFactory metricFactory = init();

  private static MetricFactory init() {

    Iterator<MetricFactory> loader = ServiceLoader.load(MetricFactory.class).iterator();
    if (loader.hasNext()) {
      return loader.next();
    }
    return new DMetricFactory();
  }

  /**
   * Return the MetricFactory implementation.
   */
  static MetricFactory get() {
    return metricFactory;
  }

}

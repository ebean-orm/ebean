package io.ebean.metric;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Lookup MetricFactory service.
 */
class MetricServiceProvider {

  private static final MetricFactory metricFactory = init();

  private static MetricFactory init() {
    Iterator<MetricFactory> loader = ServiceLoader.load(MetricFactory.class).iterator();
    if (loader.hasNext()) {
      return loader.next();
    }
    throw new IllegalStateException("MetricFactory not service loaded?");
  }

  /**
   * Return the MetricFactory implementation.
   */
  static MetricFactory get() {
    return metricFactory;
  }

}

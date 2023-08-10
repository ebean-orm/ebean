package io.ebean.metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Global registry of custom metrics instances created.
 */
public final class MetricRegistry {
  public interface RegistryEntry {
    void remove();
  }

  private static final List<Metric> list = Collections.synchronizedList(new ArrayList<>());

  /**
   * Register the metric instance.
   */
  public static RegistryEntry register(Metric location) {
    list.add(location);
    return () -> list.remove(location);
  }

  /**
   * Return all the registered extra metrics.
   */
  public static List<Metric> registered() {
    return list;
  }
}

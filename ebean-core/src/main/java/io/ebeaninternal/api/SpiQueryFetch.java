package io.ebeaninternal.api;

import io.ebean.FetchConfig;

import java.util.Set;

/**
 * Query select and fetch properties (that avoids parsing).
 */
public interface SpiQueryFetch {

  /**
   * Specify the select properties.
   */
  void selectProperties(Set<String> properties);

  /**
   * Specify the fetch properties for the given path.
   */
  void fetchProperties(String name, Set<String> properties, FetchConfig config);

}

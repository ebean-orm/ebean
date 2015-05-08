package com.avaje.ebean.event;

import com.avaje.ebean.config.ServerConfig;

/**
 * Objects extending this modify queries prior their execution.
 * <p>
 * This can be used to add expressions to a query - for example to enable
 * partitioning based on the user executing the query.
 * </p>
 * <p>
 * A BeanQueryAdapter is either found automatically via class path search or can
 * be added programmatically via {@link ServerConfig#add(BeanQueryAdapter)}.
 * </p>
 * <p>
 * Note that a BeanQueryAdapter should be thread safe (stateless) and if
 * registered automatically via class path search it needs to have a default
 * constructor.
 * </p>
 */
public interface BeanQueryAdapter {

  /**
   * Return true if this adapter is interested in queries for the given entity
   * type.
   */
  boolean isRegisterFor(Class<?> cls);

  /**
   * Returns an int to to control the order in which BeanQueryAdapter are
   * executed when there is multiple of them registered for a given entity type
   * (class).
   */
  int getExecutionOrder();

  /**
   * Modify the associated query prior to it being executed.
   */
  void preQuery(BeanQueryRequest<?> request);

}

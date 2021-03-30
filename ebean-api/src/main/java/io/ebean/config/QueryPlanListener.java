package io.ebean.config;

/**
 * EXPERIMENTAL: Listener for captured query plans.
 */
@FunctionalInterface
public interface QueryPlanListener {

  /**
   * Process the captured query plans.
   */
  void process(QueryPlanCapture capture);
}

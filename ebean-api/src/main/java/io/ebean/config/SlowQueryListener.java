package io.ebean.config;

/**
 * Listener for slow query events.
 */
@FunctionalInterface
public interface SlowQueryListener {

  /**
   * Process a slow query event.
   */
  void process(SlowQueryEvent event);
}

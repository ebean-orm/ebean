package io.ebeaninternal.server.core;

import java.time.Clock;

/**
 * Wraps the Clock such that we can change the Clock for testing purposes.
 */
public class ClockService {

  private Clock clock;

  public ClockService(Clock clock) {
    this.clock = clock;
  }

  /**
   * Change the clock for testing purposes.
   */
  public void setClock(Clock clock) {
    this.clock = clock;
  }

  /**
   * Return the Clock current timestamp in millis.
   */
  public long nowMillis() {
    return clock.millis();
  }
}

package io.ebean;

import java.time.Clock;

/**
 * The extended API for Database.
 */
public interface ExtendedServer {

  /**
   * Deprecated but no yet determined suitable replacement (to support testing only change of clock).
   * <p>
   * Set the Clock to use for <code>@WhenCreated</code> and <code>@WhenModified</code>.
   * <p>
   * Note that we only expect to change the Clock for testing purposes.
   * </p>
   */
  @Deprecated
  void setClock(Clock clock);

}

package io.ebean;

import java.time.Clock;

/**
 * The extended API for Database.
 * <p>
 * Deprecated in favour of using {@link Query#usingTransaction(Transaction)} instead.
 * <p>
 * This provides the finder methods that take an explicit transaction rather than obtaining
 * the transaction from the usual mechanism (which is ThreadLocal based).
 * <p>
 * Note that in all cases the transaction supplied can be null and in this case the Database
 * will use the normal mechanism to obtain the transaction to use.
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

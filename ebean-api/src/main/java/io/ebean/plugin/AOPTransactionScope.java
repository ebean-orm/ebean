package io.ebean.plugin;

import io.ebean.DB;
import io.ebean.TxScope;

/**
 * Helper object to make AOP generated code simpler.
 */
public final class AOPTransactionScope {
  private static boolean enabled = true;

  /**
   * Entering an enhanced transactional method.
   */
  public static void enter(TxScope txScope) {
    if (enabled) {
      server().scopedTransactionEnter(txScope);
    }
  }

  /**
   * Exiting an enhanced transactional method.
   */
  public static void exit(Object returnOrThrowable, int opCode) {
    if (enabled) {
      server().scopedTransactionExit(returnOrThrowable, opCode);
    }
  }

  private static SpiServer server() {
    return (SpiServer) DB.getDefault();
  }

  /**
   * Defines if the @Transactional does what is supposed to do or is disabled
   * (useful only unit testing)
   *
   * @param enabled if set to false, @Transactional will not create a transaction
   */
  public static void setEnabled(boolean enabled) {
    AOPTransactionScope.enabled = enabled;
  }
}

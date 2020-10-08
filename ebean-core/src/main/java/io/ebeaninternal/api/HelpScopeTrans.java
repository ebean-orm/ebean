package io.ebeaninternal.api;

import io.ebean.DB;
import io.ebean.TxScope;

/**
 * Helper object to make AOP generated code simpler.
 */
public class HelpScopeTrans {
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

  private static SpiEbeanServer server() {
    return (SpiEbeanServer) DB.getDefault();
  }

  /**
   * Defines if the @Transactional does what is supposed to do or is disabled
   * (useful only unit testing)
   *
   * @param enabled if set to false, @Transactional will not create a transaction
   */
  public static void setEnabled(boolean enabled) {
    HelpScopeTrans.enabled = enabled;
  }
}

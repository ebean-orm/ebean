package io.ebeaninternal.api;

import io.ebean.TxScope;
import io.ebean.plugin.AOPTransactionScope;

/**
 * Helper object to make AOP generated code simpler.
 */
public final class HelpScopeTrans {

  /**
   * Entering an enhanced transactional method.
   */
  public static void enter(TxScope txScope) {
    AOPTransactionScope.enter(txScope);
  }

  /**
   * Exiting an enhanced transactional method.
   */
  public static void exit(Object returnOrThrowable, int opCode) {
    AOPTransactionScope.exit(returnOrThrowable, opCode);
  }

  /**
   * Defines if the @Transactional does what is supposed to do or is disabled
   * (useful only unit testing)
   *
   * @param enabled if set to false, @Transactional will not create a transaction
   */
  public static void setEnabled(boolean enabled) {
    AOPTransactionScope.setEnabled(enabled);
  }
}

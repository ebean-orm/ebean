package io.ebeaninternal.api;

import io.ebean.Ebean;
import io.ebean.TxScope;

/**
 * Helper object to make AOP generated code simpler.
 */
public class HelpScopeTrans {

  /**
   * Entering an enhanced transactional method.
   */
  public static void enter(TxScope txScope) {
    server().scopedTransactionEnter(txScope);
  }

  /**
   * Exiting an enhanced transactional method.
   */
  public static void exit(Object returnOrThrowable, int opCode) {
    server().scopedTransactionExit(returnOrThrowable, opCode);
  }

  private static SpiEbeanServer server() {
    return (SpiEbeanServer) Ebean.getDefaultServer();
  }
}

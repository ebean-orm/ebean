package io.ebean;

/**
 * Deprecated - please migrate to using just Runnable.
 *
 *
 * Execute a TxRunnable in a Transaction scope.
 * <p>
 * Use this with the {@link Ebean#execute(Runnable)} method.
 * </p>
 * <p>
 * <pre>{@code
 *
 * // this run method runs in a transaction scope
 * // which by default is TxScope.REQUIRED
 *
 * Ebean.execute(new TxRunnable() {
 *   public void run() {
 *     User u1 = Ebean.find(User.class, 1);
 *     User u2 = Ebean.find(User.class, 2);
 *
 *     u1.setName("u1 mod");
 *     u2.setName("u2 mod");
 *
 *     Ebean.save(u1);
 *     Ebean.save(u2);
 *   }
 * });
 *
 * }</pre>
 *
 * @see TxCallable
 */
@Deprecated
public interface TxRunnable extends Runnable {

  /**
   * Run the method in a transaction sope.
   */
  @Override
  void run();
}

package com.avaje.ebean;

/**
 * Execute a TxRunnable in a Transaction scope.
 * <p>
 * Use this with the {@link Ebean#execute(TxRunnable)} method.
 * </p>
 * <p>
 * See also {@link TxCallable}.
 * </p>
 * 
 * <pre class="code">
 * 
 * // this run method runs in a transaction scope
 * // which by default is TxScope.REQUIRED
 * 
 * Ebean.execute(new TxRunnable() {
 *   public void run() {
 *     User u1 = Ebean.find(User.class, 1);
 *     User u2 = Ebean.find(User.class, 2);
 * 
 *     u1.setName(&quot;u1 mod&quot;);
 *     u2.setName(&quot;u2 mod&quot;);
 * 
 *     Ebean.save(u1);
 *     Ebean.save(u2);
 *   }
 * });
 * </pre>
 * 
 * @see TxCallable
 */
public interface TxRunnable {

  /**
   * Run the method in a transaction sope.
   */
  void run();
}

package com.avaje.ebean;

/**
 * Execute a TxCallable in a Transaction scope.
 * <p>
 * Use this with the {@link Ebean#execute(TxCallable)} method.
 * </p>
 * <p>
 * Note that this is basically the same as TxRunnable except that it returns an
 * Object (and you specify the return type via generics).
 * </p>
 * <p>
 * See also {@link TxRunnable}.
 * </p>
 * 
 * <pre class="code">
 * Ebean.execute(new TxCallable&lt;String&gt;() {
 *   public String call() {
 *     User u1 = Ebean.find(User.class, 1);
 *     User u2 = Ebean.find(User.class, 2);
 * 
 *     u1.setName(&quot;u1 mod&quot;);
 *     u2.setName(&quot;u2 mod&quot;);
 * 
 *     Ebean.save(u1);
 *     Ebean.save(u2);
 * 
 *     return u1.getEmail();
 *   }
 * });
 * </pre>
 * 
 * @see TxRunnable
 */
public interface TxCallable<T> {

  /**
   * Execute the method within a transaction scope returning the result.
   * <p>
   * If you do not want to return a result you should look to use TxRunnable
   * instead.
   * </p>
   */
  T call();
}

package io.ebean;

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
 * <p>
 * <pre>{@code
 *
 * Ebean.execute(new TxCallable<String>() {
 *   public String call() {
 *     User u1 = Ebean.find(User.class, 1);
 *     User u2 = Ebean.find(User.class, 2);
 *
 *     u1.setName("u1 mod");
 *     u2.setName("u2 mod");
 *
 *     Ebean.save(u1);
 *     Ebean.save(u2);
 *
 *     return u1.getEmail();
 *   }
 * });
 *
 * }</pre>
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

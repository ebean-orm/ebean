package io.ebean;

import java.util.concurrent.Callable;

/**
 * Deprecated - please migrate to just using Callable instead with executeCall().
 *
 * Execute a TxCallable in a Transaction scope.
 * <p>
 * Use this with the {@link Ebean#executeCall(Callable)} method.
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
 */
@Deprecated
public interface TxCallable<T> extends Callable<T> {

  /**
   * Execute the method within a transaction scope returning the result.
   * <p>
   * If you do not want to return a result you should look to use TxRunnable
   * instead.
   * </p>
   */
  T call();
}

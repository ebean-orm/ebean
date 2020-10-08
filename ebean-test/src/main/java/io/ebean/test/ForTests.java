package io.ebean.test;

import io.ebean.DB;
import io.ebean.Transaction;
import io.ebeaninternal.api.HelpScopeTrans;

/**
 * Helper methods for testing.
 */
public class ForTests {

  /**
   * Enable or disable <code>@Transactional</code> methods.
   * <p>
   * This is intended for testing purposes such that tests
   * on code with {@code @Transactional} methods don't actually
   * start or complete transactions.
   * </p>
   *
   * @param enable Set false to disable {@code @Transactional} methods
   */
  public static void enableTransactional(boolean enable) {
    HelpScopeTrans.setEnabled(enable);
  }

  /**
   * Run the closure with <code>@Transactional</code> methods
   * effectively disabled (they won't create/commit transactions).
   */
  public static void noTransactional(Runnable run) {
    try {
      enableTransactional(false);
      run.run();
    } finally {
      enableTransactional(true);
    }
  }

  /**
   * All transactions started in the closure are effectively rolled back.
   * <p>
   * This creates a wrapping transaction that uses {@link Transaction#setNestedUseSavepoint()}.
   * All nested transactions are created as savepoints. On completion the wrapping
   * transaction is rolled back.
   * </p>
   *
   * @param run Closure that runs such that all the transactions are rolled back.
   */
  public static void rollbackAll(Runnable run) {

    try (Transaction transaction = DB.beginTransaction()) {
      transaction.setNestedUseSavepoint();
      run.run();

      transaction.rollback();
    }
  }

  /**
   * Create and return a RollbackAll which should be closed at the end of the test(s).
   * <p>
   * In tests for <code>@Before</code> we create the rollbackAll and on
   * <code>@After</code> we <code>close()</code> it effectively rolling
   * back all changes made during test execution.
   * </p>
   *
   * <pre>{@code
   *
   *   private ForTests.RollbackAll rollbackAll;
   *
   *   @Before
   *   public void before() {
   *     rollbackAll = ForTests.createRollbackAll();
   *   }
   *
   *   @After
   *   public void after() {
   *     rollbackAll.close();
   *   }
   *
   *   ... tests execute and everything is rolled back
   *
   *
   * }</pre>
   */
  public static RollbackAll createRollbackAll() {

    final Transaction transaction = DB.beginTransaction();
    transaction.setNestedUseSavepoint();
    return new RollbackAll(transaction);
  }

  /**
   * A wrapping transaction used in test code to rollback all changes.
   * <p>
   * We must ensure that <code>close()</code> is called.
   * </p>
   */
  public static class RollbackAll implements AutoCloseable {

    private final Transaction transaction;

    private RollbackAll(Transaction transaction) {
      this.transaction = transaction;
    }

    /**
     * Rollback the wrapping transaction.
     */
    @Override
    public void close() {
      transaction.rollback();
    }
  }
}

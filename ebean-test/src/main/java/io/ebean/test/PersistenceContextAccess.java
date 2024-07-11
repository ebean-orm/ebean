package io.ebean.test;

import io.ebean.Transaction;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.api.SpiTransaction;

/**
 * Provides tests access to the persistence context.
 * <p>
 * Expected to be used with tests that use Spring test {@code @Rollback} {@code @Transactional}.
 * These tests have an outer transaction that will rollback. The issue is that test setup code
 * is now running in the SAME TRANSACTION as the code under test (main code we are testing) and
 * that the Ebean Persistence Context is transaction scoped - so the test setup code can load
 * entities into the ebean persistence context during the test setup phase - we SHOULD clear the
 * persistence context AFTER the setup phase of the test and BEFORE we run the code under test.
 *
 * <pre>{@code
 *
 * @Test
 * @Transactional   // spring transactional
 * @Rollback        // spring test rollback
 * void myTestWithSpringRollback() {
 *
 *   // Test Setup: this MIGHT load entities into the persistence context
 *   performTestSetup();
 *
 *   // clear out the persistence context
 *   PersistenceContextAccess.clear();
 *
 *   // Act
 *   performActionsWeAreTestingHere();
 *
 *   // Assert
 *   assertThat(...)
 *
 * }
 *
 * }</pre>
 */
public class PersistenceContextAccess {

  /**
   * Clear the persistence context of the current transaction.
   * <p>
   * This is expected to be called after test setup phase and before
   * the test executes the code we are looking to test - so
   * AFTER "setup" and BEFORE "act".
   */
  public static void clear() {
    Transaction current = Transaction.current();
    if (current != null) {
      SpiTransaction spiTransaction = (SpiTransaction) current;
      PersistenceContext pc = spiTransaction.persistenceContext();
      if (pc != null) {
        pc.clear();
      }
    }
  }
}

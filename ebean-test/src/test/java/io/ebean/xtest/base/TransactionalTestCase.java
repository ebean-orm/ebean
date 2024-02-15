package io.ebean.xtest.base;

import io.ebean.Transaction;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.tests.model.basic.ResetBasicData;

/**
 * Transactional test case. Every test is covered by a transaction, which is roll backed.
 * So no changes will persist to database.
 *
 * Use this test case if you modify data in your test case, so that the test does not interfere
 * other tests.
 *
 * @author Roland Praml, FOCONIS AG
 *
 */
public abstract class TransactionalTestCase extends BaseTestCase {

  private Transaction transaction;

  @BeforeEach
  public void startTransaction() {
    ResetBasicData.reset();
    transaction = DB.beginTransaction();
  }

  @AfterEach
  public void endTransaction() {
    transaction.rollback();
    transaction.end();
  }
}

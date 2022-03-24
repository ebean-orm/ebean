package org.tests.transaction;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.annotation.Transactional;
import io.ebean.annotation.TxType;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestTransactionalNever extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(TestTransactionalNever.class);

  private Transaction outerTxn;

  @Test
  public void testWithNever() {

    new SomeTransactionalWithNever().doStuff();
  }

  @Test
  public void testBarfOnExisting() {
    assertThrows(PersistenceException.class, this::doWithTransaction);
  }

  class SomeTransactionalWithNever {

    @Transactional(type = TxType.NEVER)
    void doStuff() {
      outerTxn = DB.currentTransaction();
      log.info("currentTransaction ...{}", outerTxn);
    }
  }

  @Transactional
  private void doWithTransaction() {

    // always barf
    new SomeTransactionalWithNever().doStuff();
  }

}

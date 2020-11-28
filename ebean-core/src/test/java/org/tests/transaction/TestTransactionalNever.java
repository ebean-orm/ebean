package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Transaction;
import io.ebean.annotation.Transactional;
import io.ebean.annotation.TxType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PersistenceException;

public class TestTransactionalNever extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(TestTransactionalNever.class);

  private Transaction outerTxn;

  @Test
  public void testWithNever() {

    new SomeTransactionalWithNever().doStuff();
  }

  @Test(expected = PersistenceException.class)
  public void testBarfOnExisting() {
    doWithTransaction();
  }

  class SomeTransactionalWithNever {

    @Transactional(type = TxType.NEVER)
    void doStuff() {
      outerTxn = Ebean.currentTransaction();
      log.info("currentTransaction ...{}", outerTxn);
    }
  }

  @Transactional
  private void doWithTransaction() {

    // always barf
    new SomeTransactionalWithNever().doStuff();
  }

}

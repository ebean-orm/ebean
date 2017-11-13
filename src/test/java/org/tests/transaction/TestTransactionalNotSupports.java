package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Transaction;
import io.ebean.annotation.Transactional;
import io.ebean.annotation.TxType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class TestTransactionalNotSupports extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(TestTransactionalNotSupports.class);

  private Transaction outerTxn;

  private Transaction currentTxn;

  @Test
  public void noCurrentTransaction() {

    outerTxn = null;
    new SomeTransactionalWithNotSupported().doStuff();
    assertNull(outerTxn);
  }


  @Test
  public void withOuterTransaction_expect_currentTransaction_null_and_originalTxnRestored() {

    outerTxn = null;
    Ebean.beginTransaction();
    try {
      currentTxn = Ebean.currentTransaction();
      assertNotNull(currentTxn);
      new SomeTransactionalWithNotSupported().doStuff();

      // there was no current transaction inside the NOT_SUPPORTED
      assertNull(outerTxn);

      // the original transaction was restored
      Transaction restored = Ebean.currentTransaction();
      assertSame(currentTxn, restored);
    } finally {
      Ebean.endTransaction();
    }
  }

  class SomeTransactionalWithNotSupported {

    @Transactional(type = TxType.NOT_SUPPORTED)
    void doStuff() {
      outerTxn = Ebean.currentTransaction();
      log.info("outer ...{}", outerTxn);
    }
  }


}

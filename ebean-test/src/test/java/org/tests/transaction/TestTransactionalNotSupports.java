package org.tests.transaction;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.annotation.Transactional;
import io.ebean.annotation.TxType;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

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
    DB.beginTransaction();
    try {
      currentTxn = DB.currentTransaction();
      assertNotNull(currentTxn);
      new SomeTransactionalWithNotSupported().doStuff();

      // there was no current transaction inside the NOT_SUPPORTED
      assertNull(outerTxn);

      // the original transaction was restored
      Transaction restored = DB.currentTransaction();
      assertSame(currentTxn, restored);
    } finally {
      DB.endTransaction();
    }
  }

  class SomeTransactionalWithNotSupported {

    @Transactional(type = TxType.NOT_SUPPORTED)
    void doStuff() {
      outerTxn = DB.currentTransaction();
      log.info("outer ...{}", outerTxn);
    }
  }


}

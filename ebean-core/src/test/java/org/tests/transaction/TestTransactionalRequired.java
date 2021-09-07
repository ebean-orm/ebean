package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.annotation.Transactional;
import io.ebean.annotation.TxType;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class TestTransactionalRequired extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(TestTransactionalRequired.class);

  private Transaction outerTxn;
  private Transaction innerTxn;
  private Transaction currentTxn;

  @Test
  public void noCurrentTransaction() {

    outerTxn = null;

    assertNull(DB.currentTransaction());

    new OuterTransactionalWithRequired().doOuter();

    assertNull(DB.currentTransaction());

    assertNotNull(outerTxn);
    assertNotNull(innerTxn);
    assertSame(outerTxn, innerTxn);
  }


  @Test
  public void withOuterBegin() {

    outerTxn = null;
    DB.beginTransaction();
    try {
      currentTxn = DB.currentTransaction();
      assertNotNull(currentTxn);
      new OuterTransactionalWithRequired().doOuter();

      // the original transaction was restored
      Transaction restored = DB.currentTransaction();
      assertSame(currentTxn, restored);
      assertSame(currentTxn, innerTxn);
      assertSame(currentTxn, outerTxn);
    } finally {
      DB.endTransaction();
    }

    assertNull(DB.currentTransaction());
  }

  class OuterTransactionalWithRequired {

    @Transactional(type = TxType.REQUIRED)
    void doOuter() {
      outerTxn = DB.currentTransaction();
      log.info("outer ...{}", outerTxn);

      new InTransactionalWithRequired().doInner();

      Transaction current = DB.currentTransaction();
      assertSame(outerTxn, current);
    }

  }

  class InTransactionalWithRequired {

    @Transactional(type = TxType.REQUIRED)
    void doInner() {
      innerTxn = DB.currentTransaction();
      log.info("inner ...{}", innerTxn);
    }
  }


}

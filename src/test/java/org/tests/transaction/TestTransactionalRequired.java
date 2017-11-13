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

public class TestTransactionalRequired extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(TestTransactionalRequired.class);

  private Transaction outerTxn;
  private Transaction innerTxn;
  private Transaction currentTxn;

  @Test
  public void noCurrentTransaction() {

    outerTxn = null;

    assertNull(Ebean.currentTransaction());

    new OuterTransactionalWithRequired().doOuter();

    assertNull(Ebean.currentTransaction());

    assertNotNull(outerTxn);
    assertNotNull(innerTxn);
    assertSame(outerTxn, innerTxn);
  }


  @Test
  public void withOuterBegin() {

    outerTxn = null;
    Ebean.beginTransaction();
    try {
      currentTxn = Ebean.currentTransaction();
      assertNotNull(currentTxn);
      new OuterTransactionalWithRequired().doOuter();

      // the original transaction was restored
      Transaction restored = Ebean.currentTransaction();
      assertSame(currentTxn, restored);
      assertSame(currentTxn, innerTxn);
      assertSame(currentTxn, outerTxn);
    } finally {
      Ebean.endTransaction();
    }

    assertNull(Ebean.currentTransaction());
  }

  class OuterTransactionalWithRequired {

    @Transactional(type = TxType.REQUIRED)
    void doOuter() {
      outerTxn = Ebean.currentTransaction();
      log.info("outer ...{}", outerTxn);

      new InTransactionalWithRequired().doInner();

      Transaction current = Ebean.currentTransaction();
      assertSame(outerTxn, current);
    }

  }

  class InTransactionalWithRequired {

    @Transactional(type = TxType.REQUIRED)
    void doInner() {
      innerTxn = Ebean.currentTransaction();
      log.info("inner ...{}", innerTxn);
    }
  }


}

package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Transaction;
import io.ebean.annotation.Transactional;
import io.ebean.annotation.TxType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class TestTransactionalRequiresNew extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(TestTransactionalRequiresNew.class);

  private Connection outerConn;
  private Transaction outerTxn;

  @Test
  public void basic() {

    outerTxn = null;
    assertNull(Ebean.currentTransaction());

    new OuterTransactionalWithRequired().doOuter();

    assertNull(Ebean.currentTransaction());
    assertNotNull(outerTxn);
  }


  class OuterTransactionalWithRequired {

    @Transactional
    void doOuter() {
      outerTxn = Ebean.currentTransaction();
      log.info("outer before ...{}", outerTxn);
      outerConn = outerTxn.getConnection();

      new InTransactionalWithRequiresNew().doInner();

      // restore the outerTxn
      Transaction current = Ebean.currentTransaction();
      log.info("outer after ...{}", current);
      assertSame(outerConn, current.getConnection());
    }

  }

  class InTransactionalWithRequiresNew {

    @Transactional(type = TxType.REQUIRES_NEW)
    void doInner() {
      Transaction innerTxn = Ebean.currentTransaction();
      log.info("inner ...{} {}", innerTxn);

      Connection connection = innerTxn.getConnection();
      assertNotSame(connection, outerConn);
    }
  }


}

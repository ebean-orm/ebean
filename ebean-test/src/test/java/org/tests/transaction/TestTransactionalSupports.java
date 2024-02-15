package org.tests.transaction;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.annotation.Transactional;
import io.ebean.annotation.TxType;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.basic.EBasic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestTransactionalSupports extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(TestTransactionalSupports.class);

  private Transaction outerTxn;

  @Test
  public void noCurrentTransaction() {

    outerTxn = null;
    new SomeTransactionalWithSupports().doStuff();
    assertNull(outerTxn);
  }


  @Test
  public void withOuterTransaction_expect_currentTransactionAvailable() {

    outerTxn = null;
    try (Transaction txn = DB.beginTransaction()) {
      new SomeTransactionalWithSupports().doStuff();
      assertNotNull(outerTxn);
    }
  }

  @Test
  public void supports_withInternalSave() {

    int got = new SomeTransactionalWithSupportsAndSave().doStuff();
    assertThat(got).isEqualTo(42);
  }

  class SomeTransactionalWithSupports {

    @Transactional(type = TxType.SUPPORTS)
    void doStuff() {
      outerTxn = DB.currentTransaction();
      log.info("outer ...{}", outerTxn);
    }
  }


  class SomeTransactionalWithSupportsAndSave {

    @Transactional(type = TxType.SUPPORTS)
    int doStuff() {

      EBasic basic = new EBasic();
      basic.setName("checkSupports");

      // effective creates a transaction replacing the 'No active transaction' placeholder
      DB.save(basic);

      return 42;
    }
  }
}

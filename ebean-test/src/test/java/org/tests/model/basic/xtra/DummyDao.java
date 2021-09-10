package org.tests.model.basic.xtra;

import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.annotation.PersistBatch;
import io.ebean.annotation.Transactional;
import io.ebean.annotation.TxType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class DummyDao {

  private final Logger logger = LoggerFactory.getLogger(DummyDao.class);

  @Transactional(type = TxType.REQUIRES_NEW)
  public void doSomething() {

    logger.info("  --- in DummyDao.doSomething() with TxType.REQUIRES_NEW");
    Transaction txn = DB.currentTransaction();
    if (txn == null) {
      logger.error("  NO TRANSACTION ??");
    } else {
      logger.info("  --- txn - " + txn);
    }

  }

  @Transactional(batch = PersistBatch.ALL, batchOnCascade = PersistBatch.ALL, batchSize = 99)
  private void doWithBatchOptionsSet() {

    Transaction txn = DB.currentTransaction();
    assertTrue(txn.isBatchMode());
    assertTrue(txn.isBatchOnCascade());
    assertEquals(99, txn.getBatchSize());
  }


  @Transactional(batch = PersistBatch.ALL, batchOnCascade = PersistBatch.NONE, batchSize = 77)
  public void doOuterWithBatchOptionsSet() {

    Transaction txn = DB.currentTransaction();

    assertTrue(txn.isBatchMode());
    assertFalse(txn.isBatchOnCascade());
    assertEquals(77, txn.getBatchSize());

    doWithBatchOptionsSet();

    // batch options set back
    assertTrue(txn.isBatchMode());
    assertFalse(txn.isBatchOnCascade());
    assertEquals(77, txn.getBatchSize());

  }

}

package org.tests.unitinternal;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.basic.EBasicVer;
import org.tests.model.basic.xtra.DummyDao;
import org.tests.model.basic.xtra.OptimisticLockExceptionThrowingDao;

import javax.persistence.OptimisticLockException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

public class TestTxTypeOnTransactional extends BaseTestCase {

  Logger logger = LoggerFactory.getLogger(TestTxTypeOnTransactional.class);

  @Test
  public void testBatchOptionsAreSet() {

    logger.info("-- test pre doOuterWithBatchOptionsSet");
    DummyDao dao = new DummyDao();
    dao.doOuterWithBatchOptionsSet();
    logger.info("-- test post doOuterWithBatchOptionsSet");
  }

  @Test
  public void test() {

    logger.info("-- test pre dao.doSomething");
    DummyDao dao = new DummyDao();
    dao.doSomething();
    logger.info("-- test post dao.doSomething");
  }

  public void testOptimisticException() {

    logger.info("-- testOptimisticException");
    EBasicVer v = new EBasicVer("occ");
    v.setDescription("blah");
    DB.save(v);

    logger.info("-- OptimisticLockExceptionThrowingDao");
    OptimisticLockExceptionThrowingDao dao = new OptimisticLockExceptionThrowingDao();
    try {
      dao.doSomething(v);
      // never get here
      fail();
    } catch (OptimisticLockException e) {
      Transaction inMethodTransaction = dao.getInMethodTransaction();
      assertFalse(inMethodTransaction.isActive());
    }
  }

}

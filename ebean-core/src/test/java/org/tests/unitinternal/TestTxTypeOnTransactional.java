package org.tests.unitinternal;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Transaction;
import org.tests.model.basic.EBasicVer;
import org.tests.model.basic.xtra.DummyDao;
import org.tests.model.basic.xtra.OptimisticLockExceptionThrowingDao;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.OptimisticLockException;

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
    Ebean.save(v);

    logger.info("-- OptimisticLockExceptionThrowingDao");
    OptimisticLockExceptionThrowingDao dao = new OptimisticLockExceptionThrowingDao();
    try {
      dao.doSomething(v);
      // never get here
      Assert.assertTrue(false);
    } catch (OptimisticLockException e) {
      Transaction inMethodTransaction = dao.getInMethodTransaction();
      boolean active = inMethodTransaction.isActive();
      Assert.assertFalse(active);
    }
  }

}

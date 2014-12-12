package com.avaje.tests.unitinternal;

import javax.persistence.OptimisticLockException;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.avaje.tests.model.basic.EBasicVer;
import com.avaje.tests.model.basic.xtra.DummyDao;
import com.avaje.tests.model.basic.xtra.OptimisticLockExceptionThrowingDao;

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
    EBasicVer v = new EBasicVer();
    v.setName("occ");
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

package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.annotation.Transactional;
import org.junit.Test;
import org.tests.model.m2m.MnyB;

public class TestBatchModelFlush extends BaseTestCase {

  @SuppressWarnings("static-access")
  @Transactional(batchSize = 50)
  @Test
  public void insert() {

    new MnyB("TestBatchModelFlush_0").save();
    new MnyB("TestBatchModelFlush_1").save();

    MnyB bean = new MnyB("TestBatchModelFlush_2");
    bean.save();
    bean.db().currentTransaction().flush();

    MnyB bean2 = new MnyB("TestBatchModelFlush_3");
    bean2.save();
    bean2.db().flush();

    new MnyB("TestBatchModelFlush_4").save();
    Ebean.flush();

    // the rest is flushed on commit
    new MnyB("TestBatchModelFlush_5").save();
  }
}

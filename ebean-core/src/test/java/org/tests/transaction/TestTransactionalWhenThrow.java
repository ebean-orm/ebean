package org.tests.transaction;

import io.ebean.DB;
import io.ebean.annotation.Transactional;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.basic.EBasicVer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class TestTransactionalWhenThrow {

  private static final Logger log = LoggerFactory.getLogger(TestTransactionalWhenThrow.class);

  @Test
  public void test() {
    EBasicVer bean = new EBasicVer("doTransactional");

    assertThatCode(() -> doTransactional(bean))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("InnerFailWithSomething");

    final EBasicVer found = DB.find(EBasicVer.class, bean.getId());
    assertThat(found).isNull();
  }

  @Transactional
  private void doTransactional(EBasicVer bean) {
    DB.save(bean);
    failWithSomething();
  }

  private void failWithSomething() {
    EBasicVer bean = new EBasicVer("failWithSomething");
    DB.save(bean);
    throw new IllegalStateException("InnerFailWithSomething");
  }

  @Test
  public void testDepth3FailWithCatch() {

    EBasicVer outer = new EBasicVer("testDepth3FailWithCatch");

    assertThatCode(() -> doOuterTransactional(outer))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Transaction is Inactive");

    final EBasicVer found = DB.find(EBasicVer.class, outer.getId());
    assertThat(found).isNull();
  }

  @Transactional
  void doOuterTransactional(EBasicVer outer) {
    DB.save(outer);
    doInnerTransactionWithCatch();
    // exiting this throws IllegalStateException("Transaction is Inactive")
    // as doInnerTransactionWithCatch() has caught and not thrown
  }

  @Transactional
  private void doInnerTransactionWithCatch() {
    try {
      EBasicVer bean = new EBasicVer("doInnerTransactionWithCatch");
      DB.save(bean);
      doInnerInner();
    } catch (Throwable e) {
      // at this point doInnerInner() has rolled back transaction and marked as inactive
      log.error("Catch internal error (and not thrown) so caller is unaware", e);
      // catching and not throwing here means the calling @Transactional method
      // doOuterTransactional() isn't aware that the transaction has been
      // rolled back already. It will then exit with:
      // IllegalStateException("Transaction is Inactive");
    }
  }

  @Transactional
  void doInnerInner() {
    EBasicVer bean = new EBasicVer("doInnerInner");
    DB.save(bean);
    failWithSomething();
    // exiting this method rolls back transaction
  }

}

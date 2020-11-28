package org.test;

import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.annotation.Transactional;
import io.ebean.test.ForTests;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ForTestsTest {

  @Test
  public void noTransactional_expect_noTransactionEnterExitCalled() {

    ForTests.noTransactional(this::checkForTransactional);
  }

  @Test
  public void enableTransactional() {

    ForTests.enableTransactional(false);

    checkForTransactional();

    ForTests.enableTransactional(true);
  }

  @Transactional
  private void checkForTransactional() {

    final Transaction transaction = DB.currentTransaction();
    assertThat(transaction).isNull();
  }

  @Test
  public void rollbackTransactions() {

    DB.find(BSimpleWithGen.class).delete();

    ForTests.rollbackAll(this::doInsert);

    final int count = DB.find(BSimpleWithGen.class).findCount();
    assertThat(count).isEqualTo(0);
  }

  @Transactional
  private void doInsert() {

    BSimpleWithGen bean = new BSimpleWithGen("doInsert");
    DB.save(bean);
  }

}

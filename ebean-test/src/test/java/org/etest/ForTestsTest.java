package org.etest;

import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.annotation.Transactional;
import io.ebean.test.ForTests;
import org.junit.jupiter.api.Test;

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

    DB.find(BSimpleFor.class).delete();

    ForTests.rollbackAll(this::doInsert);

    final int count = DB.find(BSimpleFor.class).findCount();
    assertThat(count).isEqualTo(0);
  }

  @Transactional
  private void doInsert() {

    BSimpleFor bean = new BSimpleFor("doInsert");
    DB.save(bean);
  }

}

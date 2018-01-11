package org.tests.transaction;

import io.ebean.Ebean;
import io.ebean.Transaction;
import io.ebean.annotation.Transactional;
import org.junit.Test;
import org.tests.model.basic.EBasic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestTransactionRollbackOnly {

  private EBasic one;

  private EBasic two;

  @Test
  public void transaction_setRollbackOnly() {

    doVia_currentTransaction();

    assertThat(one.getId()).isNotNull();
    assertThat(Ebean.find(EBasic.class, one.getId())).isNull();
  }

  @Transactional
  protected void doVia_currentTransaction() {

    one = new EBasic("WillNotSave");
    Ebean.save(one);

    Transaction transaction = Transaction.current();
    assertFalse(transaction.isRollbackOnly());

    transaction.setRollbackOnly();
    assertTrue(transaction.isRollbackOnly());
  }

  @Test
  public void test_Ebean_setRollbackOnly() {

    do_Ebean_setRollbackOnly();

    assertThat(two.getId()).isNotNull();
    assertThat(Ebean.find(EBasic.class, two.getId())).isNull();
  }

  @Transactional
  protected void do_Ebean_setRollbackOnly() {

    two = new EBasic("WillNotSave");
    Ebean.save(two);

    Ebean.setRollbackOnly();
  }
}

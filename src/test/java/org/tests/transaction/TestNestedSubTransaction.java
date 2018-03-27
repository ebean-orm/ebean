package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.PersistenceContextScope;
import io.ebean.Transaction;
import org.junit.Test;
import org.tests.model.basic.EBasic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class TestNestedSubTransaction extends BaseTestCase {

  @Test
  public void ebeanServer_commitTransaction_expect_sameAsTransactionCommit() {

    EbeanServer server = server();

    EBasic bean = new EBasic("x1");

    try (Transaction txn0 = server.beginTransaction()) {
      txn0.setRollbackOnly();

      server.save(bean);

      try (Transaction txn1 = server.beginTransaction()) {
        bean.setName("x2");
        server.save(bean);
        //txn1.commit();
        server.commitTransaction();
      }

      EBasic fresh = server.find(EBasic.class, bean.getId());
      assertNotNull(fresh); // FAILS
      assertThat(fresh.getName()).isEqualTo("x2");

      try (Transaction txn2 = server.beginTransaction()) {
        bean.setName("barney");
        Ebean.save(bean);
        //txn2.commit();
        server.commitTransaction();
      }

      fresh = server.find(EBasic.class)
        .setId(bean.getId())
        .setPersistenceContextScope(PersistenceContextScope.QUERY)
        .findOne();

      assertNotNull(fresh);
      assertThat(fresh.getName()).isEqualTo("barney");

    } finally {
      cleanup(bean);
    }
  }

  private void cleanup(EBasic bean) {
    Ebean.delete(EBasic.class, bean.getId());
  }
}

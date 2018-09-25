package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.PersistenceContextScope;
import io.ebean.Transaction;
import io.ebean.TxScope;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;
import org.junit.Test;
import org.tests.model.basic.EBasic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestNestedSubTransaction extends BaseTestCase {

  /**
   * MySql only supports named savepoints - review.
   */
  @IgnorePlatform({Platform.MYSQL, Platform.SQLSERVER})
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


  @IgnorePlatform({Platform.SQLSERVER, Platform.MYSQL})
  @Test
  public void nestedUseSavepoint_doubleNested_rollbackCommit() {

    EbeanServer server = server();

    EBasic bean = new EBasic("start2");

    try (Transaction txn0 = server.beginTransaction()) {
      txn0.setRollbackOnly();
      txn0.setNestedUseSavepoint();

      server.save(bean);

      try (Transaction txn1 = server.beginTransaction()) {
        bean.setName("updateNested");
        server.save(bean);

        try (Transaction txn2 = server.beginTransaction()) {
          bean.setName("barney");
          Ebean.save(bean);
          txn2.commit();
        }

        txn1.rollback();
      }

      EBasic fresh = server.find(EBasic.class)
        .setId(bean.getId())
        .setPersistenceContextScope(PersistenceContextScope.QUERY)
        .findOne();

      assertNotNull(fresh);
      assertThat(fresh.getName()).isEqualTo("start2");
    }
  }

  @IgnorePlatform({Platform.SQLSERVER, Platform.MYSQL})
  @Test
  public void nestedUseSavepoint_doubleNested_commitRollback() {

    EbeanServer server = server();

    EBasic bean = new EBasic("start3");

    try (Transaction txn0 = server.beginTransaction()) {
      txn0.setRollbackOnly();
      txn0.setNestedUseSavepoint();

      server.save(bean);

      try (Transaction txn1 = server.beginTransaction()) {
        bean.setName("updateNested3");
        server.save(bean);

        try (Transaction txn2 = server.beginTransaction()) {
          bean.setName("barney3");
          Ebean.save(bean);
          txn2.rollback();
        }

        txn1.commit();
      }

      EBasic fresh = server.find(EBasic.class)
        .setId(bean.getId())
        .setPersistenceContextScope(PersistenceContextScope.QUERY)
        .findOne();

      assertNotNull(fresh);
      assertThat(fresh.getName()).isEqualTo("updateNested3");
    }
  }

  @IgnorePlatform({Platform.SQLSERVER, Platform.MYSQL})
  @Test
  public void nestedUseSavepoint_nested_RequiresNew() {

    EbeanServer server = server();

    EBasic bean = new EBasic("start4");

    try (Transaction txn0 = server.beginTransaction()) {
      txn0.setRollbackOnly();
      txn0.setNestedUseSavepoint();

      server.save(bean);

      try (Transaction txn1 = server.beginTransaction(TxScope.requiresNew())) {
        bean.setName("updateNested4");
        server.save(bean);
        txn1.commit();
      }

      EBasic fresh = server.find(EBasic.class)
        .setId(bean.getId())
        .setPersistenceContextScope(PersistenceContextScope.QUERY)
        .findOne();

      assertNotNull(fresh);
      assertThat(fresh.getName()).isEqualTo("updateNested4");
    }

    EBasic after = server.find(EBasic.class)
      .setId(bean.getId())
      .findOne();

    assertNull(after);
  }

  @IgnorePlatform({Platform.SQLSERVER, Platform.MYSQL})
  @Test
  public void nestedUseSavepoint() {

    EbeanServer server = server();

    EBasic bean = new EBasic("start1");

    try (Transaction txn0 = server.beginTransaction()) {
      txn0.setRollbackOnly();
      txn0.setNestedUseSavepoint();

      server.save(bean);

      try (Transaction txn1 = server.beginTransaction()) {
        bean.setName("updateNested");
        server.save(bean);
        txn1.commit();
      }

      EBasic fresh = server.find(EBasic.class, bean.getId());
      assertNotNull(fresh);
      assertThat(fresh.getName()).isEqualTo("updateNested");

      try (Transaction txn2 = server.beginTransaction()) {
        bean.setName("barney");
        Ebean.save(bean);
        txn2.rollback();
      }

      fresh = server.find(EBasic.class)
        .setId(bean.getId())
        .setPersistenceContextScope(PersistenceContextScope.QUERY)
        .findOne();

      assertNotNull(fresh);
      assertThat(fresh.getName()).isEqualTo("updateNested");

    } finally {
      cleanup(bean);
    }
  }

  private void cleanup(EBasic bean) {
    Ebean.delete(EBasic.class, bean.getId());
  }
}

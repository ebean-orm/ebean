package org.tests.transaction;

import io.ebean.*;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.IgnorePlatform;
import io.ebean.annotation.Platform;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.api.TransactionEvent;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasic;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestNestedSubTransaction extends BaseTestCase {

  /**
   * MySql only supports named savepoints - review.
   */
  @IgnorePlatform({Platform.MYSQL, Platform.SQLSERVER, Platform.HANA})
  @Test
  public void ebeanServer_commitTransaction_expect_sameAsTransactionCommit() {

    Database server = server();

    EBasic bean = new EBasic("x1");

    try (Transaction txn0 = server.beginTransaction()) {
      txn0.setRollbackOnly();

      server.save(bean);

      try (Transaction txn1 = server.beginTransaction()) {
        bean.setName("x2");
        server.save(bean);
        txn1.commit();
      }

      EBasic fresh = server.find(EBasic.class, bean.getId());
      assertNotNull(fresh); // FAILS
      assertThat(fresh.getName()).isEqualTo("x2");

      try (Transaction txn2 = server.beginTransaction()) {
        bean.setName("barney");
        DB.save(bean);
        txn2.commit();
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

  @IgnorePlatform({Platform.SQLSERVER, Platform.MYSQL, Platform.HANA, Platform.ORACLE})
  @Test
  void nestedNested() {
    LoggedSql.start();
    try (Transaction txn0 = DB.beginTransaction()) {
      runNestedMethod();
      try (Transaction txnAfter = DB.beginTransaction()) {
        DB.save(new EBasic("startAfter"));
      }
    }
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(3);
    assertThat(sql.get(0)).contains("insert into e_basic").contains("sp[");
    assertThat(sql.get(1)).contains("insert into e_basic").doesNotContain("sp[");
    assertThat(sql.get(2)).contains("insert into e_basic").doesNotContain("sp[");
  }

  void runNestedMethod() {
    try (Transaction txn0 = DB.beginTransaction()) {
      txn0.setNestedUseSavepoint();
      try (Transaction nested = DB.beginTransaction()) {
        DB.save(new EBasic("nested in sp"));
        nested.commit();
      }
      DB.save(new EBasic("nested"));
      txn0.commit();
    }
  }

  @IgnorePlatform({Platform.SQLSERVER, Platform.MYSQL, Platform.HANA, Platform.ORACLE})
  @Test
  public void nestedUseSavepoint_doubleNested_rollbackCommit() {

    Database server = server();

    EBasic bean = new EBasic("start2");

    try (Transaction txn0 = server.beginTransaction()) {
      txn0.setRollbackOnly();
      txn0.setNestedUseSavepoint();

      server.save(bean);

      TransactionEvent event0 = ((SpiTransaction) txn0).event();

      try (Transaction txn1 = server.beginTransaction()) {
        bean.setName("updateNested");
        server.save(bean);

        TransactionEvent event1 = ((SpiTransaction) txn1).event();
        assertThat(event1).isNotSameAs(event0);

        try (Transaction txn2 = server.beginTransaction()) {
          bean.setName("barney");
          DB.save(bean);
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

  @IgnorePlatform({Platform.SQLSERVER, Platform.MYSQL, Platform.HANA, Platform.ORACLE})
  @Test
  public void nestedUseSavepoint_doubleNested_commitRollback() {

    Database server = server();

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
          DB.save(bean);
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

  @IgnorePlatform({Platform.SQLSERVER, Platform.MYSQL, Platform.HANA, Platform.ORACLE})
  @Test
  public void nestedUseSavepoint_nested_RequiresNew() {

    Database server = server();

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

  @IgnorePlatform({Platform.SQLSERVER, Platform.MYSQL, Platform.HANA, Platform.ORACLE})
  @Test
  public void nestedUseSavepoint() {

    Database server = server();

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
        DB.save(bean);
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
    DB.delete(EBasic.class, bean.getId());
  }
}

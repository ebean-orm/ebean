package org.tests.basic;

import io.ebean.*;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.IgnorePlatform;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.EBasic;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestQueryForUpdate extends BaseTestCase {

  @IgnorePlatform({Platform.DB2})
  @Test
  public void testForUpdate() {

    ResetBasicData.reset();

    Query<Customer> query;
    try (final Transaction transaction = DB.beginTransaction()) {
      query = DB.find(Customer.class)
        .forUpdate()
        .order().desc("id");

      query.findList();
    }

    if (isSqlServer()) {
      assertThat(sqlOf(query)).contains("with (updlock)");
    } else {
      assertThat(sqlOf(query)).contains("for update");
    }
  }

  @IgnorePlatform({Platform.DB2})
  @Test
  public void testForUpdate_withLimit() {
    ResetBasicData.reset();

    Query<Customer> query;
    try (final Transaction transaction = DB.beginTransaction()) {
      query = DB.find(Customer.class)
        .forUpdate()
        .setMaxRows(3)
        .order().desc("id");

      query.findList();
    }

    if (isSqlServer()) {
      assertThat(sqlOf(query)).contains("with (updlock)");
    } else {
      assertThat(sqlOf(query)).contains("for update");
    }
  }

  @Test
  @ForPlatform({Platform.H2, Platform.ORACLE, Platform.POSTGRES, Platform.SQLSERVER, Platform.MYSQL, Platform.MARIADB})
  public void testForUpdate_when_alreadyInPCAsReference() {
    ResetBasicData.reset();
    Order o0 = DB.find(Order.class).orderBy("id").setMaxRows(1).findOne();
    Integer customerId = o0.getCustomer().getId();

    try (Transaction transaction = DB.beginTransaction()) {

      LoggedSql.start();
      Order order = DB.find(Order.class, o0.getId());
      assert order != null;

      Customer customer = order.getCustomer();
      assertTrue(DB.beanState(customer).isReference());
      assertEquals(customerId, customer.getId());

      Customer customer1 = DB.find(Customer.class).where().idEq(customer.getId()).forUpdate().findOne();

      assertThat(customer).isSameAs(customer1);
      assertThat(customer.getName()).isNotNull();

      List<String> sql = LoggedSql.stop();
      assertThat(sql).hasSize(2);
      assertThat(sql.get(0)).contains("from o_order");
      if (isSqlServer()) {
        assertThat(sql.get(1)).contains("from o_customer t0 with (updlock) where t0.id = ?");
      } else {
        assertThat(sql.get(1)).contains("from o_customer t0 where t0.id = ? for update");
      }
      transaction.commit();
    }
  }

  @Test
  @ForPlatform({Platform.H2, Platform.ORACLE, Platform.POSTGRES, Platform.SQLSERVER, Platform.MYSQL, Platform.MARIADB})
  public void testForUpdate_when_alreadyInPCAsReference_usingLock() {
    ResetBasicData.reset();
    Order o0 = DB.find(Order.class).orderBy("id").setMaxRows(1).findOne();
    Integer customerId = o0.getCustomer().getId();

    try (Transaction transaction = DB.beginTransaction()) {

      LoggedSql.start();
      Order order = DB.find(Order.class, o0.getId());
      assert order != null;

      Customer customer = order.getCustomer();
      assertEquals(customerId, customer.getId());
      assertTrue(DB.beanState(customer).isReference());

      DB.lock(customer); // load the customer bean using select for update
      // bean is now loaded and database row lock held until commit
      assertFalse(DB.beanState(customer).isReference());

      assertThat(customer.getName()).isNotNull();

      List<String> sql = LoggedSql.stop();
      assertThat(sql).hasSize(2);
      assertThat(sql.get(0)).contains("from o_order");
      if (isSqlServer()) {
        assertThat(sql.get(1)).contains("from o_customer t0 with (updlock,nowait) where t0.id = ?");
      } else {
        assertThat(sql.get(1)).contains("from o_customer t0 where t0.id = ? for update");
      }
      transaction.commit();
    }
  }

  @Test
  @ForPlatform({Platform.H2, Platform.ORACLE, Platform.POSTGRES, Platform.SQLSERVER, Platform.MYSQL, Platform.MARIADB})
  public void testForUpdate_when_alreadyInPC() {

    EBasic basic = new EBasic("test PC cache");
    DB.save(basic);

    try (Transaction transaction = DB.beginTransaction()) {

      LoggedSql.start();

      EBasic basic0 = DB.find(EBasic.class, basic.getId());
      assertThat(basic0).isNotNull();

      EBasic basic1 = DB.find(EBasic.class)
        .setId(basic.getId())
        .forUpdate()
        .findOne();

      assertThat(basic1).isNotNull();
      assertThat(basic1).isSameAs(basic0);

      List<String> sql = LoggedSql.stop();
      assertThat(sql).hasSize(2);
      if (isH2() || isPostgresCompatible()) {
        assertSql(sql.get(0)).contains("from e_basic t0 where t0.id =");
        assertSql(sql.get(1)).contains("from e_basic t0 where t0.id =");
        assertSql(sql.get(1)).contains("for update");
      }

      transaction.end();
    }
  }

  @Test
  @ForPlatform({Platform.H2, Platform.POSTGRES, Platform.SQLSERVER, Platform.ORACLE})
  public void testForUpdate_noWait_noMaxRows() {

    ResetBasicData.reset();

    Query<Customer> query = DB.find(Customer.class)
      .forUpdateNoWait()
      .order().desc("id");

    query.findList();
    if (isOracle()) {
      assertThat(sqlOf(query)).contains("for update nowait");
    } else if (isH2()) {
      assertThat(sqlOf(query)).contains("for update");
    } else if (isSqlServer()) {
      assertThat(sqlOf(query)).contains("with (updlock,nowait)");
    } else {
      assertThat(sqlOf(query)).contains("for update nowait");
    }
  }

  @Test
  @ForPlatform({Platform.H2, Platform.POSTGRES, Platform.SQLSERVER})
  public void testForUpdate_noWait() {

    ResetBasicData.reset();

    Database server = DB.getDefault();

    try (Transaction txn = DB.beginTransaction()) {
      Query<Customer> query = DB.find(Customer.class)
        .forUpdateNoWait()
        .setMaxRows(1)
        .order().desc("id");

      List<Customer> list = query.findList();
      Customer first = list.get(0);
      if (isSqlServer()) {
        assertThat(sqlOf(query)).contains("with (updlock,nowait)");
      } else if (isH2()) {
        assertThat(sqlOf(query)).contains("for update");
      } else {
        assertThat(sqlOf(query)).contains("for update nowait");
      }
      // create a 2nd transaction to test that the
      // row is locked and we can't acquire it
      try (Transaction txn2 = server.createTransaction()) {
        logger.info("... attempt another acquire using 2nd transaction");
        server.find(Customer.class)
          .where().idEq(first.getId())
          .forUpdateNoWait()
          .usingTransaction(txn2)
          .findOne();

        assertTrue(false); // never get here
      } catch (AcquireLockException e) {
        logger.info("... got AcquireLockException " + e);
      }
      txn.commit();
    }
  }
}

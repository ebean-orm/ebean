package org.tests.basic;

import io.ebean.AcquireLockException;
import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Query;
import io.ebean.Transaction;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.EBasic;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class TestQueryForUpdate extends BaseTestCase {

  @Test
  @ForPlatform({Platform.H2, Platform.ORACLE, Platform.POSTGRES, Platform.SQLSERVER, Platform.MYSQL, Platform.MARIADB})
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
    } else if (isPostgres()) {
      assertThat(sqlOf(query)).contains("for update");
    } else {
      assertThat(sqlOf(query)).contains("for update");
    }
  }

  @Test
  @ForPlatform({ Platform.H2, Platform.ORACLE, Platform.POSTGRES, Platform.SQLSERVER, Platform.MYSQL, Platform.MARIADB})
  public void testForUpdate_when_alreadyInPC() {

    EBasic basic = new EBasic("test PC cache");
    DB.save(basic);

    try (Transaction transaction = DB.beginTransaction()) {

      LoggedSqlCollector.start();

      EBasic basic0 = DB.find(EBasic.class, basic.getId());
      assertThat(basic0).isNotNull();

      EBasic basic1 = DB.find(EBasic.class)
        .setId( basic.getId())
        .forUpdate()
        .findOne();

      assertThat(basic1).isNotNull();
      assertThat(basic1).isSameAs(basic0);

      List<String> sql = LoggedSqlCollector.stop();
      assertThat(sql).hasSize(2);
      if (isH2() || isPostgres()) {
        assertSql(sql.get(0)).contains("from e_basic t0 where t0.id =");
        assertSql(sql.get(1)).contains("from e_basic t0 where t0.id =");
        if (isPostgres()) {
          assertSql(sql.get(1)).contains("for update");
        } else {
          assertSql(sql.get(1)).contains("for update");
        }
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
    } else if (isPostgres()) {
      assertThat(sqlOf(query)).contains("for update nowait");
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
      } else if (isPostgres()) {
        assertThat(sqlOf(query)).contains("for update nowait");
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

package org.tests.basic;

import io.ebean.AcquireLockException;
import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import io.ebean.Transaction;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class TestQueryForUpdate extends BaseTestCase {

  @Test
  public void testForUpdate() {

    if (isH2() || isPostgres()) {
      ResetBasicData.reset();

      Query<Customer> query = Ebean.find(Customer.class)
        .forUpdate()
        .setMaxRows(1)
        .order().desc("id");

      query.findList();
      assertThat(sqlOf(query)).contains("for update");
    }
  }

  @Test
  public void testForUpdate_noWait() {

    if (isPostgres() || isOracle()) {
      ResetBasicData.reset();

      EbeanServer server = Ebean.getDefaultServer();

      Ebean.beginTransaction();
      try {
        Query<Customer> query = Ebean.find(Customer.class)
          .forUpdateNoWait()
          .setMaxRows(1)
          .order().desc("id");

        List<Customer> list = query.findList();
        Customer first = list.get(0);

        assertThat(sqlOf(query)).contains("for update nowait");

        // create a 2nd transaction to test that the
        // row is locked and we can't acquire it
        Transaction txn2 = server.createTransaction();
        try {
          logger.info("... attempt another acquire using 2nd transaction");
          Query<Customer> query2 =
            server.find(Customer.class)
              .where().idEq(first.getId())
              .forUpdateNoWait();

          server.findUnique(query2, txn2);
          assertTrue(false); // never get here
        } catch (AcquireLockException e) {
          logger.info("... got AcquireLockException " + e);
        } finally {
          txn2.end();
        }

      } finally {
        Ebean.endTransaction();
      }
    }
  }
}

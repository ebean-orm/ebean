package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.tests.model.basic.Customer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class TestConnectionCloseOnSqlerr extends BaseTestCase {

  static boolean runManuallyNow = true;

  @Test
  public void test() {

    if (runManuallyNow) {
      return;
    }

    try {
      for (int i = 0; i < 100; i++) {
        try {
          Query<Customer> q0 = DB.find(Customer.class).where().icontains("namexxx", "Rob")
            .query();

          q0.findList();
        } catch (Exception e) {
          if (e.getMessage().contains("Unsuccessfully waited")) {
            fail();
          } else {
            e.printStackTrace();
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  @Test
  public void testTransactional() {

    if (runManuallyNow) {
      return;
    }

    try {
      for (int i = 0; i < 100; i++) {
        DB.beginTransaction();
        try {
          Query<Customer> q0 = DB.find(Customer.class).where().icontains("namexxx", "Rob")
            .query();

          q0.findList();
          DB.commitTransaction();
        } catch (Exception e) {
          if (e.getMessage().contains("Unsuccessfully waited")) {
            fail("No connections found while only one thread is running. (after " + i + " queries)");
          } else {
            e.printStackTrace();
          }
        } finally {
          if (DB.currentTransaction().isActive()) {
            DB.rollbackTransaction();
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}

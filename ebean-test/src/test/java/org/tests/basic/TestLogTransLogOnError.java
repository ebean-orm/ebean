package org.tests.basic;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.EBasicVer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestLogTransLogOnError extends BaseTestCase {

  @Test
  public void testQueryError() {

    ResetBasicData.reset();

    try (Transaction txn = DB.beginTransaction()) {

      DB.find(Customer.class).findList();
      DB.find(Order.class).where().gt("id", 1).findList();

      EBasicVer newBean = new EBasicVer("aName");
      newBean.setDescription("something");

      // DB.save(newBean);

      // t.log("--- next query should error");
      List<Customer> list = DB.find(Customer.class).where().eq("id", "NotAnInt!!").findList();

      assertEquals(0, list.size());
      // Get here with mysql?
      // assertTrue(false);
      txn.commit();

    } catch (RuntimeException e) {
      // e.printStackTrace();
      assertTrue(true);
    }
  }

  public void testPersistError() {

    ResetBasicData.reset();

    try (Transaction txn = DB.beginTransaction()) {
      DB.find(Customer.class).findList();

      EBasicVer newBean = new EBasicVer("aName");
      newBean
        .setDescription("something sdfjksdjflsjdflsjdflksjdfkjd fsjdfkjsdkfjsdkfjskdjfskjdf"
          + " sjdf sdjflksjdfkjsdlfkjsdkfjs ksjdfksjdlfjsldf something sdfjksdjflsjdflsjdflksjdfkjd"
          + "fsjdfkjsdkfjsdkfjskdjfskjdf sjdf sdjflksjdfkjsdlfkjsdkfjs ksjdfksjdlfjsldf something s"
          + "dfjksdjflsjdflsjdflksjdfkjd fsjdfkjsdkfjsdkfjskdjfskjdf sjdf sdjflksjdfkjsdlfkjsdkfjs ");

      // t.log("--- next insert should error");
      DB.save(newBean);

      // never get here
      assertTrue(false);
      txn.commit();

    } catch (RuntimeException e) {
      // e.printStackTrace();
      assertTrue(true);
    }
  }
}

package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.Customer;
import org.tests.model.basic.EBasicVer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestLogTransLogOnError extends BaseTestCase {

  @Test
  public void testQueryError() {

    ResetBasicData.reset();

    Ebean.beginTransaction();
    try {

      Ebean.find(Customer.class).findList();
      Ebean.find(Order.class).where().gt("id", 1).findList();

      EBasicVer newBean = new EBasicVer("aName");
      newBean.setDescription("something");

      // Ebean.save(newBean);

      // t.log("--- next query should error");
      List<Customer> list = Ebean.find(Customer.class).where().eq("id", "NotAnInt!!").findList();

      Assert.assertEquals(0, list.size());
      // Get here with mysql?
      // Assert.assertTrue(false);

    } catch (RuntimeException e) {
      // e.printStackTrace();
      Assert.assertTrue(true);

    } finally {
      Ebean.endTransaction();
    }
  }

  public void testPersistError() {

    ResetBasicData.reset();

    Ebean.beginTransaction();
    try {
      Ebean.find(Customer.class).findList();

      EBasicVer newBean = new EBasicVer("aName");
      newBean
        .setDescription("something sdfjksdjflsjdflsjdflksjdfkjd fsjdfkjsdkfjsdkfjskdjfskjdf"
          + " sjdf sdjflksjdfkjsdlfkjsdkfjs ksjdfksjdlfjsldf something sdfjksdjflsjdflsjdflksjdfkjd"
          + "fsjdfkjsdkfjsdkfjskdjfskjdf sjdf sdjflksjdfkjsdlfkjsdkfjs ksjdfksjdlfjsldf something s"
          + "dfjksdjflsjdflsjdflksjdfkjd fsjdfkjsdkfjsdkfjskdjfskjdf sjdf sdjflksjdfkjsdlfkjsdkfjs ");

      // t.log("--- next insert should error");
      Ebean.save(newBean);

      // never get here
      Assert.assertTrue(false);

    } catch (RuntimeException e) {
      // e.printStackTrace();
      Assert.assertTrue(true);

    } finally {
      Ebean.endTransaction();
    }

  }
}

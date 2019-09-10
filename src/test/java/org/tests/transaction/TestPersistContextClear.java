package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Transaction;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.api.SpiTransaction;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestPersistContextClear extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Order order0 = null;

    Transaction t = Ebean.beginTransaction();
    try {
      ResetBasicData.createOrderCustAndOrder("testPc");
      SpiTransaction spiTxn = (SpiTransaction) t;
      PersistenceContext pc = spiTxn.getPersistenceContext();

      // no orders or customers in the PC
      Assert.assertEquals(0, pc.size(Order.class));
      Assert.assertEquals(0, pc.size(Customer.class));


      EbeanServer server = Ebean.getServer(null);
      List<Order> list = server.find(Order.class).fetch("customer").fetch("details").findList();

      int orderSize = list.size();
      Assert.assertTrue(orderSize > 1);

      // keep a hold of one of them
      order0 = list.get(0);

      Assert.assertEquals(orderSize, pc.size(Order.class));

      System.gc();
      Assert.assertEquals(orderSize, pc.size(Order.class));
      Assert.assertTrue(pc.size(Customer.class) > 0);

      list = null;
      // System.gc();

      // transaction still holds PC ...
      // System.out.println("pc2:"+pc);
      // These asserts may not succeed depending on JVM
      // Assert.assertEquals(pc.size(Order.class), 1);
      // Assert.assertEquals(pc.size(Customer.class), 1);

    } finally {
      t.end();
    }

    System.gc();

    // we still have the order
    Assert.assertNotNull(order0);
    // its likely the only one in the PC now
    // due to the System.gc(); but can't garuntee it
    // so removing these asserts... can put them back
    // to manually test.
    // Assert.assertEquals(pc.size(Order.class), 1);
    // Assert.assertEquals(pc.size(Customer.class), 1);

  }

}

package org.tests.transaction;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Transaction;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.api.SpiTransaction;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestPersistContextClear extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Order order0 = null;

    Transaction t = DB.beginTransaction();
    try {
      ResetBasicData.createOrderCustAndOrder("testPc");
      SpiTransaction spiTxn = (SpiTransaction) t;
      PersistenceContext pc = spiTxn.getPersistenceContext();

      // no orders or customers in the PC
      assertEquals(0, pc.size(Order.class));
      assertEquals(0, pc.size(Customer.class));

      Database server = DB.getDefault();
      List<Order> list = server.find(Order.class).fetch("customer").fetch("details").findList();

      int orderSize = list.size();
      assertTrue(orderSize > 1);

      // keep a hold of one of them
      order0 = list.get(0);

      assertEquals(orderSize, pc.size(Order.class));

      System.gc();
      assertEquals(orderSize, pc.size(Order.class));
      assertTrue(pc.size(Customer.class) > 0);

      list = null;
      // System.gc();

      // transaction still holds PC ...
      // System.out.println("pc2:"+pc);
      // These asserts may not succeed depending on JVM
      // assertEquals(pc.size(Order.class), 1);
      // assertEquals(pc.size(Customer.class), 1);

    } finally {
      t.end();
    }

    System.gc();

    // we still have the order
    assertNotNull(order0);
    // its likely the only one in the PC now
    // due to the System.gc(); but can't garuntee it
    // so removing these asserts... can put them back
    // to manually test.
    // assertEquals(pc.size(Order.class), 1);
    // assertEquals(pc.size(Customer.class), 1);

  }

}

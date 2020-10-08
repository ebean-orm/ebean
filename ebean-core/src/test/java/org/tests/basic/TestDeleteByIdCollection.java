package org.tests.basic;

import io.ebean.Ebean;
import io.ebean.TransactionalTestCase;

import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestDeleteByIdCollection extends TransactionalTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Customer c0 = ResetBasicData.createCustomer("del1", "del1 ship", "del1 bill", 1);
    Customer c1 = ResetBasicData.createCustomer("del2", "del2 ship", "del2 bill", 2);

    Ebean.save(c0);
    Ebean.save(c1);

    Customer c0Back = Ebean.find(Customer.class, c0.getId());
    Customer c1Back = Ebean.find(Customer.class, "" + c1.getId());

    Assert.assertNotNull(c0Back);
    Assert.assertNotNull(c1Back);

    List<String> ids = new ArrayList<>();
    // also test id type conversion
    ids.add("" + c0.getId());
    ids.add("" + c1.getId());


    Ebean.deleteAll(Customer.class, ids);
    awaitL2Cache();

    c0Back = Ebean.find(Customer.class, c0.getId());
    c1Back = Ebean.find(Customer.class, "" + c1.getId());

    Assert.assertNull(c0Back);
    Assert.assertNull(c1Back);
  }

  @Test
  public void testDelByStatement() {

    ResetBasicData.reset();

    Order order0 = ResetBasicData.createOrderCustAndOrder("delBySql 0");
    Order order1 = ResetBasicData.createOrderCustAndOrder("delBySql 1");

    Order o0Back = Ebean.find(Order.class, order0.getId());
    Order o1Back = Ebean.find(Order.class, order1.getId());

    Assert.assertNotNull(o0Back);
    Assert.assertNotNull(o1Back);


    List<Object> ids = new ArrayList<>();
    // also test id type conversion
    ids.add(order0.getId());
    ids.add(order1.getId());

    Ebean.deleteAll(Order.class, ids);
    awaitL2Cache();

    o0Back = Ebean.find(Order.class, order0.getId());
    o1Back = Ebean.find(Order.class, order1.getId());

    Assert.assertNull(o0Back);
    Assert.assertNull(o1Back);
  }

}

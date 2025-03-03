package org.tests.basic;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.base.TransactionalTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestDeleteByIdCollection extends TransactionalTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Customer c0 = ResetBasicData.createCustomer("del1", "del1 ship", "del1 bill", 1);
    Customer c1 = ResetBasicData.createCustomer("del2", "del2 ship", "del2 bill", 2);

    DB.save(c0);
    DB.save(c1);

    Customer c0Back = DB.find(Customer.class, c0.getId());
    Customer c1Back = DB.find(Customer.class, "" + c1.getId());

    assertNotNull(c0Back);
    assertNotNull(c1Back);

    List<String> ids = new ArrayList<>();
    // also test id type conversion
    ids.add("" + c0.getId());
    ids.add("" + c1.getId());


    DB.deleteAll(Customer.class, ids);
    awaitL2Cache();

    LoggedSql.start();
    c0Back = DB.find(Customer.class, c0.getId());
    c1Back = DB.find(Customer.class, "" + c1.getId());
    assertThat(LoggedSql.stop()).isEmpty();

    assertNull(c0Back);
    assertNull(c1Back);
  }

  @Test
  public void testDelByStatement() {

    ResetBasicData.reset();

    Order order0 = ResetBasicData.createOrderCustAndOrder("delBySql 0");
    Order order1 = ResetBasicData.createOrderCustAndOrder("delBySql 1");

    Order o0Back = DB.find(Order.class, order0.getId());
    Order o1Back = DB.find(Order.class, order1.getId());

    assertNotNull(o0Back);
    assertNotNull(o1Back);


    List<Object> ids = new ArrayList<>();
    // also test id type conversion
    ids.add(order0.getId());
    ids.add(order1.getId());

    DB.deleteAll(Order.class, ids);
    awaitL2Cache();

    o0Back = DB.find(Order.class, order0.getId());
    o1Back = DB.find(Order.class, order1.getId());

    assertNull(o0Back);
    assertNull(o1Back);
  }

}

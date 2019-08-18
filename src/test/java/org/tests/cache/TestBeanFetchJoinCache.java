package org.tests.cache;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static io.ebean.CacheMode.ON;

public class TestBeanFetchJoinCache extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

//    DB.find(Customer.class)
//      .setBeanCacheMode(ON)
//      .findList();


    DB.find(Customer.class)
      .where().idIn(1,2,3)
      //.setBeanCacheMode(ON)
      .findList();

    List<Order> orders = DB.find(Order.class)
      .fetchQuery("customer", "name")
      .findList();


    orders = DB.find(Order.class)
      .fetchQuery("customer", "+cache, name")
      .findList();

    for (Order order : orders) {
      order.getCustomer().getName();
    }

  }
}

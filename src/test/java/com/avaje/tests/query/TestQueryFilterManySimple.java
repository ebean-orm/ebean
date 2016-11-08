package com.avaje.tests.query;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.FetchConfig;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.sql.Date;
import java.util.List;

public class TestQueryFilterManySimple extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    // not really last week :)
    Date lastWeek = Date.valueOf("2010-01-01");

    List<Customer> list = Ebean
      .find(Customer.class)
      // .join("orders", new JoinConfig().lazy())
      // .join("orders", new JoinConfig().query())
      .fetch("orders").fetch("contacts", new FetchConfig().query()).where().ilike("name", "rob%")
      .filterMany("orders").eq("status", Order.Status.NEW).gt("orderDate", lastWeek)
      .filterMany("contacts").isNotNull("firstName").findList();

    // invoke lazy loading
    list.get(0).getOrders().size();
  }
}

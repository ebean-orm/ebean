package com.avaje.tests.query;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.ebean.QueryIterator;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestQueryFindIterate extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getServer(null);

    Query<Customer> query = server.find(Customer.class)
        .setAutofetch(false)
        //.fetch("contacts", new FetchConfig().query(2)).where().gt("id", 0).orderBy("id")
        .setMaxRows(2);

    int count = 0;

    QueryIterator<Customer> it = query.findIterate();
    try {
      while (it.hasNext()) {
        Customer customer = it.next();
        customer.hashCode();
        count++;
      }
    } finally {
      it.close();
    }

    Assert.assertEquals(2, count);
  }
}

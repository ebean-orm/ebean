package com.avaje.tests.query;

import javax.persistence.PersistenceException;

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
  
  @Test(expected=PersistenceException.class)
  public void testWithExceptionInQuery() {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getServer(null);

    // intentionally a query with incorrect type binding
    Query<Customer> query = server.find(Customer.class)
        .setAutofetch(false)
        .where().gt("id","JUNK_NOT_A_LONG")
        .setMaxRows(2);

    // this throws an exception immediately
    QueryIterator<Customer> it = query.findIterate();
    it.hashCode();
    Assert.assertTrue("Never get here as exception thrown", false);
  }
  
  
  @Test(expected=IllegalStateException.class)
  public void testWithExceptionInLoop() {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getServer(null);

    Query<Customer> query = server.find(Customer.class)
        .setAutofetch(false)
        .where().gt("id", 0)
        .setMaxRows(2);

    QueryIterator<Customer> it = query.findIterate();
    try {
      while (it.hasNext()) {
        Customer customer = it.next();
        if (customer != null) {
          throw new IllegalStateException("cause an exception");
        }
      }
      
    } finally {
      it.close();
    }
  }
}

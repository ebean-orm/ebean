package com.avaje.tests.update;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Customer;

public class TestUpdatePartial extends BaseTestCase {

  @Test
  public void test() {

    Customer c = new Customer();
    c.setName("TestUpdateMe");
    c.setStatus(Customer.Status.ACTIVE);
    c.setSmallnote("a note");

    Ebean.save(c);

    Customer c2 = Ebean.find(Customer.class)
        .select("status, smallnote")
        .setId(c.getId())
        .findUnique();
    
    c2.setStatus(Customer.Status.INACTIVE);
    c2.setSmallnote("2nd note");

    Ebean.save(c2);

    Customer c3 = Ebean.find(Customer.class)
        .select("status")
        .setId(c.getId())
        .findUnique();
   
    c3.setStatus(Customer.Status.NEW);
    c3.setSmallnote("3rd note");

    Ebean.save(c3);

  }

  /**
   * If we have no changes detected, don't execute an Update and don't update the Version column.
   */
  @Test
  public void testWithoutChangesAndVersionColumn() {
    // arrange
    Customer customer = new Customer();
    customer.setName("something");

    Ebean.save(customer);

    // act
    Customer customerWithoutChanges = Ebean.find(Customer.class, customer.getId());
    Ebean.save(customerWithoutChanges);

    // assert
    Assert.assertEquals(customer.getUpdtime().getTime(), customerWithoutChanges.getUpdtime().getTime());
  }
}

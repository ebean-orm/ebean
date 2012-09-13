package com.avaje.tests.basic;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Assert;

import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Contact;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestLazyLoadEmptyOneToMany extends TestCase {

  public void test() {

    ResetBasicData.reset();

    Customer c = new Customer();
    c.setName("testll");

    Ebean.save(c);

    Customer c1 = Ebean.find(Customer.class)
        .setAutofetch(false)
        .select("id")
        .fetch("contacts", "id")
        .where().idEq(c.getId())
        .findUnique();

    List<Contact> contacts = c1.getContacts();
    int sz = contacts.size();

    Assert.assertTrue(sz == 0);
  }
}

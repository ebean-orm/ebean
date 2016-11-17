package com.avaje.tests.basic;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.Contact;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestLazyLoadEmptyOneToMany extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Customer c = new Customer();
    c.setName("testll");

    Ebean.save(c);

    Customer c1 = Ebean.find(Customer.class)
      .setAutoTune(false)
      .select("id")
      .fetch("contacts", "id")
      .where().idEq(c.getId())
      .findUnique();

    List<Contact> contacts = c1.getContacts();
    int sz = contacts.size();

    Assert.assertTrue(sz == 0);
  }
}

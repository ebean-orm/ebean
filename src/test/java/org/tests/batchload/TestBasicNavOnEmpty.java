package org.tests.batchload;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestBasicNavOnEmpty extends BaseTestCase {

  @Test
  public void test() {

    Customer c = new Customer();
    c.setName("HelloRob");

    Ebean.save(c);

    c = Ebean.find(Customer.class, c.getId());

    List<Contact> contacts = c.getContacts();
    Assert.assertEquals(0, contacts.size());
  }

}

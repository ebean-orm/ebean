package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Assert;
import org.junit.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

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
      .findOne();

    List<Contact> contacts = c1.getContacts();
    int sz = contacts.size();

    Assert.assertTrue(sz == 0);

    // cleanup
    Ebean.delete(c1);
  }
}

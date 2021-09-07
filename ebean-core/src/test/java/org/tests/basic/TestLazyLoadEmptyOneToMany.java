package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestLazyLoadEmptyOneToMany extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Customer c = new Customer();
    c.setName("testll");

    DB.save(c);

    Customer c1 = DB.find(Customer.class)
      .setAutoTune(false)
      .select("id")
      .fetch("contacts", "id")
      .where().idEq(c.getId())
      .findOne();

    List<Contact> contacts = c1.getContacts();
    int sz = contacts.size();

    assertTrue(sz == 0);

    // cleanup
    DB.delete(c1);
  }
}

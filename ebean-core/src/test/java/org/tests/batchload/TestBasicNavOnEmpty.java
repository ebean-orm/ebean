package org.tests.batchload;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestBasicNavOnEmpty extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Customer c = new Customer();
    c.setName("HelloRob");

    DB.save(c);

    c = DB.find(Customer.class, c.getId());

    List<Contact> contacts = c.getContacts();
    assertEquals(0, contacts.size());

    // cleanup
    DB.delete(c);
  }

}

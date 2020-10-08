package org.tests.batchload;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Assert;
import org.junit.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

public class TestBasicNavOnEmpty extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Customer c = new Customer();
    c.setName("HelloRob");

    Ebean.save(c);

    c = Ebean.find(Customer.class, c.getId());

    List<Contact> contacts = c.getContacts();
    Assert.assertEquals(0, contacts.size());

    // cleanup
    Ebean.delete(c);
  }

}

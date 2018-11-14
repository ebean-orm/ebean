package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.junit.Assert;
import org.junit.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

public class TestLazyLoadGlobalConfig extends BaseTestCase {

  @Test
  public void test() {
    ResetBasicData.reset();

    Customer customer = Ebean.find(Customer.class, 1);

    if (customer != null) {
      List<Contact> contacts = customer.getContacts();
      Assert.assertEquals(0, contacts.size());
    }
  }
}

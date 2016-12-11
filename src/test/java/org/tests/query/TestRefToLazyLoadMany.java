package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestRefToLazyLoadMany extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Customer> custList = Ebean.find(Customer.class).select("id").findList();

    Customer c = custList.get(0);

    List<Contact> contacts2 = c.getContacts();
    Assert.assertEquals(3, Ebean.getBeanState(c).getLoadedProps().size());

    // now lazy load the contacts
    contacts2.size();

    Customer c2 = Ebean.getReference(Customer.class, c.getId());

    // we only "loaded" the contacts BeanList and not all of c2
    List<Contact> contacts = c2.getContacts();
    // Set<String> loadedProps = Ebean.getBeanState(c2).getLoadedProps();
    // assertEquals(1, loadedProps.size());

    // now lazy load the contacts
    contacts.size();

  }
}

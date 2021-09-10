package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRefToLazyLoadMany extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Customer> custList = DB.find(Customer.class).select("id").findList();

    Customer c = custList.get(0);

    List<Contact> contacts2 = c.getContacts();
    assertEquals(3, DB.beanState(c).loadedProps().size());

    // now lazy load the contacts
    contacts2.size();

    Customer c2 = DB.reference(Customer.class, c.getId());

    // we only "loaded" the contacts BeanList and not all of c2
    List<Contact> contacts = c2.getContacts();
    // Set<String> loadedProps = DB.beanState(c2).getLoadedProps();
    // assertEquals(1, loadedProps.size());

    // now lazy load the contacts
    contacts.size();

  }
}

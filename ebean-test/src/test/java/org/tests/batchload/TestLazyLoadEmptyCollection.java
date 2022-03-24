package org.tests.batchload;

import io.ebean.DB;
import io.ebean.xtest.base.TransactionalTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;

import java.util.List;

public class TestLazyLoadEmptyCollection extends TransactionalTestCase {

  @Test
  public void test() {

    Customer c = new Customer();
    c.setName("lazytest");

    Contact con = new Contact("jim", "slim");
    c.addContact(con);

    DB.save(c);

    List<Customer> list = DB.find(Customer.class)
      .fetchQuery("contacts")
      .fetchQuery("contacts.notes")
      .findList();

    for (Customer customer : list) {
      List<Contact> contacts = customer.getContacts();
      for (Contact contact : contacts) {
        contact.getNotes();
      }
    }

  }

}

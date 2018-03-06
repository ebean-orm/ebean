package org.tests.batchload;

import io.ebean.Ebean;
import io.ebean.FetchConfig;
import io.ebean.TransactionalTestCase;

import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.junit.Test;

import java.util.List;

public class TestLazyLoadEmptyCollection extends TransactionalTestCase {

  @Test
  public void test() {

    Customer c = new Customer();
    c.setName("lazytest");

    Contact con = new Contact("jim", "slim");
    c.addContact(con);

    Ebean.save(c);

    List<Customer> list = Ebean.find(Customer.class)
      .fetch("contacts", new FetchConfig().query(0))
      .fetch("contacts.notes", new FetchConfig().query(100))
      .findList();

    for (Customer customer : list) {
      List<Contact> contacts = customer.getContacts();
      for (Contact contact : contacts) {
        contact.getNotes();
      }
    }

  }

}

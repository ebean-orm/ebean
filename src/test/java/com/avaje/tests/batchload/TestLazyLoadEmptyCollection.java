package com.avaje.tests.batchload;

import java.util.List;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.FetchConfig;
import com.avaje.tests.model.basic.Contact;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestLazyLoadEmptyCollection extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

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

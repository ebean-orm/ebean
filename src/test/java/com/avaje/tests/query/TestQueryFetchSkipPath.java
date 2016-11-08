package com.avaje.tests.query;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.*;
import org.junit.Test;

import java.util.List;

public class TestQueryFetchSkipPath extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Order> list = Ebean.find(Order.class)

      // .setAutoTune(true)
      .setAutoTune(false).select("status")
      // .fetch("customer","id")
      // .fetch("customer.contacts","id")
      .fetch("customer.contacts.notes", "title").findList();

    for (Order order : list) {
      order.getStatus();
      Customer customer = order.getCustomer();
      List<Contact> contacts = customer.getContacts();
      for (Contact contact : contacts) {
        List<ContactNote> notes = contact.getNotes();
        for (ContactNote contactNote : notes) {
          contactNote.getTitle();
        }
      }
    }

  }

}

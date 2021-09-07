package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestQueryJoinMulipleMany extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Order> list = DB.find(Order.class).fetch("customer").fetch("customer.contacts").where()
      .gt("id", 0).findList();

    assertNotNull(list);

    for (Order order : list) {
      List<Contact> contacts = order.getCustomer().getContacts();
      contacts.size();
    }

  }
}

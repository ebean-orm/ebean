package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestQueryJoinMulipleMany extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    List<Order> list = Ebean.find(Order.class).fetch("customer").fetch("customer.contacts").where()
      .gt("id", 0).findList();

    Assert.assertNotNull(list);

    for (Order order : list) {
      List<Contact> contacts = order.getCustomer().getContacts();
      contacts.size();
    }

  }
}

package com.avaje.tests.query.joins;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.FetchConfig;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import com.avaje.ebeaninternal.server.el.ElPropertyValue;
import com.avaje.tests.model.basic.Contact;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestQueryJoinQueryNonRoot extends BaseTestCase {

  @Test
  public void test() {

    SpiEbeanServer server = (SpiEbeanServer) Ebean.getServer(null);
    BeanDescriptor<Order> d = server.getBeanDescriptor(Order.class);
    ElPropertyValue elGetValue = d.getElGetValue("customer.contacts");

    Assert.assertTrue(elGetValue.containsMany());

    ResetBasicData.reset();

    List<Order> list = Ebean.find(Order.class)
        .fetch("customer")
        .fetch("customer.contacts", "firstName", new FetchConfig().query().lazy(10))
        .fetch("customer.contacts.group")
        .where().lt("id", 3).findList();

    Assert.assertNotNull(list);
    Assert.assertTrue(list.size() > 0);

    for (Order order : list) {
      List<Contact> contacts = order.getCustomer().getContacts();
      for (Contact contact : contacts) {
        contact.getFirstName();
      }
    }

    // String oq =
    // "find order join customer join customer.contacts join details (+query(4),+lazy(5))";
    // Query<Order> q = Ebean.createQuery(Order.class, oq);
    // q.setAutofetch(false);
    // List<Order> list2 = q.findList();
    //
    // Assert.assertTrue(list2.size() > 0);

  }
  

}

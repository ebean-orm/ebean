package org.tests.query.joins;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.FetchConfig;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.el.ElPropertyValue;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

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
    Assert.assertTrue(!list.isEmpty());

    for (Order order : list) {
      List<Contact> contacts = order.getCustomer().getContacts();
      for (Contact contact : contacts) {
        contact.getFirstName();
      }
    }

    // String oq =
    // "find order join customer join customer.contacts join details (+query(4),+lazy(5))";
    // Query<Order> q = Ebean.createQuery(Order.class, oq);
    // q.setAutoTune(false);
    // List<Order> list2 = q.findList();
    //
    // Assert.assertTrue(list2.size() > 0);

  }


}

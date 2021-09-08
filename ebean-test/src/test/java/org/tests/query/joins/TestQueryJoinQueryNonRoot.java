package org.tests.query.joins;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.el.ElPropertyValue;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestQueryJoinQueryNonRoot extends BaseTestCase {

  @Test
  public void test() {

    SpiEbeanServer server = (SpiEbeanServer) DB.getDefault();
    BeanDescriptor<Order> d = server.descriptor(Order.class);
    ElPropertyValue elGetValue = d.elGetValue("customer.contacts");

    assertTrue(elGetValue.containsMany());

    ResetBasicData.reset();

    List<Order> list = DB.find(Order.class)
      .fetch("customer")
      .fetchQuery("customer.contacts", "firstName")
      .fetch("customer.contacts.group")
      .where().lt("id", 3).findList();

    assertNotNull(list);
    assertTrue(!list.isEmpty());

    for (Order order : list) {
      List<Contact> contacts = order.getCustomer().getContacts();
      for (Contact contact : contacts) {
        contact.getFirstName();
      }
    }

    // String oq =
    // "find order join customer join customer.contacts join details (+query(4),+lazy(5))";
    // Query<Order> q = DB.createQuery(Order.class, oq);
    // q.setAutoTune(false);
    // List<Order> list2 = q.findList();
    //
    // assertTrue(list2.size() > 0);

  }


}

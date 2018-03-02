package org.tests.text.json;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.TransactionalTestCase;
import io.ebean.text.json.JsonContext;
import org.tests.model.basic.Customer;
import org.tests.model.basic.MRole;
import org.tests.model.basic.MUser;
import org.tests.model.basic.Order;
import org.tests.model.basic.Order.Status;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.Product;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class TestTextJsonUpdateCascade extends TransactionalTestCase {

  @Test
  public void test() throws IOException {

    Customer c0 = ResetBasicData.createCustAndOrder("Test Json");

    Customer c2 = Ebean.getReference(Customer.class, c0.getId());
    List<Order> orders = c2.getOrders();

    Assert.assertEquals(1, orders.size());

    Order order = orders.get(0);
    int size = order.getDetails().size();

    Assert.assertTrue(size >= 3);

    Customer cref = Ebean.getReference(Customer.class, c0.getId());
    order.setCustomer(cref);
    order.setStatus(Status.SHIPPED);

    OrderDetail orderDetail0 = order.getDetails().get(0);
    orderDetail0.setShipQty(300);
    orderDetail0.setUnitPrice(56.98d);

    // remove one of the details...
    OrderDetail removedDetail = order.getDetails().remove(2);
    Assert.assertNotNull(removedDetail);

    Product p = Ebean.getReference(Product.class, 1);
    OrderDetail newDetail = new OrderDetail(p, 899, 12.12d);
    // newDetail.setOrder(order);
    order.addDetail(newDetail);

    EbeanServer server = Ebean.getServer(null);

    JsonContext jsonContext = server.json();
    String jsonString = jsonContext.toJson(order);

    Order updOrder = jsonContext.toBean(Order.class, jsonString);

    server.update(updOrder);

    MRole r1 = new MRole();
    r1.setRoleName("rolej1");
    Ebean.save(r1);

    MRole r2 = new MRole();
    r2.setRoleName("rolej2");
    Ebean.save(r2);

    MRole r3 = new MRole();
    r3.setRoleName("rolej3");
    Ebean.save(r3);

    MUser u0 = new MUser();
    u0.setUserName("userj1");
    u0.addRole(r1);
    u0.addRole(r2);
    u0.addRole(r3);

    Ebean.save(u0);

    jsonContext.toJson(u0);

    String s = "{\"userid\":" + u0.getUserid()
      + ",\"userName\":\"userj1\", \"roles\":[{\"roleid\":" + r2.getRoleid() + "},{\"roleid\":"
      + r3.getRoleid() + "}]} ";

    MUser updMUser = jsonContext.toBean(MUser.class, s);

    server.update(updMUser);

    // checked transaction log to confirm correct behaviour
  }

}

package com.avaje.tests.text.json;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.MRole;
import com.avaje.tests.model.basic.MUser;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.Order.Status;
import com.avaje.tests.model.basic.OrderDetail;
import com.avaje.tests.model.basic.Product;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestTextJsonUpdateCascade extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

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

    JsonContext jsonContext = server.createJsonContext();
    String jsonString = jsonContext.toJsonString(order);
    System.out.println(jsonString);

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

    String jsonUser = jsonContext.toJsonString(u0);

    System.out.println(jsonUser);

    String s = "{\"userid\":" + u0.getUserid()
        + ",\"userName\":\"userj1\", \"roles\":[{\"roleid\":" + r2.getRoleid() + "},{\"roleid\":"
        + r3.getRoleid() + "}]} ";

    MUser updMUser = jsonContext.toBean(MUser.class, s);

    server.update(updMUser);

    // checked transaction log to confirm correct behaviour
  }

}

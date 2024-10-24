package org.tests.text.json;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.xtest.base.TransactionalTestCase;
import io.ebean.text.json.JsonContext;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.*;
import org.tests.model.basic.Order.Status;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestTextJsonUpdateCascade extends TransactionalTestCase {

  @Test
  public void test() throws IOException {

    Customer c0 = ResetBasicData.createCustAndOrder("Test Json");

    Customer c2 = DB.reference(Customer.class, c0.getId());
    List<Order> orders = c2.getOrders();

    assertEquals(1, orders.size());

    Order order = orders.get(0);
    int size = order.getDetails().size();

    assertTrue(size >= 3);

    Customer cref = DB.reference(Customer.class, c0.getId());
    order.setCustomer(cref);
    order.setStatus(Status.SHIPPED);

    OrderDetail orderDetail0 = order.getDetails().get(0);
    orderDetail0.setShipQty(300);
    orderDetail0.setUnitPrice(BigDecimal.valueOf(56.98d));

    // remove one of the details...
    OrderDetail removedDetail = order.getDetails().remove(2);
    assertNotNull(removedDetail);

    Product p = DB.reference(Product.class, 1);
    OrderDetail newDetail = new OrderDetail(p, 899, BigDecimal.valueOf(12.12d));
    // newDetail.setOrderBy(order);
    order.addDetail(newDetail);

    Database server = DB.getDefault();

    JsonContext jsonContext = server.json();
    String jsonString = jsonContext.toJson(order);

    Order updOrder = jsonContext.toBean(Order.class, jsonString);

    server.update(updOrder);

    MRole r1 = new MRole();
    r1.setRoleName("rolej1");
    DB.save(r1);

    MRole r2 = new MRole();
    r2.setRoleName("rolej2");
    DB.save(r2);

    MRole r3 = new MRole();
    r3.setRoleName("rolej3");
    DB.save(r3);

    MUser u0 = new MUser();
    u0.setUserName("userj1");
    u0.addRole(r1);
    u0.addRole(r2);
    u0.addRole(r3);

    DB.save(u0);

    jsonContext.toJson(u0);

    String s = "{\"userid\":" + u0.getUserid()
      + ",\"userName\":\"userj1\", \"roles\":[{\"roleid\":" + r2.getRoleid() + "},{\"roleid\":"
      + r3.getRoleid() + "}]} ";

    MUser updMUser = jsonContext.toBean(MUser.class, s);

    server.update(updMUser);

    // checked transaction log to confirm correct behaviour
  }

}

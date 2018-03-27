package org.tests.query.other;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.meta.BasicMetricVisitor;
import io.ebean.meta.MetaInfoManager;
import io.ebean.meta.MetaOrmQueryMetric;
import io.ebean.meta.MetaOrmQueryNode;
import org.junit.Assert;
import org.junit.Test;
import org.tests.model.basic.Address;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

public class TestObjectGraphNodeStatsCollection extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getServer(null);

    MetaInfoManager infoManager = server.getMetaInfoManager();

    server.find(Order.class).findCount();

    infoManager.collectNodeStatistics(true);
    infoManager.resetAllMetrics();

    runFindOrderQuery(server);
    runFindCustomerQuery(server);

    List<MetaOrmQueryNode> nodeStatistics = infoManager.collectNodeStatistics(true);
    for (MetaOrmQueryNode stat : nodeStatistics) {
      System.out.println(stat);
    }

    BasicMetricVisitor basic = infoManager.visitBasic();
    for (MetaOrmQueryMetric queryMetric : basic.getOrmQueryMetrics()) {
      System.out.println(queryMetric);
    }

  }

  private void runFindCustomerQuery(EbeanServer server) {

    List<Customer> customers = server.find(Customer.class)
      .select("name")
      .fetch("contacts")
      .findList();

    Assert.assertTrue(!customers.isEmpty());

    List<Customer> custs = server.find(Customer.class).select("name").findList();
    for (Customer customer : custs) {
      customer.getShippingAddress();
    }
  }

  private void runFindOrderQuery(EbeanServer server) {
    List<Order> orders = server.find(Order.class)
      .where().gt("id", 0)
      .order().asc("orderDate")
      .setMaxRows(40)
      .findList();

    for (Order order : orders) {
      Customer customer = order.getCustomer();
      Address billingAddress = customer.getBillingAddress();
      if (billingAddress != null) {
        billingAddress.getCity();
      }
      Address shippingAddress = customer.getShippingAddress();
      if (shippingAddress != null) {
        shippingAddress.getCity();
      }
      List<OrderDetail> details = order.getDetails();
      for (OrderDetail orderDetail : details) {
        orderDetail.getUnitPrice();
        orderDetail.getProduct().getName();
      }

    }

  }

  @Test
  public void testFindByIds() {

    ResetBasicData.reset();

    List<Integer> ids = Ebean.find(Order.class).findIds();
    Assert.assertTrue(!ids.isEmpty());

  }

}

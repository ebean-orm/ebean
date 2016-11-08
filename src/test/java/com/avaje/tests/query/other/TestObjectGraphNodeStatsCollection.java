package com.avaje.tests.query.other;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.meta.MetaInfoManager;
import com.avaje.ebean.meta.MetaObjectGraphNodeStats;
import com.avaje.ebean.meta.MetaQueryPlanStatistic;
import com.avaje.tests.model.basic.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestObjectGraphNodeStatsCollection extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getServer(null);

    MetaInfoManager infoManager = server.getMetaInfoManager();

    server.find(Order.class).findCount();

    infoManager.collectNodeStatistics(true);
    infoManager.collectQueryPlanStatistics(true);

    runFindOrderQuery(server);
    runFindCustomerQuery(server);

    List<MetaObjectGraphNodeStats> nodeStatistics = infoManager.collectNodeStatistics(true);
    for (MetaObjectGraphNodeStats stat : nodeStatistics) {
      stat.toString();
    }

    List<MetaQueryPlanStatistic> planStatistics = infoManager.collectQueryPlanStatistics(true);
    for (MetaQueryPlanStatistic planStatistic : planStatistics) {
      planStatistic.getSql();
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

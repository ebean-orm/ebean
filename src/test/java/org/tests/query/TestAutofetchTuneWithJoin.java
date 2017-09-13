package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.bean.ObjectGraphOrigin;
import io.ebeaninternal.api.SpiQuery;
import org.tests.model.basic.Address;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

public class TestAutofetchTuneWithJoin extends BaseTestCase {

  @Test
  public void test() {
    runQuery();
    collectUsage();
  }

  private void runQuery() {

    ResetBasicData.reset();

    Query<Order> q = Ebean.find(Order.class)
      .setAutoTune(true)
      //.fetch("customer")
      //.fetch("customer.contacts")
      .where().lt("id", 3).query();

    List<Order> list = q.findList();

    for (Order order : list) {
      order.getOrderDate();
      order.getShipDate();
      // order.setShipDate(new Date(System.currentTimeMillis()));
      Customer customer = order.getCustomer();
      customer.getName();
      Address shippingAddress = customer.getShippingAddress();
      if (shippingAddress != null) {
        shippingAddress.getLine1();
        shippingAddress.getCity();
      }
      // customer.getContacts()
    }

    SpiQuery<?> sq = (SpiQuery<?>) q;
    ObjectGraphNode parentNode = sq.getParentNode();
    ObjectGraphOrigin origin = parentNode.getOriginQueryPoint();
    assertThat(origin).isNotNull();
    // MetaAutoFetchStatistic metaAutoFetchStatistic =
    // ((DefaultOrmQuery<?>)q).getMetaAutoFetchStatistic();
    // if (metaAutoFetchStatistic != null) {
    // List<NodeUsageStats> nodeUsageStats =
    // metaAutoFetchStatistic.getNodeUsageStats();
    // System.out.println(nodeUsageStats);
    // List<QueryStats> queryStats = metaAutoFetchStatistic.getQueryStats();
    // System.out.println(queryStats);
    // }

  }

  private static void collectUsage() {

    Ebean.getDefaultServer().getAutoTune().collectProfiling();
  }

}

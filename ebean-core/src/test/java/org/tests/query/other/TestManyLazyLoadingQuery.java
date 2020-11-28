package org.tests.query.other;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestManyLazyLoadingQuery extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    SpiEbeanServer server = (SpiEbeanServer) Ebean.getServer(null);

    BeanDescriptor<Order> descOrder = server.getBeanDescriptor(Order.class);
    BeanPropertyAssocMany<?> beanProperty = (BeanPropertyAssocMany<?>) descOrder.getBeanProperty("details");

    List<Object> parentIds = new ArrayList<>();
    parentIds.add(1);


    List<Order> orders =
      Ebean.find(Order.class)
        .where().lt("id", 4)
        .findList();

    for (Order order : orders) {
      List<OrderDetail> details = order.getDetails();
      System.out.println(details.size());
    }


    // start transaction to keep PC going to lazy query
    Ebean.beginTransaction();
    try {
      Ebean.find(Order.class, 1);

      SpiQuery<?> query0 = (SpiQuery<?>) Ebean.find(OrderDetail.class);

      query0.setLazyLoadForParents(beanProperty);

      beanProperty.addWhereParentIdIn(query0, parentIds, false);

      query0.findList();
      assertThat(query0.getGeneratedSql()).contains(" from o_order_detail t0 where (t0.order_id) ");
      platformAssertIn(query0.getGeneratedSql(), "where (t0.order_id)");

    } finally {
      Ebean.endTransaction();
    }

    List<OrderDetail> details = Ebean.find(OrderDetail.class)
      .where().eq("order.id", 1)
      .findList();

    for (OrderDetail orderDetail : details) {
      System.out.println(orderDetail);
    }

  }

}

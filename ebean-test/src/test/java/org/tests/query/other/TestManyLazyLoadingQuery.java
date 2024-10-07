package org.tests.query.other;

import io.ebean.Transaction;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestManyLazyLoadingQuery extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    SpiEbeanServer server = (SpiEbeanServer) DB.getDefault();

    BeanDescriptor<Order> descOrder = server.descriptor(Order.class);
    BeanPropertyAssocMany<?> beanProperty = (BeanPropertyAssocMany<?>) descOrder.beanProperty("details");

    List<Object> parentIds = new ArrayList<>();
    parentIds.add(1);


    List<Order> orders =
      DB.find(Order.class)
        .where().lt("id", 4)
        .findList();

    for (Order order : orders) {
      List<OrderDetail> details = order.getDetails();
      System.out.println(details.size());
    }


    // start transaction to keep PC going to lazy query
    try (Transaction txn = DB.beginTransaction()) {
      DB.find(Order.class, 1);

      SpiQuery<?> query0 = (SpiQuery<?>) DB.find(OrderDetail.class);

      query0.setLazyLoadForParents(beanProperty);

      beanProperty.addWhereParentIdIn(query0, parentIds, false);

      query0.findList();
      assertThat(query0.getGeneratedSql()).contains(" from o_order_detail t0 where (t0.order_id) ");
      platformAssertIn(query0.getGeneratedSql(), "where (t0.order_id)");
    }

    List<OrderDetail> details = DB.find(OrderDetail.class)
      .where().eq("order.id", 1)
      .findList();

    for (OrderDetail orderDetail : details) {
      System.out.println(orderDetail);
    }

  }

}

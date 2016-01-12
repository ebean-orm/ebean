package com.avaje.tests.rawsql;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.OrderAggregate;
import com.avaje.tests.model.basic.OrderDetail;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class TestOrderReportTotal extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<OrderAggregate> query = Ebean.createQuery(OrderAggregate.class);

    List<OrderAggregate> list = query.findList();
    assertNotNull(list);

    Query<OrderAggregate> q2 = Ebean.createQuery(OrderAggregate.class);
    q2.where().gt("id", 1);
    q2.having().gt("totalItems", 1);

    List<OrderAggregate> l2 = q2.findList();
    assertNotNull(l2);

  }

  @Test
  public void testOrderDetailCount() {

    ResetBasicData.reset();

    int detailsCount = Ebean.find(OrderDetail.class)
        .where()
          .gt("order.id", 2)
          .istartsWith("order.customer.name","rob")
        .findRowCount();

    assertThat(detailsCount).isGreaterThan(0);
  }
}

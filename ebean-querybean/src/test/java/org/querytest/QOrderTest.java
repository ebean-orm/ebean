package org.querytest;

import io.ebean.FetchGroup;
import org.example.domain.Order;
import org.example.domain.query.QCustomer;
import org.example.domain.query.QOrder;
import org.junit.Test;

public class QOrderTest {

  private static final QCustomer cu = QCustomer.alias();

  private static final QOrder or = QOrder.alias();

  private static final FetchGroup<Order> fg = QOrder.forFetchGroup()
    .select(or.status, or.shipDate)
    .customer.fetchCache(cu.name, cu.status, cu.registered, cu.comments)
    .buildFetchGroup();

  @Test
  public void fetchCache() {


    new QOrder()
      .status.eq(Order.Status.NEW)
      .customer.fetchCache(cu.name, cu.registered)
      .findList();

    new QOrder()
      .status.eq(Order.Status.NEW)
      .customer.fetchCache()
      .findList();
  }

  @Test
  public void viaFetchGraph() {

    new QOrder()
      .status.eq(Order.Status.NEW)
      .select(fg)
      .findList();
  }

}

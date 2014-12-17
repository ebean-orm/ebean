package com.avaje.tests.rawsql;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.OrderDetail;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.List;

public class TestRawSqlMasterDetail extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    String rs = "select t0.id, t0.status, t1.id, t1.name, "+
            " t2.id, t2.order_qty, t3.id, t3.name " +
            "from o_order t0 join o_customer t1 on t1.id = t0.kcustomer_id " +
            "join o_order_detail t2 on t2.order_id = t0.id  " +
            "join o_product t3 on t3.id = t2.product_id  " +
            "where t0.id <= :maxOrderId  and t3.id = :productId "+
            "order by t0.id, t2.id asc";

    RawSql rawSql = RawSqlBuilder.parse(rs)
            .columnMapping("t0.id", "id")
            .columnMapping("t0.status", "status")
            .columnMapping("t1.id", "customer.id")
            .columnMapping("t1.name", "customer.name")
            .columnMapping("t2.id", "details.id")
            .columnMapping("t2.order_qty", "details.orderQty")
            .columnMapping("t3.id", "details.product.id")
            .columnMapping("t3.name", "details.product.name")
            .create();

    List<Order> ordersFromRaw = Ebean.find(Order.class)
            .setRawSql(rawSql)
            .setParameter("maxOrderId", 2)
            .setParameter("productId", 1)
            .findList();

    printOrders(ordersFromRaw, "using RawSql");

  }

  private void printOrders(List<Order> orders, String heading) {
    System.out.println("-------------- "+heading);
    for (Order order : orders) {
      List<OrderDetail> details = order.getDetails();
      System.out.println("order: "+order.getId()+" "+order.getCustomer().getName());
      for (OrderDetail detail : details) {
        System.out.println("detailId:" + detail.getId() + " productId:" + detail.getProduct().getId() + " qty:" +
                detail.getOrderQty());
      }
    }
    System.out.println("-------------- "+heading+" complete");
  }
}


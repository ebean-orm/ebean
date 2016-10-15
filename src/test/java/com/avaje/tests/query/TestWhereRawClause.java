package com.avaje.tests.query;

import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Order;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.tests.model.basic.OrderDetail;
import com.avaje.tests.model.basic.ResetBasicData;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

public class TestWhereRawClause extends BaseTestCase {


  @Test
  public void testRawClauseWithJunction() {

    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class)
        .where()
        .raw("(status = ? or (orderDate < ? and shipDate is null) or customer.name like ?)",
            Order.Status.APPROVED, new Timestamp(System.currentTimeMillis()), "Rob")
        .query();

    query.findList();

    assertThat(query.getGeneratedSql()).contains(" where (t0.status = ? or (t0.order_date < ? and t0.ship_date is null) or t1.name like ?)");
  }

  @Test
  public void testRawClause() {

    ResetBasicData.reset();

    Ebean.find(OrderDetail.class)
        .where()
        .not(Expr.eq("id", 1))
        .raw("orderQty < shipQty")
        .findList();

  }

  @Test
  public void testRawWithBindParams() {

    ResetBasicData.reset();

    Ebean.find(OrderDetail.class)
        .where()
        .ne("id", 42)
        .raw("orderQty < ?", 100)
        .gt("id", 1)
        .raw("unitPrice > ? and product.id > ?", 2, 3)
        .findList();

  }
}

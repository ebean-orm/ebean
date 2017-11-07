package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Expr;
import io.ebean.Query;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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
  @ForPlatform(Platform.POSTGRES)
  public void testRawPostgresArray() {

    ResetBasicData.reset();

    List<String> names = new ArrayList<>();
    names.add("Rob");
    names.add("Fiona");

    List<Customer> list = Ebean.find(Customer.class)
      .where()
      .raw("name = any(?)", names)
      .findList();

    assertThat(list).isNotEmpty();
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

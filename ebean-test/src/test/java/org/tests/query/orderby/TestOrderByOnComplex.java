package org.tests.query.orderby;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestOrderByOnComplex extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class).orderBy().desc("customer");

    query.findList();

    String sql = query.getGeneratedSql();
    assertTrue(sql.contains("order by t0.kcustomer_id desc"));
  }

  @Test
  public void testOrderByCase() {

    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class)
      .orderBy("case when status=3 then 10 when status=2 then 11 else 99 end");

    List<Order> list = query.findList();

    assertThat(sqlOf(query)).contains("select t0.id, t0.status, t0.order_date, t0.ship_date, t1.name, t0.cretime, t0.updtime, t0.kcustomer_id from o_order t0 join o_customer t1 on t1.id = t0.kcustomer_id order by case when t0.status=3 then 10 when t0.status=2 then 11 else 99 end");
    assertThat(list).isNotEmpty();
  }

}

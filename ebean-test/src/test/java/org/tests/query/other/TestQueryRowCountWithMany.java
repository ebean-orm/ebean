package org.tests.query.other;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestQueryRowCountWithMany extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    LoggedSql.start();

    Long productId = 1L;

    Query<Order> query = DB.find(Order.class)
      .fetch("details")
      .where().eq("details.product.id", productId)
      .orderBy("cretime asc").query();

    List<Order> list = query.findList();

    // select distinct t0.id c0, t0.status c1, t0.order_date c2, t0.ship_date c3, t2.name c4, t0.cretime c5, t0.updtime c6, t0.kcustomer_id c7,
    //        t1.id c8, t1.order_qty c9, t1.ship_qty c10, t1.unit_price c11, t1.cretime c12, t1.updtime c13, t1.order_id c14, t1.product_id c15, t0.cretime, t0.id, t1.id, t1.order_qty, t1.cretime
    // from o_order t0
    // join o_customer t2 on t2.id = t0.kcustomer_id
    // left join o_order_detail t1 on t1.order_id = t0.id
    // join o_order_detail u1 on u1.order_id = t0.id
    // where t1.id > 0  and u1.product_id = ?
    // order by t0.cretime, t0.id, t1.id asc, t1.order_qty asc, t1.cretime desc; --bind(1)

    String generatedSql = sqlOf(query, 1);
    if (platformDistinctOn()) {
      assertThat(generatedSql).contains("select distinct on (t0.cretime, t0.id, t1.id, t1.order_qty, t1.cretime) t0.id, t0.status,"); // need the distinct

    } else {
      assertThat(generatedSql).contains("select distinct t0.id, t0.status,"); // need the distinct
    }
    assertThat(generatedSql).contains("left join o_order_detail t1 on t1.order_id = t0.id"); //fetch join
    assertThat(generatedSql).contains("join o_order_detail u1 on u1.order_id = t0.id"); //predicate join
    assertThat(generatedSql).contains(" u1.product_id = ?"); // u1 as predicate alias
    assertThat(generatedSql).contains(" order by t0.cretime");


    int rowCount = query.findCount();

    // select count(*) from o_order t0
    // left join o_order_detail t1 on t1.order_id = t0.id
    // where t1.product_id = ? ; --bind(1)

    // select count(*) from (
    // select distinct t0.id c0 from o_order t0 join o_order_detail u1 on u1.order_id = t0.id where
    // u1.product_id = ?
    // ); --bind(1)

    List<String> sqlLogged = LoggedSql.stop();

    assertEquals(list.size(), rowCount);
    assertEquals(2, sqlLogged.size());
    assertThat(trimSql(sqlLogged.get(1), 1)).contains(
      "select count(*) from ( select distinct t0.id from o_order t0 join o_order_detail u1 on u1.order_id = t0.id and u1.id > 0 where u1.product_id = ?)");

  }


  @Test
  public void testWithFirstRowsMaxRows() {

    ResetBasicData.reset();

    Long productId = 1L;

    Query<Order> query = DB.find(Order.class)
      .fetch("details")
      .where().eq("details.product.id", productId)
      .setFirstRow(2)
      .setMaxRows(20)
      .orderBy("cretime asc")
      .query();

    LoggedSql.start();
    query.findCount();

    List<String> sqlLogged = LoggedSql.stop();

    assertEquals(1, sqlLogged.size());
    assertThat(trimSql(sqlLogged.get(0), 1)).contains("select count(*) from ( select distinct t0.id from o_order t0 join o_order_detail u1 on u1.order_id = t0.id and u1.id > 0 where u1.product_id = ?)");

    query.findList();
  }
}

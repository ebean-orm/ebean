package org.tests.query;

import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestQueryMultiManyOrder extends BaseTestCase {

  @Test
  void test() {
    ResetBasicData.reset();
    LoggedSql.start();

    Query<Order> q = DB.find(Order.class)
      .fetch("shipments")
      .fetch("details")
      .fetch("details.product")
      .fetch("customer")
      .where().gt("id", 0).query();

    List<Order> list = q.findList();
    assertThat(list).isNotEmpty();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("from o_order t0 join o_customer t1 on t1.id = t0.kcustomer_id left join or_order_ship t2 on t2.order_id = t0.id where");
    assertThat(sql.get(1)).contains("from o_order_detail t0 left join o_product t1 on t1.id = t0.product_id");
  }
}

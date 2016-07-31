package com.avaje.tests.query.other;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;
import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryRawExpressionMany extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Integer quantity = 1;

    Query<Order> query = Ebean.find(Order.class)
        .where().raw("details.orderQty = ?", quantity)
        .query();

    LoggedSqlCollector.start();

    query.findCount();
    List<String> sql = LoggedSqlCollector.stop();

    assertThat(sql.get(0)).contains("select count(*) from ( select distinct t0.id c0 from o_order t0 left outer join o_order_detail t1 on t1.order_id = t0.id  where t1.order_qty = ?)");
  }
}

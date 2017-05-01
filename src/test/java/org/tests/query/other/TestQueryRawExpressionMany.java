package org.tests.query.other;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.ebeantest.LoggedSqlCollector;
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

    assertThat(trimSql(sql.get(0), 1)).contains("select count(*) from ( select distinct t0.id from o_order t0 left join o_order_detail t1 on t1.order_id = t0.id  where t1.order_qty = ?)");
  }
}

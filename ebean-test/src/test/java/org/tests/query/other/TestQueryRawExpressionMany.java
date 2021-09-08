package org.tests.query.other;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryRawExpressionMany extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Integer quantity = 1;

    Query<Order> query = DB.find(Order.class)
      .where().raw("details.orderQty = ?", quantity)
      .query();

    LoggedSql.start();

    query.findCount();
    List<String> sql = LoggedSql.stop();

    assertThat(trimSql(sql.get(0), 1)).contains("select count(*) from ( select distinct t0.id from o_order t0 left join o_order_detail t1 on t1.order_id = t0.id and t1.id > 0 where t1.order_qty = ?)");
  }
}

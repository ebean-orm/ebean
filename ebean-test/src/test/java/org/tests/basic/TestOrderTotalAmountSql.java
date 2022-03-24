package org.tests.basic;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.SqlRow;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestOrderTotalAmountSql extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    String sql = "select order_id, sum(order_qty*unit_price) as total_amount from o_order_detail  where order_qty > :minQty  group by order_id";
    List<SqlRow> sqlRows = DB.sqlQuery(sql).setParameter("minQty", 1).findList();

    for (SqlRow sqlRow : sqlRows) {
      Integer id = sqlRow.getInteger("order_id");
      Double amount = sqlRow.getDouble("total_amount");
      assertNotNull(id);
      assertNotNull(amount);
    }
  }

}

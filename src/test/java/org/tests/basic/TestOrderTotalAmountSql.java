package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.SqlRow;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestOrderTotalAmountSql extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    String sql = "select order_id, sum(order_qty*unit_price) as total_amount from o_order_detail  where order_qty > :minQty  group by order_id";
    List<SqlRow> sqlRows = Ebean.createSqlQuery(sql).setParameter("minQty", 1).findList();

    for (SqlRow sqlRow : sqlRows) {
      Integer id = sqlRow.getInteger("order_id");
      Double amount = sqlRow.getDouble("total_amount");
      Assert.assertNotNull("sqlRows: " + sqlRows, id);
      Assert.assertNotNull("sqlRows: " + sqlRows, amount);
    }
  }

}

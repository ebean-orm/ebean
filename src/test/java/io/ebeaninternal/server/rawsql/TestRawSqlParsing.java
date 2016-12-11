package io.ebeaninternal.server.rawsql;

import io.ebean.RawSql;
import io.ebean.RawSql.Sql;
import io.ebean.RawSqlBuilder;
import junit.framework.TestCase;

public class TestRawSqlParsing extends TestCase {

  public void test() {

    String sql
      = " select order_id, sum(order_qty*unit_price) as totalAmount"
      + " from o_order_detail "
      + " group by order_id";

    RawSql rawSql = RawSqlBuilder
      .parse(sql)
      .columnMapping("order_id", "order.id")
      //.columnMapping("sum(order_qty*unit_price)","totalAmount")
      .create();

    Sql rs = rawSql.getSql();

    String s = rs.toString();
    assertTrue(s, s.contains("[order_id, sum"));

  }
}

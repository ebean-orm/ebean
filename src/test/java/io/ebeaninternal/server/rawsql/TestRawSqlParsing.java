package io.ebeaninternal.server.rawsql;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebeaninternal.server.rawsql.SpiRawSql.Sql;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class TestRawSqlParsing extends BaseTestCase {

  @Test
  public void test() {

    String sql
      = " select order_id, sum(order_qty*unit_price) as totalAmount"
      + " from o_order_detail "
      + " group by order_id";

    RawSql rawSql = RawSqlBuilder
      .parse(sql)
      .columnMapping("order_id", "order.id")
      .create();

    Sql rs = ((SpiRawSql)rawSql).getSql();

    String s = rs.toString();
    assertTrue(s, s.contains("[order_id, sum"));
  }

  @Test
  @ForPlatform(Platform.POSTGRES)
  public void testDoubleColon() {

    ResetBasicData.reset();

    String sql = "select id, name from o_customer where name=:name and MD5(id::text) BETWEEN '00000000000000000000000000000000' AND 'ffffffffffffffffffffffffffffffff'";

    RawSql rawSql = RawSqlBuilder
      .parse(sql)
      .create();

    List<Customer> list = Ebean.createQuery(Customer.class)
      .setRawSql(rawSql)
      .setParameter("name", "Rob")
      .findList();

    assertThat(list).isNotEmpty();
  }

  @Test
  public void testWhere() {

    ResetBasicData.reset();

    RawSql sql = RawSqlBuilder.parse("SELECT id, name FROM o_customer ${where}").create();

    List<Customer> customers = Ebean.createQuery(Customer.class)
      .setRawSql(sql)
      .where().gt("id", 1)
      .findList();

    assertThat(customers).isNotEmpty();
  }

  @ForPlatform({Platform.H2, Platform.POSTGRES})
  @Test
  public void testUnion() {

    ResetBasicData.reset();

    String sql =
      "select id, name from (" +
      "  select id, name from o_customer where name <= 'f' " +
      "  union all " +
      "  select id, name from o_customer where name > 'f' " +
      ") all_split";


    RawSql rawSql = RawSqlBuilder.parse(sql).create();

    Query<Customer> query = Ebean.createQuery(Customer.class)
      .setRawSql(rawSql)
      .setFirstRow(1)
      .setMaxRows(5);
      //.orderById(false);

    query.findList();

    assertThat(sqlOf(query)).contains(") all_split limit 5 offset 1");
  }

}

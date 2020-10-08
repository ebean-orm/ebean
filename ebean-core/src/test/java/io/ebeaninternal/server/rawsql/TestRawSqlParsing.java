package io.ebeaninternal.server.rawsql;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.RawSql;
import io.ebean.RawSqlBuilder;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebeaninternal.server.rawsql.SpiRawSql.Sql;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.tests.rawsql.A2Customer;
import org.tests.rawsql.ACustomer;

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

    List<Customer> list = DB.find(Customer.class)
      .setRawSql(rawSql)
      .setParameter("name", "Rob")
      .findList();

    assertThat(list).isNotEmpty();
  }

  @Test
  public void testWhere() {

    ResetBasicData.reset();

    RawSql sql = RawSqlBuilder.parse("SELECT id, name FROM o_customer ${where}").create();

    List<Customer> customers = DB.find(Customer.class)
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

    Query<Customer> query = DB.find(Customer.class)
      .setRawSql(rawSql)
      .setFirstRow(1)
      .setMaxRows(5);
      //.orderById(false);

    query.findList();

    assertThat(sqlOf(query)).contains(") all_split limit 5 offset 1");
  }


  @ForPlatform({Platform.H2, Platform.POSTGRES})
  @Test
  public void testUnion_explicitWhere() {
    ResetBasicData.reset();

    String sql =
      "select id, name from (" +
        "  select id, name from o_customer where name <= 'f' " +
        "  union all " +
        "  select id, name from o_customer where name > 'f' " +
        ") all_split ${where}";

    RawSql rawSql = RawSqlBuilder.parse(sql).create();
    Query<Customer> query = DB.find(Customer.class)
      .setRawSql(rawSql)
      .setFirstRow(1)
      .setMaxRows(5);
    query.findList();

    assertThat(sqlOf(query)).contains(") all_split limit 5 offset 1");

    Query<Customer> query1 = DB.find(Customer.class)
      .setRawSql(rawSql)
      .where().gt("id", 2)
      .setFirstRow(1)
      .setMaxRows(5)
      .query();
    query1.findList();

    assertThat(sqlOf(query1)).contains("all_split  where id > ? limit 5 offset 1");
  }

  @ForPlatform({Platform.H2, Platform.POSTGRES})
  @Test
  public void testUnion_explicitAndhere() {
    ResetBasicData.reset();

    String sql =
      "select id, name from (" +
        "  select id, name from o_customer where name <= 'f' " +
        "  union all " +
        "  select id, name from o_customer where name > 'f' " +
        ") all_split where 1=1 ${andWhere}";

    RawSql rawSql = RawSqlBuilder.parse(sql).create();
    Query<Customer> query = DB.find(Customer.class)
      .setRawSql(rawSql)
      .setFirstRow(1)
      .setMaxRows(5);
    query.findList();

    assertThat(sqlOf(query)).contains(") all_split where 1=1 limit 5 offset 1");

    Query<Customer> query1 = DB.find(Customer.class)
      .setRawSql(rawSql)
      .where().gt("id", 2)
      .setFirstRow(1)
      .setMaxRows(5)
      .query();
    query1.findList();

    assertThat(sqlOf(query1)).contains("all_split where 1=1  and id > ? limit 5 offset 1");
  }

  @ForPlatform({Platform.H2, Platform.POSTGRES})
  @Test
  public void testColumnName2() {

    ResetBasicData.reset();

    String sql = "select 42 custId, 'bar' customerName from o_customer";

    RawSql rawSql = RawSqlBuilder.parse(sql).create();

    Query<A2Customer> query = DB.find(A2Customer.class)
      .setRawSql(rawSql);

    final List<A2Customer> list = query.findList();

    assertThat(list).isNotEmpty();
    for (A2Customer customer : list) {
      assertThat(customer.getCustId()).isEqualTo(42L);
      assertThat(customer.getCustomerName()).isEqualTo("bar");
    }
  }

  @ForPlatform({Platform.H2, Platform.POSTGRES})
  @Test
  public void testColumnAlias() {

    ResetBasicData.reset();

    String sql = "select 43 custId, name as custName from o_customer";

    RawSql rawSql = RawSqlBuilder.parse(sql).create();

    Query<ACustomer> query = DB.find(ACustomer.class)
      .setRawSql(rawSql);

    final List<ACustomer> list = query.findList();

    assertThat(list).isNotEmpty();
    for (ACustomer customer : list) {
      assertThat(customer.getCustId()).isEqualTo(43L);
      assertThat(customer.getCustName()).isNotNull();
    }
  }

  @ForPlatform({Platform.H2, Platform.POSTGRES})
  @Test
  public void testColumnNameMapping() {

    ResetBasicData.reset();

    String sql = "select v.custId, v.customerName from (select id custId, name customerName from o_customer) v";

    RawSql rawSql = RawSqlBuilder.parse(sql)
      .columnMapping("v.customerName", "custName")
      .create();

    Query<ACustomer> query = DB.find(ACustomer.class)
      .setRawSql(rawSql);

    final List<ACustomer> list = query.findList();

    assertThat(list).isNotEmpty();
    for (ACustomer customer : list) {
      assertThat(customer.getCustId()).isGreaterThan(0L);
      assertThat(customer.getCustName()).isNotNull();
    }
  }
}

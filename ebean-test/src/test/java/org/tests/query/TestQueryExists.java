package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryExists extends BaseTestCase {

  @Test
  public void testExistsBoolean_basic() {

    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class)
      .where().gt("id", 1)
      .query();

    LoggedSql.start();
    boolean check = query.exists();
    String sql = LoggedSql.stop().get(0);
    assertThat(check).isTrue();

    if (isH2() || isPostgresCompatible()) {
      assertThat(sql).contains("select t0.id from o_order t0 where t0.id > ? limit 1");
    }

    assertThat(DB.find(Order.class).where().gt("id", 1).exists()).isTrue();
    assertThat(DB.find(Order.class).where().or().gt("id", 1).isNull("shipDate").exists()).isTrue();
  }

  @Test
  public void testExists_orders_onOneToMany() {

    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class)
      .where().raw("exists (select 1 from o_order_detail where order_id = t0.id)")
      .query();

    List<Order> ordersThatHave = query.findList();

    Query<Order> query2 = DB.find(Order.class)
      .where().raw("not exists (select 1 from o_order_detail where order_id = t0.id)")
      .query();

    List<Order> ordersThatDontHave = query2.findList();

    assertSql(query).contains(" exists (");
    assertThat(query2.getGeneratedSql()).contains(" not exists (");

    assertThat(ordersThatHave).isNotEmpty();
    assertThat(ordersThatDontHave).isNotEmpty();
  }

  @Test
  public void testExists_onOneToMany() {

    ResetBasicData.reset();

    Query<Customer> query = DB.find(Customer.class)
      .where().raw("exists (select 1 from contact where customer_id = t0.id)")
      .query();

    List<Customer> customersWithContacts = query.findList();

    Query<Customer> query2 = DB.find(Customer.class)
      .where().raw("not exists (select 1 FROM contact where customer_id = t0.id)")
      .query();

    query2.findList();

    assertSql(query).contains(" exists (");
    assertThat(query2.getGeneratedSql()).contains(" not exists (");

    assertThat(customersWithContacts).isNotEmpty();
  }

  @Test
  public void testExists() {
    ResetBasicData.reset();

    Query<Order> subQuery = DB.find(Order.class).alias("sq").select("id").where().raw("sq.kcustomer_id = qt.id").query();

    Query<Customer> query = DB.find(Customer.class).alias("qt").where().exists(subQuery).query();

    query.findList();
    String sql = query.getGeneratedSql();

    assertThat(sql).contains("exists (select 1 from");
  }

  @Test
  public void testNotExists() {
    ResetBasicData.reset();

    Query<Order> subQuery = DB.find(Order.class).alias("sq").where().raw("sq.kcustomer_id = qt.id").query();
    Query<Customer> query = DB.find(Customer.class).alias("qt").where().notExists(subQuery).query();

    query.findList();
    String sql = query.getGeneratedSql();

    assertThat(sql).contains("not exists (select 1 from");
  }

  @Test
  public void testExistsNoWhere() {
    ResetBasicData.reset();

    Query<Order> subQuery = DB.find(Order.class).alias("sq").select("id");
    Query<Customer> query = DB.find(Customer.class).alias("qt").where().notExists(subQuery).query();

    query.findList();
    String sql = query.getGeneratedSql();

    assertThat(sql).contains("not exists (select 1 from o_order sq)");
  }
}

package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryExists extends BaseTestCase {

  @Test
  public void testExistsBoolean_basic() {

    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class)
      .where().gt("id", 1)
      .query();

    boolean check = query.exists();
    assertThat(check).isTrue();

    String sql = sqlOf(query);
    if (isH2() || isPostgres()) {
      assertThat(sql).contains("select t0.id from o_order t0 where t0.id > ? limit 1");
    }

    assertThat(Ebean.find(Order.class).where().gt("id", 1).exists()).isTrue();
    assertThat(Ebean.find(Order.class).where().or().gt("id", 1).isNull("shipDate").exists()).isTrue();
  }

  @Test
  public void testExists_orders_onOneToMany() {

    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class)
      .where().raw("exists (select 1 from o_order_detail where order_id = t0.id)")
      .query();

    List<Order> ordersThatHave = query.findList();

    Query<Order> query2 = Ebean.find(Order.class)
      .where().raw("not exists (select 1 from o_order_detail where order_id = t0.id)")
      .query();

    List<Order> ordersThatDontHave = query2.findList();

    assertThat(query.getGeneratedSql()).contains(" exists (");
    assertThat(query2.getGeneratedSql()).contains(" not exists (");

    assertThat(ordersThatHave).isNotEmpty();
    assertThat(ordersThatDontHave).isNotEmpty();
  }

  @Test
  public void testExists_onOneToMany() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
      .where().raw("exists (select 1 from contact where customer_id = t0.id)")
      .query();

    List<Customer> customersWithContacts = query.findList();

    Query<Customer> query2 = Ebean.find(Customer.class)
      .where().raw("not exists (select 1 FROM contact where customer_id = t0.id)")
      .query();

    query2.findList();

    assertThat(query.getGeneratedSql()).contains(" exists (");
    assertThat(query2.getGeneratedSql()).contains(" not exists (");

    assertThat(customersWithContacts).isNotEmpty();
  }

  @Test
  public void testExists() {
    ResetBasicData.reset();

    Query<Order> subQuery = Ebean.find(Order.class).alias("sq").select("id").where().raw("sq.kcustomer_id = qt.id").query();

    Query<Customer> query = Ebean.find(Customer.class).alias("qt").where().exists(subQuery).query();

    query.findList();
    String sql = query.getGeneratedSql();

    assertThat(sql).contains("exists (");
  }

  @Test
  public void testNotExists() {
    ResetBasicData.reset();

    Query<Order> subQuery = Ebean.find(Order.class).alias("sq").select("id").where().raw("sq.kcustomer_id = qt.id").query();
    Query<Customer> query = Ebean.find(Customer.class).alias("qt").where().notExists(subQuery).query();

    query.findList();
    String sql = query.getGeneratedSql();

    assertThat(sql).contains("not exists (");
  }
}

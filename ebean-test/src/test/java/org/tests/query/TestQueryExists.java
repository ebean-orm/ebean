package org.tests.query;

import io.ebean.xtest.BaseTestCase;
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
      assertThat(sql).contains("select exists(select 1 from o_order t0 where t0.id > ?)");
    }

    assertThat(DB.find(Order.class).where().gt("id", 1).exists()).isTrue();
    assertThat(DB.find(Order.class).where().or().gt("id", 1).isNull("shipDate").exists()).isTrue();
  }

  @Test
  public void testExistsBoolean_returnsFalse() {
    ResetBasicData.reset();

    // no order will ever have id = -1
    assertThat(DB.find(Order.class).where().eq("id", -1).exists()).isFalse();
  }

  @Test
  public void testExistsBoolean_withJoin() {
    ResetBasicData.reset();

    // customers that have at least one contact — exercises the join path in buildExistsQuery
    Query<Customer> query = DB.find(Customer.class)
      .where().isNotNull("contacts.firstName")
      .query();

    LoggedSql.start();
    boolean result = query.exists();
    String sql = LoggedSql.stop().get(0);

    assertThat(result).isTrue();
    if (isH2() || isPostgresCompatible()) {
      assertThat(sql).contains("select exists(select 1");
    }

    // no customer has a contact with this name — should return false
    assertThat(DB.find(Customer.class).where().eq("contacts.firstName", "NoSuchPerson_xyz").exists()).isFalse();
  }

  @Test
  public void testExistsBoolean_queryPlanReuse() {
    ResetBasicData.reset();

    // first call builds the plan; second call should reuse it (no extra SQL generation)
    LoggedSql.start();
    boolean first = DB.find(Order.class).where().gt("id", 1).exists();
    boolean second = DB.find(Order.class).where().gt("id", 1).exists();
    List<String> sqls = LoggedSql.stop();

    assertThat(first).isTrue();
    assertThat(second).isTrue();
    assertThat(sqls).hasSize(2);
    // strip the --micros(...) timing suffix
    String sql0 = sqls.get(0).split(";")[0];
    String sql1 = sqls.get(1).split(";")[0];
    assertThat(sql0).isEqualTo(sql1);
    if (isH2() || isPostgresCompatible()) {
      assertThat(sql0).isEqualTo("select exists(select 1 from o_order t0 where t0.id > ?)");
    }
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

package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Expr;
import io.ebean.Query;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class TestWhereRawClause extends BaseTestCase {


  @Test
  public void testRawClauseWithJunction() {

    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class)
      .where()
      .raw("(status = ? or (orderDate < ? and shipDate is null) or customer.name like ?)",
        Order.Status.APPROVED, new Timestamp(System.currentTimeMillis()), "Rob")
      .query();

    query.findList();

    assertThat(query.getGeneratedSql()).contains(" where (t0.status = ? or (t0.order_date < ? and t0.ship_date is null) or t1.name like ?)");
  }

  @Test
  public void testRawClause() {

    ResetBasicData.reset();

    Ebean.find(OrderDetail.class)
      .where()
      .not(Expr.eq("id", 1))
      .raw("orderQty < shipQty")
      .findList();

  }

  @Test
  public void testRaw_bindExpansion_subquery() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
      .where()
      .raw("id in (select c.id from o_customer c where c.name in (?1))", asList("Rob", "Fiona", "Jack"))
      .query();

    List<Customer> list = query.findList();
    assertThat(list).isNotEmpty();
    assertThat(sqlOf(query)).contains(" t0.id in (select c.id from o_customer c where c.name in (?,?,?))");
  }

  @Test
  public void testRawOrEmpty_when_notEmpty() {

    ResetBasicData.reset();

    List<String> vals = asList("Rob", "Fiona", "Jack");

    Query<Customer> query = Ebean.find(Customer.class)
      .select("name")
      .where()
      .rawOrEmpty("id in (select c.id from o_customer c where c.name in (?1))", vals)
      .query();

    List<Customer> list = query.findList();
    assertThat(list).isNotEmpty();
    assertThat(sqlOf(query)).contains(" t0.id in (select c.id from o_customer c where c.name in (?,?,?))");
  }

  @Test
  public void testRawOrEmpty_when_null() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
      .select("name")
      .where()
      .rawOrEmpty("id in (select c.id from o_customer c where c.name in (?1))", null)
      .query();

    List<Customer> list = query.findList();
    assertThat(list).isNotEmpty();
    assertThat(sqlOf(query)).isEqualTo("select t0.id, t0.name from o_customer t0");
  }

  @Test
  public void testRawOrEmpty_when_empty() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
      .select("name")
      .where()
      .rawOrEmpty("id in (select c.id from o_customer c where c.name in (?1))", Collections.emptySet())
      .query();

    List<Customer> list = query.findList();
    assertThat(list).isNotEmpty();
    assertThat(sqlOf(query)).isEqualTo("select t0.id, t0.name from o_customer t0");
  }

  @Test
  public void testRaw_bindExpansion() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
      .where()
      .raw("name in (?1)", asList("Rob", "Fiona", "Jack"))
      .query();

    List<Customer> list = query.findList();
    assertThat(list).isNotEmpty();
    assertThat(sqlOf(query)).contains(" t0.name in (?,?,?)");
  }

  @Test
  @ForPlatform(Platform.POSTGRES)
  public void testRawPostgresArray() {

    ResetBasicData.reset();

    List<Customer> list = Ebean.find(Customer.class)
      .where()
      .raw("name = any(?)", asList("Rob", "Fiona", "Jack"))
      .findList();

    assertThat(list).isNotEmpty();
  }

  @Test
  @ForPlatform(Platform.POSTGRES)
  public void testRawOrEmpty_PostgresArray() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();

    Ebean.find(Customer.class)
      .select("name").where().rawOrEmpty("name = any(?)", asList("Rob", "Fiona", "Jack"))
      .findList();

    Ebean.find(Customer.class)
      .select("name").where().rawOrEmpty("name = any(?)", asList())
      .findList();

    Ebean.find(Customer.class)
      .select("name").where().rawOrEmpty("name = any(?)", null)
      .findList();

    List<String> sql = LoggedSqlCollector.stop();

    assertThat(sql.get(0)).isEqualTo("select t0.id, t0.name from o_customer t0 where t0.name = any(?); --bind(Array[3]={Rob,Fiona,Jack})");
    assertThat(sql.get(1)).isEqualTo("select t0.id, t0.name from o_customer t0; --bind()");
    assertThat(sql.get(2)).isEqualTo("select t0.id, t0.name from o_customer t0; --bind()");
  }

  @Test
  public void testRawWithBindParams() {

    ResetBasicData.reset();

    Ebean.find(OrderDetail.class)
      .where()
      .ne("id", 42)
      .raw("orderQty < ?", 100)
      .gt("id", 1)
      .raw("unitPrice > ? and product.id > ?", 2, 3)
      .findList();

  }
}

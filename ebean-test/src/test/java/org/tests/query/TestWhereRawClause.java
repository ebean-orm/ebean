package org.tests.query;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Expr;
import io.ebean.Query;
import io.ebean.xtest.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestWhereRawClause extends BaseTestCase {


  @Test
  public void testRawClauseWithJunction() {

    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class)
      .where()
      .raw("(status = ? or (orderDate < ? and shipDate is null) or customer.name like ?)",
        Order.Status.APPROVED, new Timestamp(System.currentTimeMillis()), "Rob")
      .query();

    query.findList();

    assertSql(query).contains(" where (t0.status = ? or (t0.order_date < ? and t0.ship_date is null) or t1.name like ?)");
  }

  @Test
  public void testRawClause() {

    ResetBasicData.reset();

    DB.find(OrderDetail.class)
      .where()
      .not(Expr.eq("id", 1))
      .raw("orderQty < shipQty")
      .findList();

  }

  @Test
  public void testRaw_bindExpansion_subquery() {

    ResetBasicData.reset();

    Query<Customer> query = DB.find(Customer.class)
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

    Query<Customer> query = DB.find(Customer.class)
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

    Query<Customer> query = DB.find(Customer.class)
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

    Query<Customer> query = DB.find(Customer.class)
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

    Query<Customer> query = DB.find(Customer.class)
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

    List<Customer> list = DB.find(Customer.class)
      .where()
      .raw("name = any(?)", asList("Rob", "Fiona", "Jack"))
      .findList();

    assertThat(list).isNotEmpty();
  }

  @Test
  @ForPlatform(Platform.POSTGRES)
  public void testRawOrEmpty_PostgresArray() {

    ResetBasicData.reset();

    LoggedSql.start();

    DB.find(Customer.class)
      .select("name").where().rawOrEmpty("name = any(?)", asList("Rob", "Fiona", "Jack"))
      .findList();

    DB.find(Customer.class)
      .select("name").where().rawOrEmpty("name = any(?)", asList())
      .findList();

    DB.find(Customer.class)
      .select("name").where().rawOrEmpty("name = any(?)", null)
      .findList();

    List<String> sql = LoggedSql.stop();

    assertThat(sql.get(0)).contains("select t0.id, t0.name from o_customer t0 where t0.name = any(?);");
    assertThat(sql.get(1)).contains("select t0.id, t0.name from o_customer t0; --bind()");
    assertThat(sql.get(2)).contains("select t0.id, t0.name from o_customer t0; --bind()");
  }

  @Test
  public void testRawWithBindParams() {

    ResetBasicData.reset();

    DB.find(OrderDetail.class)
      .where()
      .ne("id", 42)
      .raw("orderQty < ?", 100)
      .gt("id", 1)
      .raw("unitPrice > ? and product.id > ?", 2, 3)
      .findList();

  }

  @Test
  public void testRawClauseSubQuery() {
    LoggedSql.start();

    List<Customer> list;
    List<String> sql;

    DB.find(Customer.class)
      .select("name")
      .where()
      .raw("id in (select c.id as test_id from o_customer c)")
      .findList();

    sql = LoggedSql.collect();
    System.out.println(sql);
    assertThat(sql.get(0)).contains("select t0.id, t0.name from o_customer t0 where t0.id in (select c.id as test_id from o_customer c);");


    /*
    this generate wrong query and throw SQLException. This is bug not only for column id, but others too (when there is conflict with name from upper level table)

    Syntax error in SQL statement "select t0.id, t0.name from o_customer t0 where t0.id in (select c.id as t0[*].id from o_customer c)"; expected ",, FROM, WHERE, GROUP BY, HAVING, WINDOW, QUALIFY, INTERSECT, UNION, EXCEPT, MINUS, ORDER BY, OFFSET, FETCH, LIMIT, FOR, ,, )";
    SQL statement:
    select t0.id, t0.name from o_customer t0 where t0.id in (select c.id as t0.id from o_customer c) [42001-214] Bind values:[null] Query was:select t0.id, t0.name from o_customer t0 where t0.id in (select c.id as t0.id from o_customer c)
    */
    DB.find(Customer.class)
      .select("name")
      .where()
      .raw("id in (select c.id as id from o_customer c)")
      .findList();

    sql = LoggedSql.collect();
    System.out.println(sql);
    assertThat(sql.get(0)).contains("select t0.id, t0.name from o_customer t0 where t0.id in (select c.id as id from o_customer c);");


    /*
    example of possible workaround
    subqueries are created by users, so we cannot use only one possible way to keep it working
     */

    DB.find(Customer.class)
      .select("name")
      .where()
      .raw("id in (select \"id\" from (select c.id as \"id\", c.id || ' ' || c.name as \"name\" from o_customer c) w(\"id\", \"name\") )")
      .findList();

    sql = LoggedSql.stop();
    System.out.println(sql);
    assertThat(sql.get(0)).contains("select t0.id, t0.name from o_customer t0 where t0.id in (select \"id\" from (select c.id as \"id\", c.id || ' ' || c.name as \"name\" from o_customer c) w(\"id\", \"name\") );");
  }
}

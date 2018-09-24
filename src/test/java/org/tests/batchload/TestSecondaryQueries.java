package org.tests.batchload;

import io.ebean.Ebean;
import io.ebean.FetchConfig;
import io.ebean.Query;
import io.ebean.QueryIterator;
import io.ebean.TransactionalTestCase;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestSecondaryQueries extends TransactionalTestCase {

  @Test
  public void fetchQuery() {

    LoggedSqlCollector.start();

    Ebean.find(Order.class)
      .select("status")
      .fetchQuery("customer", "name")
      .findList();

    List<String> sql = LoggedSqlCollector.stop();

    assertThat(sql).hasSize(2);
    assertThat(trimSql(sql.get(0), 2)).contains("select t0.id, t0.status, t0.kcustomer_id from o_order t0");
    assertThat(trimSql(sql.get(1), 2)).contains("select t0.id, t0.name from o_customer t0 where t0.id");
    platformAssertIn(sql.get(1), " where t0.id");
  }

  @Test
  public void fetchLazy() {

    LoggedSqlCollector.start();

    List<Order> orders = Ebean.find(Order.class)
      .select("status")
      .fetchLazy("customer", "name")
      .setMaxRows(10)
      .setUseCache(false)
      .findList();

    List<String> sql = LoggedSqlCollector.stop();

    assertThat(sql).hasSize(1);
    if (isSqlServer()) {
      assertThat(trimSql(sql.get(0), 2)).contains("select top 10 t0.id, t0.status, t0.kcustomer_id from o_order t0 order by t0.id");
    } else {
      assertThat(trimSql(sql.get(0), 2)).contains("select t0.id, t0.status, t0.kcustomer_id from o_order t0");
    }

    LoggedSqlCollector.start();

    // invoke lazy loading
    for (Order order : orders) {
      order.getCustomer().getName();
    }

    sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    assertThat(trimSql(sql.get(0), 1)).contains("select t0.id, t0.name from o_customer t0 where t0.id");
    platformAssertIn(sql.get(0), " where t0.id");
  }

  @Test
  public void fetchIterate() {

    LoggedSqlCollector.start();

    try (QueryIterator<Order> orders =
           Ebean.find(Order.class).select("status")
        .setMaxRows(10)
        .setUseCache(false)
        .findIterate()) {

      while (orders.hasNext()) {
        orders.next(); // dummy read
      }
    }
    List<String> sql = LoggedSqlCollector.stop();

    assertThat(sql).hasSize(1);
    if (isSqlServer()) {
      assertThat(trimSql(sql.get(0), 2)).contains("select top 10 t0.id, t0.status from o_order t0 order by t0.id");
    } else {
      assertThat(trimSql(sql.get(0), 2)).contains("select t0.id, t0.status from o_order t0");
    }

  }
  @Test
  public void testSecQueryOneToMany() {

    Order testOrder = ResetBasicData.createOrderCustAndOrder("testSecQry10");
    Integer custId = testOrder.getCustomer().getId();

    Query<Customer> query = Ebean.find(Customer.class)
      .select("name")
      .fetch("contacts", "+query")
      .setId(custId);

    LoggedSqlCollector.start();

    Customer cust = query.findOne();
    assertNotNull(cust);

    List<String> sql = LoggedSqlCollector.stop();

    String generatedSql = query.getGeneratedSql();
    assertThat(generatedSql).contains("from o_customer t0 where t0.id = ?");

    assertEquals(2, sql.size());
    assertThat(sql.get(1)).contains("from contact t0 where (t0.customer_id) ");
    platformAssertIn(sql.get(1), " where (t0.customer_id)");
  }


  @Test
  public void testManyToOneWithManyPlusOneToMany() {

    Query<Order> query = Ebean.find(Order.class)
      .select("status")
      .fetch("customer", "name, status", new FetchConfig().query())
      .fetch("customer.contacts")
      .fetch("details", new FetchConfig().query())
      .where().eq("status", Order.Status.NEW)
      .query();


    LoggedSqlCollector.start();

    List<Order> list = query.findList();
    assertTrue(!list.isEmpty());

    for (Order order : list) {
      order.getCustomer().getStatus();
    }

    List<String> sql = LoggedSqlCollector.stop();

    String generatedSql = sqlOf(query, 2);
    //select t0.id c0, t0.status c1, t0.kcustomer_id c2 from o_order t0 where t0.status = ? ; --bind(NEW)
    assertEquals("select t0.id, t0.status, t0.kcustomer_id from o_order t0 where t0.status = ? ", generatedSql);


    //List<SpiQuery<?>> secondaryQueries = spiQuery.getLoggedSecondaryQueries();
    assertEquals(3, sql.size());

    //SpiQuery<?> custSecondaryQuery =;
    String custSecondarySql =  sql.get(1);

    // select t0.id c0, t0.name c1, t0.status c2,
    //        t1.id c3, t1.first_name c4, t1.last_name c5, t1.phone c6, t1.mobile c7, t1.email c8, t1.cretime c9, t1.updtime c10, t1.customer_id c11, t1.group_id c12
    // from o_customer t0
    // left  join contact t1 on t1.customer_id = t0.id
    // where t0.id = ?   order by t0.id; --bind(1)

    assertThat(custSecondarySql).contains("from o_customer t0 ");
    assertThat(custSecondarySql).contains("left join contact t1 on t1.customer_id = t0.id ");
    assertThat(custSecondarySql).contains("where t0.id ");

    String ordSecondarySql =  sql.get(2);

    // select ...
    // from o_order_detail t0
    // where (t0.order_id) in (?,?,?,?,?) ; --bind(1,4,1,1,1)

    assertThat(ordSecondarySql).contains(" from o_order_detail t0 where t0.id > 0 and (t0.order_id) ");
    platformAssertIn(ordSecondarySql, "and (t0.order_id)");
  }

}

package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.ebeantest.LoggedSqlCollector;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.OrderShipment;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestQueryFetchManyTwoDeep extends BaseTestCase {

  @Test
  public void testFetchOneToManyWithChildOneToMany() {

    ResetBasicData.reset();

    // test that join to order.details is not included in the initial query (included in query join)
    Query<Customer> query = Ebean.find(Customer.class)
      .setAutoTune(false)
      .fetch("orders")
      .fetch("orders.details");


    LoggedSqlCollector.start();

    List<Customer> list = query.findList();

    List<String> sql = LoggedSqlCollector.stop();

    assertTrue("has rows", !list.isEmpty());
    String mainSql = sqlOf(query);
    assertThat(mainSql).contains("from o_customer t0 ");
    assertThat(mainSql).contains("left join o_order t1 on t1.kcustomer_id = t0.id");
    assertThat(mainSql).contains("left join o_customer t2 on t2.id = t1.kcustomer_id");
    assertThat(mainSql).doesNotContain("join or_order_ship");

    //select t0.id c0, t0.status c1, t0.name c2, t0.smallnote c3, t0.anniversary c4, t0.cretime c5, t0.updtime c6, t0.billing_address_id c7, t0.shipping_address_id c8, t1.id c9, t1.status c10, t1.order_date c11, t1.ship_date c12,
    //       t2.name c13, t1.cretime c14, t1.updtime c15, t1.kcustomer_id c16
    // from o_customer t0
    // left join o_order t1 on t1.kcustomer_id = t0.id
    // left join o_customer t2 on t2.id = t1.kcustomer_id
    // where t1.order_date is not null  order by t0.id; --bind()


    //List<SpiQuery<?>> secondaryQueries = spiQuery.getLoggedSecondaryQueries();
    //Assert.assertNotNull(secondaryQueries);
    assertEquals(2, sql.size());

    //SpiQuery<?> secondaryQuery = secondaryQueries.get(0);
    String secondarySql = sql.get(1);
    assertThat(secondarySql).contains("from o_order_detail t0 where t0.id > 0 and (t0.order_id) ");
    platformAssertIn(secondarySql, "(t0.order_id)");

    // select t0.order_id c0, t0.id c1, t0.order_qty c2, t0.ship_qty c3, t0.unit_price c4, t0.cretime c5, t0.updtime c6, t0.order_id c7, t0.product_id c8
    // from o_order_detail t0
    // where (t0.order_id) in (?,?,?,?,?)

  }

  @Test
  public void testFetchOptionalManyToOneThenDownToMany() {

    ResetBasicData.reset();

    // test that join to order.details is not included
    Query<OrderShipment> shipQuery = Ebean.find(OrderShipment.class)
      .setAutoTune(false)
      .fetch("order")
      .fetch("order.details");

    List<OrderShipment> shipList = shipQuery.findList();
    assertTrue("has rows", !shipList.isEmpty());

    String generatedSql = shipQuery.getGeneratedSql();

    // select ...
    // from or_order_ship t0
    // left join o_order t1 on t1.id = t0.order_id
    // left join o_customer t3 on t3.id = t1.kcustomer_id
    // left join o_order_detail t2 on t2.order_id = t1.id
    // where t2.id > 0 ; --bind()

    assertTrue(generatedSql.contains("from or_order_ship t0"));
    // Relationship from OrderShipment to Order is optional so outer join here
    assertTrue(generatedSql.contains("left join o_order t1 on t1.id = t0.order_id"));
    assertTrue(generatedSql.contains("left join o_customer t3 on t3.id = t1.kcustomer_id"));
    assertTrue(generatedSql.contains("left join o_order_detail t2 on t2.order_id = t1.id"));


    // If OrderShipment to Order is not optional you get inner joins up to o_order_detail (which is a many)

    // select ...
    // from or_order_ship t0
    // join o_order t1 on t1.id = t0.order_id
    // join o_customer t3 on t3.id = t1.kcustomer_id
    // left join o_order_detail t2 on t2.order_id = t1.id
    // where t2.id > 0 ; --bind()
  }


  @Test
  public void testFetchMandatoryManyToOneThenDownToMany() {

    ResetBasicData.reset();

    // test that join to order.details is not included
    Query<Contact> query = Ebean.find(Contact.class)
      .setAutoTune(false)
      .fetch("customer")
      .fetch("customer.orders");

    List<Contact> shipList = query.findList();
    assertTrue("has rows", !shipList.isEmpty());

    String generatedSql = query.getGeneratedSql();

    // select ...
    // from contact t0
    // join o_customer t1 on t1.id = t0.customer_id
    // left join o_order t2 on t2.kcustomer_id = t1.id
    // left join o_customer t3 on t3.id = t2.kcustomer_id
    // where t2.order_date is not null ; --bind()

    assertThat(generatedSql).contains("from contact t0 ");
    // Relationship from Contact to Customer is mandatory so inner join here
    assertThat(generatedSql).contains("join o_customer t1 on t1.id = t0.customer_id");
    // outer join on many relationship 'orders'
    assertThat(generatedSql).contains("left join o_order t2 on t2.kcustomer_id = t1.id");

  }

  @Test
  public void testFetchMandatoryManyToOneWithPredicate() {

    ResetBasicData.reset();

    // test that join to order.details is not included
    Query<Contact> query = Ebean.find(Contact.class)
      .setAutoTune(false)
      .fetch("customer")
      .where().ilike("customer.name", "Rob%")
      .query();

    List<Contact> list = query.findList();
    assertTrue("has rows", !list.isEmpty());

    String generatedSql = query.getGeneratedSql();

    // select ...
    // from contact t0
    // join o_customer t1 on t1.id = t0.customer_id
    // where lower(t1.name) like ? ; --bind(rob%)

    assertThat(generatedSql).contains("from contact t0 ");
    assertThat(generatedSql).contains("join o_customer t1 on t1.id = t0.customer_id");
    assertThat(generatedSql).contains("where lower(t1.name) like ");

  }

}

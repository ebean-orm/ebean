package org.tests.query.joins;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;
import org.junit.Assert;
import org.junit.Test;

public class TestQueryManyToOneWhereClauseJoin extends BaseTestCase {


  /**
   * Testing 'where join' created for a ManyToOne relationship.
   */
  @Test
  public void testJoin() {

    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class)
      .where().ilike("customer.name", "rob%")
      .query();

    query.findList();

    //select t0.id c0, t0.status c1, t0.order_date c2, t0.ship_date c3, t1.name c4, t0.cretime c5, t0.updtime c6, t0.kcustomer_id c7
    String expectedSql = "from o_order t0 join o_customer t1 on t1.id = t0.kcustomer_id  where lower(t1.name) like ";
    Assert.assertTrue(query.getGeneratedSql().contains(expectedSql));

    // select t0.id c0, t0.status c1, t0.order_date c2, t0.ship_date c3, t1.name c4, t0.cretime c5, t0.updtime c6, t0.kcustomer_id c7
    // from o_order t0
    // join o_customer t1 on t1.id = t0.kcustomer_id
    // where lower(t1.name) like ? ; --bind(rob%)
  }

  /**
   * Although this is a disjunction it is on a ManyToOne so the 'default' join type is still fine.
   */
  @Test
  public void testDisjunctionOnManyToOneJoin() {

    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class)
      .where().disjunction().ilike("customer.name", "rob%").gt("id", 1).endJunction()
      .query();

    query.findList();

    //select t0.id c0, t0.status c1, t0.order_date c2, t0.ship_date c3, t1.name c4, t0.cretime c5, t0.updtime c6, t0.kcustomer_id c7
    String expectedSql = "from o_order t0 join o_customer t1 on t1.id = t0.kcustomer_id  where (lower(t1.name) like ";
    Assert.assertTrue(query.getGeneratedSql().contains(expectedSql));

    // select t0.id c0, t0.status c1, t0.order_date c2, t0.ship_date c3, t1.name c4, t0.cretime c5, t0.updtime c6, t0.kcustomer_id c7
    // from o_order t0
    // join o_customer t1 on t1.id = t0.kcustomer_id
    // where (lower(t1.name) like ?  or t0.id > ? ) ; --bind(rob%,1)

  }


  /**
   * Testing ManyToOne relationship with predicate and fetch.
   */
  @Test
  public void testWhereAndFetch() {

    ResetBasicData.reset();

    Query<Order> query = Ebean.find(Order.class)
      .fetch("customer")
      .fetch("customer.contacts")
      .where().ilike("customer.name", "rob%")
      .query();

    query.findList();

    String generatedSql = query.getGeneratedSql();
    Assert.assertTrue(generatedSql.contains("from o_order t0 join o_customer t1 on t1.id = t0.kcustomer_id"));
    Assert.assertTrue(generatedSql.contains("left join contact t2 on t2.customer_id = t1.id"));
    Assert.assertTrue(generatedSql.contains("where lower(t1.name) like "));

    // select t0.id c0, t0.status c1, t0.order_date c2, t0.ship_date c3, t1.name c4, t0.cretime c5, t0.updtime c6,
    //        t1.id c7, t1.status c8, t1.name c9, t1.smallnote c10, t1.anniversary c11, t1.cretime c12, t1.updtime c13, t1.billing_address_id c14, t1.shipping_address_id c15,
    //        t2.id c16, t2.first_name c17, t2.last_name c18, t2.phone c19, t2.mobile c20, t2.email c21, t2.cretime c22, t2.updtime c23, t2.customer_id c24, t2.group_id c25
    // from o_order t0
    // join o_customer t1 on t1.id = t0.kcustomer_id
    // left join contact t2 on t2.customer_id = t1.id
    // where lower(t1.name) like ? ; --bind(rob%)
  }
}

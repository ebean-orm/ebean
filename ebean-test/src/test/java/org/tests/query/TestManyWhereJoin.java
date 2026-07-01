package org.tests.query;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.Product;
import org.tests.model.basic.ResetBasicData;

import static org.assertj.core.api.Assertions.assertThat;

public class TestManyWhereJoin extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<Customer> query = DB.find(Customer.class).select("id,status")
      // the where on a 'many' (like orders) requires an
      // additional join and distinct which is independent
      // of a fetch join (if there is a fetch join)
      .where().eq("orders.status", Order.Status.NEW)
      // .where().eq("orders.details.product.name", "Desk")
      .query();

    query.findList();
    String sql = sqlOf(query, 1);

    // select distinct t0.id c0, t0.status c1
    // from o_customer t0
    // join o_order u1 on u1.kcustomer_id = t0.id
    // where u1.status = ? ; --bind(NEW)

    if (isPostgresCompatible()) {
      assertThat(sql).contains("select distinct on (t0.id) t0.id, ");
    } else {
      assertThat(sql).contains("select distinct t0.id");
    }
    assertThat(sql).contains("join o_order ");
    assertThat(sql).contains(".status = ?");
    assertThat(sql).contains("t0.id, t0.status from o_customer t0 join o_order u1 on u1.kcustomer_id = t0.id and u1.order_date is not null where u1.status = ?");
  }

  @Test
  public void testWithFetchJoinAndWhere() {

    ResetBasicData.reset();

    Query<Customer> query = DB.find(Customer.class).select("id,status")
      .fetch("orders")
      // the where on a 'many' (like orders) requires an
      // additional join and distinct which is independent
      // of a fetch join (if there is a fetch join)
      .where().eq("orders.status", Order.Status.NEW)
      // .where().eq("orders.details.product.name", "Desk")
      .query();

    query.findList();
    String sql = sqlOf(query, 3);

    // select distinct t0.id c0, t0.status c1,
    //        t1.id c2, t1.status c3, t1.order_date c4, t1.ship_date c5, t2.name c6, t1.cretime c7, t1.updtime c8, t1.kcustomer_id c9, t0.id
    // from o_customer t0
    // left join o_order t1 on t1.kcustomer_id = t0.id
    // left join o_customer t2 on t2.id = t1.kcustomer_id
    // join o_order u1 on u1.kcustomer_id = t0.id
    // where t1.order_date is not null  and u1.status = ?
    // order by t0.id; --bind(NEW)

    if (platformDistinctOn()) {
      assertThat(sql).contains("select distinct on (t0.id, t1.id) t0.id, t0.status,");
    } else {
      assertThat(sql).contains("select distinct t0.id, t0.status, t1.id, t1.status,");
    }
    assertThat(sql).contains("left join o_order t1 on ");
    assertThat(sql).contains("join o_order u1 on ");
    assertThat(sql).contains(" u1.status = ?");
  }

  @Test
  public void testUsingForeignKey() {

    ResetBasicData.reset();

    Long productId = 1L;

    Query<Order> query = DB.find(Order.class)
      .where().eq("details.product.id", productId)
      .orderBy("cretime asc").query();

    query.findList();
    String sql = sqlOf(query, 3);

    // select distinct t0.id c0, t0.status c1, t0.order_date c2, t0.ship_date c3, t1.name c4, t0.cretime c5, t0.updtime c6, t0.kcustomer_id c7, t0.cretime
    // from o_order t0
    // join o_customer t1 on t1.id = t0.kcustomer_id
    // join o_order_detail u1 on u1.order_id = t0.id
    // where u1.product_id = ?
    // order by t0.cretime; --bind(1)

    if (platformDistinctOn()) {
      assertThat(sql).contains("select distinct on (t0.cretime, t0.id) t0.id, t0.status,");
    } else {
      assertThat(sql).contains("select distinct t0.id, t0.status,");
    }
    assertThat(sql).contains(" join o_order_detail u1 on u1.order_id = t0.id");
    assertThat(sql).contains(" where u1.product_id = ?");
  }

  /**
   * Same as previous test but use a reference bean.
   */
  @Test
  public void testUsingForeignKeyReferenceBean() {

    ResetBasicData.reset();

    Product product = DB.reference(Product.class, 1L);

    Query<Order> query = DB.find(Order.class)
      //.fetch("details")
      .where().eq("details.product", product)
      .orderBy("cretime asc").query();

    query.findList();
    String sql = sqlOf(query, 3);

    // select distinct t0.id c0, t0.status c1, t0.order_date c2, t0.ship_date c3, t1.name c4, t0.cretime c5, t0.updtime c6, t0.kcustomer_id c7, t0.cretime
    // from o_order t0
    // join o_customer t1 on t1.id = t0.kcustomer_id
    // join o_order_detail u1 on u1.order_id = t0.id
    // where u1.product_id = ?
    // order by t0.cretime

    if (platformDistinctOn()) {
      assertThat(sql).contains("select distinct on (t0.cretime, t0.id) t0.id, t0.status,");
    } else {
      assertThat(sql).contains("select distinct t0.id, t0.status,");
    }
    assertThat(sql).contains(" join o_order_detail u1 on u1.order_id = t0.id");
    assertThat(sql).contains(" where u1.product_id = ?");
  }

  /**
   * Additionally add a fetch join.
   */
  @Test
  public void testUsingForeignKeyAndFetch() {

    ResetBasicData.reset();

    Product product = DB.reference(Product.class, 1L);

    Query<Order> query = DB.find(Order.class)
      .fetch("details")
      .where().eq("details.product", product)
      .orderBy("cretime asc").query();

    query.findList();
    String sql = sqlOf(query, 3);

    // select distinct t0.id c0, t0.status c1, t0.order_date c2, t0.ship_date c3, t2.name c4, t0.cretime c5, t0.updtime c6, t0.kcustomer_id c7,
    //        t1.id c8, t1.order_qty c9, t1.ship_qty c10, t1.unit_price c11, t1.cretime c12, t1.updtime c13, t1.order_id c14, t1.product_id c15, t0.cretime, t0.id, t1.id, t1.order_qty, t1.cretime
    // from o_order t0
    // join o_customer t2 on t2.id = t0.kcustomer_id
    // left join o_order_detail t1 on t1.order_id = t0.id
    // join o_order_detail u1 on u1.order_id = t0.id
    // where t1.id > 0  and u1.product_id = ?
    // order by t0.cretime, t0.id, t1.id asc, t1.order_qty asc, t1.cretime desc; --bind(1)

    if (platformDistinctOn()) {
      assertThat(sql).contains("select distinct on (t0.cretime, t0.id, t1.id, t1.order_qty, t1.cretime) t0.id, t0.status,");
    } else {
      assertThat(sql).contains("select distinct t0.id, t0.status,");
    }
    assertThat(sql).contains(" join o_order_detail u1 on u1.order_id = t0.id");
    assertThat(sql).contains(" u1.product_id = ?");

    // additional join for fetching the many details
    assertThat(sql).contains(" left join o_order_detail t1 on t1.order_id = t0.id");
    assertThat(sql).contains("order by t0.cretime, t0.id, t1.id asc, t1.order_qty asc, t1.cretime desc");
  }
}

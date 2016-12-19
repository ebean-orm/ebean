package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import static org.assertj.core.api.Assertions.assertThat;

public class TestImplicitJoinOnParentRelationship extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
      .select("id, name")
      .where().eq("orders.details.product.name", "Desk")
      .query();

    query.findList();

    if (isPostgres()) {
      String expectedSql = "select distinct on (t0.id) t0.id, t0.name from o_customer t0 join o_order u1 on u1.kcustomer_id = t0.id  join o_order_detail u2 on u2.order_id = u1.id  join o_product u3 on u3.id = u2.product_id  where u3.name = ? ";
      assertThat(sqlOf(query, 1)).contains(expectedSql);

    } else {
      String expectedSql = "select distinct t0.id, t0.name from o_customer t0 join o_order u1 on u1.kcustomer_id = t0.id  join o_order_detail u2 on u2.order_id = u1.id  join o_product u3 on u3.id = u2.product_id  where u3.name = ? ";
      assertThat(sqlOf(query, 1)).contains(expectedSql);
    }

    // select distinct t0.id c0, t0.name c1
    // from o_customer t0
    // join o_order u1 on u1.kcustomer_id = t0.id
    // join o_order_detail u2 on u2.order_id = u1.id
    // join o_product u3 on u3.id = u2.product_id
    // where u3.name = ?

  }


  @Test
  public void testWithDisjunction() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
      .select("id, name")
      .where().disjunction().eq("orders.details.product.name", "Desk").eq("id", 4).endJunction()
      .query();

    query.findList();

    if (isPostgres()) {
      String expectedSql = "select distinct on (t0.id) t0.id, t0.name from o_customer t0 left join o_order u1 on u1.kcustomer_id = t0.id  left join o_order_detail u2 on u2.order_id = u1.id  left join o_product u3 on u3.id = u2.product_id  where (u3.name = ?  or t0.id = ? ) ";
      assertThat(sqlOf(query, 1)).contains(expectedSql);
    } else {
      String expectedSql = "select distinct t0.id, t0.name from o_customer t0 left join o_order u1 on u1.kcustomer_id = t0.id  left join o_order_detail u2 on u2.order_id = u1.id  left join o_product u3 on u3.id = u2.product_id  where (u3.name = ?  or t0.id = ? ) ";
      assertThat(sqlOf(query, 1)).contains(expectedSql);
    }
  }

  @Test
  public void testWithOr() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
      .select("id, name")
      .where().or().eq("orders.details.product.name", "Desk").eq("id", 4).endJunction()
      .query();

    query.findList();

    if (isPostgres()) {
      String expectedSql = "select distinct on (t0.id) t0.id, t0.name from o_customer t0 left join o_order u1 on u1.kcustomer_id = t0.id  left join o_order_detail u2 on u2.order_id = u1.id  left join o_product u3 on u3.id = u2.product_id  where (u3.name = ?  or t0.id = ? ) ";
      assertThat(sqlOf(query, 1)).contains(expectedSql);

    } else {
      String expectedSql = "select distinct t0.id, t0.name from o_customer t0 left join o_order u1 on u1.kcustomer_id = t0.id  left join o_order_detail u2 on u2.order_id = u1.id  left join o_product u3 on u3.id = u2.product_id  where (u3.name = ?  or t0.id = ? ) ";
      assertThat(sqlOf(query, 1)).contains(expectedSql);
    }
  }
}

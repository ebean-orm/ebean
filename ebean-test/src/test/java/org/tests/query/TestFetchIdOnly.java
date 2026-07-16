package org.tests.query;

import io.ebean.DB;
import io.ebean.Query;
import io.ebean.text.PathProperties;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Fetching only the id of a *ToOne association should not require a join - the
 * foreign key column is already present on the owning table (issue #3643).
 */
public class TestFetchIdOnly extends BaseTestCase {

  @Test
  public void test_withFetchPath() {
    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class)
      .apply(PathProperties.parse("status,customer(id)"));

    query.findList();
    assertSql(query.getGeneratedSql()).contains("select t0.id, t0.status, t0.kcustomer_id from o_order t0");
  }

  @Test
  public void test_withSelect() {
    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class)
      .select("status, customer");

    query.findList();
    assertSql(query.getGeneratedSql()).contains("select t0.id, t0.status, t0.kcustomer_id from o_order t0");
  }

  @Test
  public void test_withFetch() {
    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class)
      .select("status")
      .fetch("customer", "id");

    query.findList();
    assertSql(query.getGeneratedSql()).contains("select t0.id, t0.status, t0.kcustomer_id from o_order t0");
  }

  @Test
  public void test_withFetch_whenIncludesMoreThanId_expectJoin() {
    ResetBasicData.reset();

    Query<Order> query = DB.find(Order.class)
      .select("status")
      .fetch("customer", "id, name");

    query.findList();
    assertThat(query.getGeneratedSql()).contains("join");
  }
}

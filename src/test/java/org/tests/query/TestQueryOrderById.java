package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.Test;
import org.tests.model.basic.Customer;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class TestQueryOrderById extends BaseTestCase {

  @Test
  public void orderById_default_expectNotOrderById() {

    Query<Customer> query = DB.find(Customer.class)
      .select("id,name")
      .setFirstRow(1)
      .setMaxRows(5);

    query.findList();
    assertThat(sqlOf(query)).isEqualTo("select t0.id, t0.name from o_customer t0 limit 5 offset 1");
  }

  @Test
  public void orderById_whenTrue_expectOrderById() {

    Query<Customer> query = DB.find(Customer.class)
      .select("id,name")
      .setFirstRow(1)
      .setMaxRows(5)
      .orderById(true);

    query.findList();
    assertThat(sqlOf(query)).isEqualTo("select t0.id, t0.name from o_customer t0 order by t0.id limit 5 offset 1");
  }
}

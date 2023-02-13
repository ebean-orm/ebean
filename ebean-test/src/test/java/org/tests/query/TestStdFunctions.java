package org.tests.query;

import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;

import static io.ebean.StdOperators.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testing the expressions in StdFunctions (but without query beans so using Query.Property.of).
 */
public class TestStdFunctions {

  @Test
  void coalesceLike() {
    Query.Property<String> name = Query.Property.of("name");
    var query = DB.find(Customer.class)
      .select(coalesce(name, "na").toString())
      .where()
      .add(like(coalesce(name, "na"), "foo%"))
      .query();

    query.findSingleAttributeList();
    assertThat(query.getGeneratedSql()).contains("select coalesce(t0.name,'na') from o_customer t0 where coalesce(t0.name,'na') like ?");
  }

  @Test
  void concatEq() {
    var name = Query.Property.of("name");
    var status = Query.Property.of("status");

    var query = DB.find(Customer.class)
      .select(concat(name, "na", status).toString())
      .where()
      .add(eq(concat(name, "na", status), "foo"))
      .query();

    query.findSingleAttributeList();
    assertThat(query.getGeneratedSql()).contains("select concat(t0.name,'na',t0.status) from o_customer t0 where concat(t0.name,'na',t0.status) = ?");
  }
}

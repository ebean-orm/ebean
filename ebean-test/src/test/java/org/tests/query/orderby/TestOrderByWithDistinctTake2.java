package org.tests.query.orderby;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestOrderByWithDistinctTake2 extends BaseTestCase {

  @Test
  public void testRegex() {
    String test = "helloasc asc ASC desc DESC boodesc desc ASC";

    test = test.replaceAll("(?i)\\b asc\\b|\\b desc\\b", "");
    assertEquals("helloasc boodesc", test);
  }

  @Test
  public void test() {

    Query<Customer> query = DB.find(Customer.class)
      .select("id, name")
      .where().ilike("contacts.firstName", "R%")
      .orderBy("name desc").query();

    query.findList();

    String generatedSql = sqlOf(query);

    // select distinct t0.id c0, t0.name
    // from o_customer t0 join contact u1 on u1.customer_id = t0.id
    // where lower(u1.first_name) like ?
    // order by t0.name; --bind(r%)

    if (platformDistinctOn()) {
      assertThat(generatedSql).contains("select distinct on (t0.name, t0.id) t0.id, t0.name");

    } else {
      assertThat(generatedSql).contains("select distinct t0.id, t0.name");
    }
    assertThat(generatedSql).contains("order by t0.name desc");
    assertThat(generatedSql).contains("from o_customer t0 join contact u1 on u1.customer_id = t0.id");
    assertThat(generatedSql).contains("where lower(u1.first_name) like ");
  }

  @Test
  public void testWithAscAndDesc() {

    Query<Customer> query = DB.find(Customer.class)
      .select("id")
      .where().ilike("contacts.firstName", "R%")
      .orderBy("name asc,id desc").query();

    query.findList();

    String generatedSql = sqlOf(query);

    if (platformDistinctOn()) {
      assertThat(generatedSql).contains("select distinct on (t0.name, t0.id) t0.id, t0.name");
    } else {
      assertThat(generatedSql).contains("select distinct t0.id, t0.name");
    }
    assertThat(generatedSql).contains("order by t0.name, t0.id desc");
    assertThat(generatedSql).contains("from o_customer t0 join contact u1 on u1.customer_id = t0.id");
    assertThat(generatedSql).contains("where lower(u1.first_name) like ");
  }

}

package org.tests.query.orderby;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.Customer;
import org.junit.Assert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOrderByWithDistinctTake2 extends BaseTestCase {

  @Test
  public void testRegex() {

    String test = "helloasc asc ASC desc DESC boodesc desc ASC";

    test = test.replaceAll("(?i)\\b asc\\b|\\b desc\\b", "");
    Assert.assertEquals("helloasc boodesc", test);
  }


  @Test
  public void test() {

    Query<Customer> query = Ebean.find(Customer.class)
      .select("id, name")
      .where().ilike("contacts.firstName", "R%")
      .order("name desc");

    query.findList();

    String generatedSql = sqlOf(query);

    // select distinct t0.id c0, t0.name
    // from o_customer t0 join contact u1 on u1.customer_id = t0.id
    // where lower(u1.first_name) like ?
    // order by t0.name; --bind(r%)

    if (isPostgres()) {
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

    Query<Customer> query = Ebean.find(Customer.class)
      .select("id")
      .where().ilike("contacts.firstName", "R%")
      .order("name asc,id desc");

    query.findList();

    String generatedSql = sqlOf(query);

    if (isPostgres()) {
      assertThat(generatedSql).contains("select distinct on (t0.name, t0.id) t0.id, t0.name, t0.id");
    } else {
      assertThat(generatedSql).contains("select distinct t0.id, t0.name, t0.id");
    }
    assertThat(generatedSql).contains("order by t0.name, t0.id desc");
    assertThat(generatedSql).contains("from o_customer t0 join contact u1 on u1.customer_id = t0.id");
    assertThat(generatedSql).contains("where lower(u1.first_name) like ");
  }

}

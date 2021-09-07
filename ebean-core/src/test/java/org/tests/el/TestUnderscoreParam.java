package org.tests.el;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.tests.model.basic.Customer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestUnderscoreParam extends BaseTestCase {

  @Test
  public void test() {

    Query<Customer> query = DB.find(Customer.class)
      .where().raw("name like ?", "Rob%")
      .query();

    query.findList();

    assertSql(query).contains("where t0.name like ?");

  }

}

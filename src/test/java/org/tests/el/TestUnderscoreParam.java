package org.tests.el;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.Customer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestUnderscoreParam extends BaseTestCase {

  @Test
  public void test() {

    Query<Customer> query = Ebean.find(Customer.class)
      .where().raw("name like ?", "Rob%")
      .query();

    query.findList();

    assertThat(query.getGeneratedSql()).contains("where t0.name like ?");

  }

}

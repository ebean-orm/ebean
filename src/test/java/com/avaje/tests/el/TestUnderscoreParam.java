package com.avaje.tests.el;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestUnderscoreParam {

  @Test
  public void test() {

    Query<Customer> query = Ebean.find(Customer.class)
      .where().raw("name like ?", "Rob%")
      .query();

    query.findList();

    assertThat(query.getGeneratedSql()).contains("where t0.name like ?");

  }

}

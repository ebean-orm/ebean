package org.tests.query;

import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import static org.assertj.core.api.Assertions.assertThat;

class TestQueryAlsoIf {

  int dummy = 1;

  @Test
  void apply() {
    ResetBasicData.reset();
    Query<Customer> query = DB.find(Customer.class)
      .select("name")
      .alsoIf(() -> dummy == 1, qy -> qy.where().isNotNull("name"));

    query.findList();
    assertThat(query.getGeneratedSql()).isEqualTo("select t0.id, t0.name from o_customer t0 where t0.name is not null");
  }

  @Test
  void notApply() {
    ResetBasicData.reset();
    Query<Customer> query = DB.find(Customer.class)
      .select("name")
      .alsoIf(() -> dummy > 1, qy -> qy.where().isNotNull("name"));

    query.findList();
    assertThat(query.getGeneratedSql()).isEqualTo("select t0.id, t0.name from o_customer t0");
  }
}

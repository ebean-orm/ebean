package org.tests.query;

import io.ebean.DB;
import io.ebean.Query;
import io.ebean.QueryBuilder;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class TestQueryAlsoIf extends BaseTestCase {

  int dummy = 1;

  @Test
  void also() {
    ResetBasicData.reset();

    MyPager myPager = new MyPager(10);
    Query<Customer> query = DB.find(Customer.class)
      .select("name")
      .also(myPager);

    query.findList();
    if (isLimitOffset()) {
      assertThat(query.getGeneratedSql()).isEqualTo("select t0.id, t0.name from o_customer t0 limit 10");
    }
  }

  static class MyPager implements Consumer<QueryBuilder<?, ?>> {

    final int maxRows;

    MyPager(int maxRows) {
      this.maxRows = maxRows;
    }

    @Override
    public void accept(QueryBuilder<?, ?> queryBuilder) {
      queryBuilder.setMaxRows(maxRows);
    }
  }

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

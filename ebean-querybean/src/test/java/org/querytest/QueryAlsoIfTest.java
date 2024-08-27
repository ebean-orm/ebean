package org.querytest;

import io.ebean.QueryBuilder;
import org.example.domain.Customer;
import org.example.domain.query.QCustomer;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.domain.query.QCustomer.Alias.name;

class QueryAlsoIfTest {

  int dummy = 1;

  @Test
  void also() {
    var myPager = new MyPager(10);
    var q = new QCustomer()
      .select(name)
      .also(myPager)
      .query();

    q.findList();
    assertThat(q.getGeneratedSql()).contains("select /* QueryAlsoIfTest.also */ t0.id, t0.name from be_customer t0 limit 10");
  }

  static class MyPager implements Consumer<QueryBuilder<?,?>> {

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
    var q = new QCustomer()
      .select(name)
      .name.isNotNull()
      .alsoIf(() -> dummy == 1, query -> query.status.equalTo(Customer.Status.GOOD))
      .query();

    q.findList();
    assertThat(q.getGeneratedSql()).isEqualTo("select /* QueryAlsoIfTest.apply */ t0.id, t0.name from be_customer t0 where t0.name is not null and t0.status = ?");
  }

  @Test
  void notApply() {
    var q = new QCustomer()
      .select(name)
      .name.isNotNull()
      .alsoIf(() -> dummy > 1, query -> query.status.equalTo(Customer.Status.GOOD))
      .query();

    q.findList();
    assertThat(q.getGeneratedSql()).isEqualTo("select /* QueryAlsoIfTest.notApply */ t0.id, t0.name from be_customer t0 where t0.name is not null");
  }
}

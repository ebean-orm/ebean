package org.querytest;

import org.example.domain.Customer;
import org.example.domain.query.QCustomer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.domain.query.QCustomer.Alias.name;

class QueryAlsoIfTest {

  int dummy = 1;

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

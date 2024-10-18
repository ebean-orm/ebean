package org.querytest;

import io.ebean.QueryBuilder;
import io.ebean.QueryBuilderProjection;
import io.ebean.typequery.IQueryBean;
import io.ebean.typequery.QueryBean;
import org.example.domain.Customer;
import org.example.domain.query.QCustomer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.domain.query.QCustomer.Alias.name;

class QueryAlsoIfTest {

  int dummy = 1;

  @Test
  void also() {
    var q = new QCustomer()
      .select(name)
      .name.isNotNull()
      .also(query -> query.email.isNotNull())
      .query();

    q.findList();
    assertThat(q.getGeneratedSql()).isEqualTo("select /* QueryAlsoIfTest.also */ t0.id, t0.name from be_customer t0 where t0.name is not null and t0.email is not null");
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

  @Test
  void queryBuilders_expect_fluidUseOfSELF() {
    var q = new QCustomer();
    checkQueryBean(q);
    checkIQueryBean(q);
    checkQueryBuilder(q);
    checkQueryBuilderProjection(q);
  }

  private void checkQueryBean(QueryBean<?, ?> queryBean) {
    queryBean.setFirstRow(10).setMaxRows(10);
  }

  private void checkIQueryBean(IQueryBean<?, ?> iQueryBean) {
    iQueryBean.setFirstRow(10).setMaxRows(20);
  }

  private void checkQueryBuilderProjection(QueryBuilderProjection<?, ?> queryBuilder) {
    queryBuilder.fetch("a").fetch("b");
  }

  private void checkQueryBuilder(QueryBuilder<?,?> queryBuilder) {
    queryBuilder.setFirstRow(10).setMaxRows(10);
  }
}

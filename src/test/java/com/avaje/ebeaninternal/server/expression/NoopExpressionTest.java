package com.avaje.ebeaninternal.server.expression;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class NoopExpressionTest extends BaseTestCase {

  @Test
  public void test() {

    Query<Customer> query = Ebean.find(Customer.class)
        .select("id")
        .where().add(NoopExpression.INSTANCE)
        .query();

    query.findList();
    String generatedSql = sqlOf(query);

    assertThat(generatedSql).contains("select t0.id from o_customer t0 where 1=1");
  }

  @Test
  public void test_withPreAndPost() {

    Query<Customer> query = Ebean.find(Customer.class)
        .select("id")
        .where().eq("name", null)
        .add(NoopExpression.INSTANCE)
        .ne("status", null)
        .query();

    query.findList();
    String generatedSql = sqlOf(query);

    assertThat(generatedSql).contains("select t0.id from o_customer t0 where t0.name is null  and 1=1 and t0.status is not null");
  }

  @Test
  public void isSameByPlan_when_same() {

    assertThat(new NoopExpression().isSameByPlan(new NoopExpression())).isTrue();
  }

  @Test
  public void isSameByPlan_when_diffExpressionType() {

    assertThat(new NoopExpression().isSameByPlan(null)).isFalse();
  }

  @Test
  public void isSameByBind_when_same() {

    assertThat(new NoopExpression().isSameByBind(new NoopExpression())).isTrue();
  }

  @Test
  public void isSameByBind_when_diffExpressionType() {

    assertThat(new NoopExpression().isSameByBind(null)).isTrue();
  }

}

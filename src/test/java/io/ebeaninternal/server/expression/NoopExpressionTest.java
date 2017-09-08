package io.ebeaninternal.server.expression;

import io.ebean.Ebean;
import io.ebean.Query;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.onetoone.album.Cover;

import static org.assertj.core.api.Assertions.assertThat;


public class NoopExpressionTest extends BaseExpressionTest {

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
  public void test_withSoftDeleteBean() {

    Query<Cover> query = Ebean.find(Cover.class)
      .where().add(NoopExpression.INSTANCE)
      .query();

    query.findCount();
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

    assertThat(generatedSql).contains("select t0.id from o_customer t0 where t0.name is null  and 1=1  and t0.status is not null");
  }

  @Test
  public void isSameByPlan_when_same() {

    same(new NoopExpression(), new NoopExpression());
  }

  @Test
  public void isSameByPlan_when_diffExpressionType() {

    different(new NoopExpression(), null);
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

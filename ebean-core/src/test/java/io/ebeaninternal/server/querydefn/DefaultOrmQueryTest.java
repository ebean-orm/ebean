package io.ebeaninternal.server.querydefn;


import io.ebean.CacheMode;
import io.ebean.DB;
import io.ebeaninternal.api.BindValuesKey;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.core.OrmQueryRequestTestHelper;
import io.ebeaninternal.server.expression.BaseExpressionTest;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultOrmQueryTest extends BaseExpressionTest {

  @Test
  public void when_forUpdate_then_excludeFromBeanCache() {

    DefaultOrmQuery<Customer> q1 = (DefaultOrmQuery<Customer>) DB.find(Customer.class)
      .forUpdate().where().eq("id", 42).query();

    assertThat(q1.beanCacheMode()).isSameAs(CacheMode.OFF);
  }

  @Test
  public void checkForId_when_eqId_then_translatedTo_setId() {

    DefaultOrmQuery<Order> q1 = (DefaultOrmQuery<Order>) DB.find(Order.class).where().eq("id", 42).query();
    assertThat(q1.whereExpressions()).isNotNull();
    assertThat(q1.getId()).isNull();

    assertThat(q1.isFindById()).isTrue();

    assertThat(q1.getId()).isEqualTo(42);
    assertThat(q1.whereExpressions()).isNull();
  }

  @Test
  public void checkForId_when_idEq_ok() {

    DefaultOrmQuery<Order> q1 = (DefaultOrmQuery<Order>) DB.find(Order.class).where().idEq(42).query();
    assertThat(q1.getId()).isEqualTo(42);
    assertThat(q1.isFindById()).isTrue();
    assertThat(q1.getId()).isEqualTo(42);
  }

  @Test
  public void checkForId_when_setId_ok() {

    DefaultOrmQuery<Order> q1 = (DefaultOrmQuery<Order>) DB.find(Order.class).setId(42);
    assertThat(q1.getId()).isEqualTo(42);
    assertThat(q1.isFindById()).isTrue();
    assertThat(q1.getId()).isEqualTo(42);
  }

  @Test
  void when_distinctOn_then_planChanges() {
    DefaultOrmQuery<Order> q1 = (DefaultOrmQuery<Order>) DB.find(Order.class).distinctOn("name");
    DefaultOrmQuery<Order> q2 = (DefaultOrmQuery<Order>) DB.find(Order.class);

    prepare(q1, q2);
    assertThat(q1.createQueryPlanKey()).isNotEqualTo(q2.createQueryPlanKey());
  }

  @Test
  void when_distinctOn_match() {
    DefaultOrmQuery<Order> q1 = (DefaultOrmQuery<Order>) DB.find(Order.class).distinctOn("name");
    DefaultOrmQuery<Order> q2 = (DefaultOrmQuery<Order>) DB.find(Order.class).distinctOn("name");

    prepare(q1, q2);
    assertThat(q1.createQueryPlanKey()).isEqualTo(q2.createQueryPlanKey());
    assertThat(bindKey(q1)).isEqualTo(bindKey(q2));
  }

  @Test
  void when_distinctOn_copy() {
    DefaultOrmQuery<Order> q1 = (DefaultOrmQuery<Order>) DB.find(Order.class).distinctOn("name");
    SpiQuery<Order> copy = q1.copy();
    assertThat(copy.distinctOn()).isEqualTo("name");
  }

  @Test
  public void when_addWhere_then_planChanges() {

    DefaultOrmQuery<Order> q1 = (DefaultOrmQuery<Order>) DB.find(Order.class).where().in("name", "a", "b", "c").query();
    DefaultOrmQuery<Order> q2 = (DefaultOrmQuery<Order>) DB.find(Order.class).where().in("id", 2, 2, 3).query();

    prepare(q1, q2);
    assertThat(q1.createQueryPlanKey()).isNotEqualTo(q2.createQueryPlanKey());
    assertThat(bindKey(q1)).isNotEqualTo(bindKey(q2));
  }

  @Test
  public void when_sameWhereWithDiffBindValues_then_planSame_bindDiff() {

    DefaultOrmQuery<Order> q1 = (DefaultOrmQuery<Order>) DB.find(Order.class).where().in("id", 1, 2, 3).query();
    DefaultOrmQuery<Order> q2 = (DefaultOrmQuery<Order>) DB.find(Order.class).where().in("id", 2, 2, 3).query();

    prepare(q1, q2);
    assertThat(q1.createQueryPlanKey()).isEqualTo(q2.createQueryPlanKey());
    assertThat(bindKey(q1)).isNotEqualTo(bindKey(q2));
  }

  @Test
  public void when_sameWhereAndBindValues_then_planSameAndBind() {

    DefaultOrmQuery<Order> q1 = (DefaultOrmQuery<Order>) DB.find(Order.class).where().in("id", 1, 2, 3).query();
    DefaultOrmQuery<Order> q2 = (DefaultOrmQuery<Order>) DB.find(Order.class).where().in("id", 1, 2, 3).query();

    prepare(q1, q2);
    assertThat(q1.createQueryPlanKey()).isEqualTo(q2.createQueryPlanKey());
    assertThat(bindKey(q1)).isEqualTo(bindKey(q2));
  }

  @Test
  public void when_diffFirstMaxRows_then_differentPlan() {

    DefaultOrmQuery<Order> query1 = (DefaultOrmQuery<Order>) DB.find(Order.class)
      .setFirstRow(0)
      .setMaxRows(31);

    DefaultOrmQuery<Order> query2 = (DefaultOrmQuery<Order>) DB.find(Order.class)
      .setFirstRow(1)
      .setMaxRows(0);

    prepare(query1, query2);
    assertThat(query1.createQueryPlanKey()).isNotEqualTo(query2.createQueryPlanKey());
  }

  private <T> void prepare(DefaultOrmQuery<T> q1, DefaultOrmQuery<T> q2) {

    OrmQueryRequest<T> r1 = OrmQueryRequestTestHelper.queryRequest(q1);
    q1.prepare(r1);

    OrmQueryRequest<T> r2 = OrmQueryRequestTestHelper.queryRequest(q2);
    q2.prepare(r2);
  }

  private BindValuesKey bindKey(DefaultOrmQuery<Order> query) {
    BindValuesKey key = new BindValuesKey(spiEbeanServer());
    query.queryBindKey(key);
    return key;
  }
}

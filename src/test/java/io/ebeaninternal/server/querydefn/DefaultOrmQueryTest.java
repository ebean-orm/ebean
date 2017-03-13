package io.ebeaninternal.server.querydefn;


import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultOrmQueryTest extends BaseTestCase {

  @Test
  public void when_forUpdate_then_excludeFromBeanCache() {

    DefaultOrmQuery<Customer> q1 = (DefaultOrmQuery<Customer>) Ebean.find(Customer.class)
      .setForUpdate(true).where().eq("id", 42).query();

    assertThat(q1.isExcludeBeanCache()).isTrue();
  }

  @Test
  public void checkForId_when_eqId_then_translatedTo_setId() {

    DefaultOrmQuery<Order> q1 = (DefaultOrmQuery<Order>) Ebean.find(Order.class).where().eq("id", 42).query();
    assertThat(q1.getWhereExpressions()).isNotNull();
    assertThat(q1.getId()).isNull();

    q1.checkIdEqualTo();

    assertThat(q1.getId()).isEqualTo(42);
    assertThat(q1.getWhereExpressions()).isNull();
  }

  @Test
  public void checkForId_when_idEq_ok() {

    DefaultOrmQuery<Order> q1 = (DefaultOrmQuery<Order>) Ebean.find(Order.class).where().idEq(42).query();
    assertThat(q1.getId()).isEqualTo(42);
    q1.checkIdEqualTo();
    assertThat(q1.getId()).isEqualTo(42);
  }

  @Test
  public void when_addWhere_then_planChanges() {

    DefaultOrmQuery<Order> q1 = (DefaultOrmQuery<Order>) Ebean.find(Order.class).where().in("name", "a", "b", "c").query();
    DefaultOrmQuery<Order> q2 = (DefaultOrmQuery<Order>) Ebean.find(Order.class).where().in("id", 2, 2, 3).query();

    prepare(q1, q2);
    assertThat(q1.createQueryPlanKey()).isNotEqualTo(q2.createQueryPlanKey());
    assertThat(q1.queryBindHash()).isNotEqualTo(q2.queryBindHash());
  }

  @Test
  public void when_sameWhereWithDiffBindValues_then_planSame_bindDiff() {

    DefaultOrmQuery<Order> q1 = (DefaultOrmQuery<Order>) Ebean.find(Order.class).where().in("id", 1, 2, 3).query();
    DefaultOrmQuery<Order> q2 = (DefaultOrmQuery<Order>) Ebean.find(Order.class).where().in("id", 2, 2, 3).query();

    prepare(q1, q2);
    assertThat(q1.createQueryPlanKey()).isEqualTo(q2.createQueryPlanKey());
    assertThat(q1.queryBindHash()).isNotEqualTo(q2.queryBindHash());
  }

  @Test
  public void when_sameWhereAndBindValues_then_planSameAndBind() {

    DefaultOrmQuery<Order> q1 = (DefaultOrmQuery<Order>) Ebean.find(Order.class).where().in("id", 1, 2, 3).query();
    DefaultOrmQuery<Order> q2 = (DefaultOrmQuery<Order>) Ebean.find(Order.class).where().in("id", 1, 2, 3).query();

    prepare(q1, q2);
    assertThat(q1.createQueryPlanKey()).isEqualTo(q2.createQueryPlanKey());
    assertThat(q1.queryBindHash()).isEqualTo(q2.queryBindHash());
  }

  @Test
  public void when_diffFirstMaxRows_then_differentPlan() throws Exception {

    DefaultOrmQuery<?> query1 = (DefaultOrmQuery<?>) Ebean.find(Order.class)
      .setFirstRow(0)
      .setMaxRows(31);

    DefaultOrmQuery<?> query2 = (DefaultOrmQuery<?>) Ebean.find(Order.class)
      .setFirstRow(1)
      .setMaxRows(0);

    prepare(query1, query2);
    assertThat(query1.createQueryPlanKey()).isNotEqualTo(query2.createQueryPlanKey());
  }

  private void prepare(DefaultOrmQuery<?> q1, DefaultOrmQuery<?> q2) {
    q1.prepare(null);
    q2.prepare(null);
  }
}

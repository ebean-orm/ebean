package com.avaje.ebeaninternal.server.querydefn;


import com.avaje.ebean.Ebean;
import com.avaje.ebean.FetchConfig;
import com.avaje.tests.model.basic.Order;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultOrmQueryTest {

  @Test
  public void when_addWhere_then_planChanges() {

    DefaultOrmQuery<Order> q1 = (DefaultOrmQuery<Order>)Ebean.find(Order.class).where().in("name", "a","b","c").query();
    DefaultOrmQuery<Order> q2 = (DefaultOrmQuery<Order>)Ebean.find(Order.class).where().in("id", 2,2,3).query();

    assertThat(q1.createQueryPlanKey()).isNotEqualTo(q2.createQueryPlanKey());
    assertThat(q1.queryBindHash()).isNotEqualTo(q2.queryBindHash());
  }

  @Test
  public void when_sameWhereWithDiffBindValues_then_planSame_bindDiff() {

    DefaultOrmQuery<Order> q1 = (DefaultOrmQuery<Order>)Ebean.find(Order.class).where().in("id", 1,2,3).query();
    DefaultOrmQuery<Order> q2 = (DefaultOrmQuery<Order>)Ebean.find(Order.class).where().in("id", 2,2,3).query();

    assertThat(q1.createQueryPlanKey()).isEqualTo(q2.createQueryPlanKey());
    assertThat(q1.queryBindHash()).isNotEqualTo(q2.queryBindHash());
  }

  @Test
  public void when_sameWhereAndBindValues_then_planSameAndBind() {

    DefaultOrmQuery<Order> q1 = (DefaultOrmQuery<Order>)Ebean.find(Order.class).where().in("id", 1,2,3).query();
    DefaultOrmQuery<Order> q2 = (DefaultOrmQuery<Order>)Ebean.find(Order.class).where().in("id", 1,2,3).query();

    assertThat(q1.createQueryPlanKey()).isEqualTo(q2.createQueryPlanKey());
    assertThat(q1.queryBindHash()).isEqualTo(q2.queryBindHash());
  }


  @Test
  public void when_FetchConfig_then_differentPlan() throws Exception {

    DefaultOrmQuery<?> query1 = (DefaultOrmQuery<?>)Ebean.find(Order.class)
        .select("status, shipDate")
        .fetch("details", "orderQty, unitPrice", new FetchConfig().query())
        .fetch("details.product", "sku, name");


    DefaultOrmQuery<?> query2 = (DefaultOrmQuery<?>)Ebean.find(Order.class)
        .select("status, shipDate")
        .fetch("details", "orderQty, unitPrice")
        .fetch("details.product", "sku, name");

    assertThat(query1.createQueryPlanKey()).isNotEqualTo(query2.createQueryPlanKey());
  }

  @Test
  public void when_diffFirstMaxRows_then_differentPlan() throws Exception {

    DefaultOrmQuery<?> query1 = (DefaultOrmQuery<?>)Ebean.find(Order.class)
        .setFirstRow(0)
        .setMaxRows(31);

    DefaultOrmQuery<?> query2 = (DefaultOrmQuery<?>)Ebean.find(Order.class)
        .setFirstRow(1)
        .setMaxRows(0);

    assertThat(query1.createQueryPlanKey()).isNotEqualTo(query2.createQueryPlanKey());
  }

}
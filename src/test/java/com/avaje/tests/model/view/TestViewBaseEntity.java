package com.avaje.tests.model.view;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.OrderDetail;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestViewBaseEntity {

  @Test
  public void fetch() {

    ResetBasicData.reset();

    Query<EOrderAgg> query = Ebean.find(EOrderAgg.class)
        .where().gt("orderTotal", 20)
        .query();

    List<EOrderAgg> list = query.findList();

    assertThat(query.getGeneratedSql()).contains("select t0.order_id c0, t0.order_total c1, t0.ship_total c2, t0.order_id c3 from order_agg_vw t0 where t0.order_total > ? ");
    assertThat(list).isNotEmpty();
  }

  @Test
  public void lazyLoad() {

    ResetBasicData.reset();

    Query<EOrderAgg> query = Ebean.find(EOrderAgg.class)
        //.fetch("order", "id")
        .where().gt("orderTotal", 20)
        .query();

    List<EOrderAgg> list = query.findList();
    for (EOrderAgg agg : list) {
      Order order = agg.getOrder();
      List<OrderDetail> details = order.getDetails();
      assertThat(details).isNotEmpty();
    }
  }

  @Test
  public void fetchJoin() {

    ResetBasicData.reset();

    Query<EOrderAgg> query = Ebean.find(EOrderAgg.class)
        .fetch("order")
        .fetch("order.details")
        .where().gt("orderTotal", 20)
        .query();

    List<EOrderAgg> list = query.findList();
    for (EOrderAgg agg : list) {
      Order order = agg.getOrder();
      List<OrderDetail> details = order.getDetails();
      assertThat(details).isNotEmpty();
    }

    assertThat(query.getGeneratedSql()).contains("from order_agg_vw t0 left outer join o_order t1 on t1.id = t0.order_id  left outer join o_customer t3 on t3.id = t1.kcustomer_id  left outer join o_order_detail t2 on t2.order_id = t1.id  where t2.id > 0  and t0.order_total > ?");
  }
}

package org.tests.model.view;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.tests.model.basic.Order;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestViewBaseEntity extends BaseTestCase {

  @Test
  public void fetch() {

    ResetBasicData.reset();

    Query<EOrderAgg> query = Ebean.find(EOrderAgg.class)
      .where().gt("orderTotal", 20)
      .query();

    List<EOrderAgg> list = query.findList();

    assertThat(sqlOf(query, 3)).contains("select t0.order_id, t0.order_total, t0.ship_total, t0.order_id from order_agg_vw t0 where t0.order_total > ? ");
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

    assertThat(query.getGeneratedSql()).contains("from order_agg_vw t0 left join o_order t1 on t1.id = t0.order_id  left join o_customer t3 on t3.id = t1.kcustomer_id  left join o_order_detail t2 on t2.order_id = t1.id  where t2.id > 0  and t0.order_total > ?");
  }
}

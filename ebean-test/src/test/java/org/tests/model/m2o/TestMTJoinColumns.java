package org.tests.model.m2o;

import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestMTJoinColumns {

  @Test
  void test() {
    MTJTrans parent = new MTJTrans();
    parent.setOrgId(51L);

    DB.save(parent);

    MTJTrans found = DB.find(MTJTrans.class)
      .setId(parent.id())
      .fetch("order")
      .findOne();

    assertThat(found.order()).isNull();

    var order = new MTJOrder()
      .setOrgId(51L)
      .setOther("some");
    DB.save(order);
    found.setOrder(order);
    DB.save(found);

    LoggedSql.start();
    MTJTrans found2 = DB.find(MTJTrans.class)
      .setId(parent.id())
      .fetch("order")
      .findOne();

    assertThat(found2.order()).isNotNull();
    assertThat(found2.order().id()).isEqualTo(order.id());
    assertThat(found2.order().other()).isEqualTo("some");

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("from mtjtrans t0 left join mtjorder t1 on t1.org_id = t0.org_id and t1.id = t0.order_id where t0.id = ?");
  }
}

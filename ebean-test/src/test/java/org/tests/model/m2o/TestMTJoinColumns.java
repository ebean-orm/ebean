package org.tests.model.m2o;

import io.ebean.DB;
import org.junit.jupiter.api.Test;

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

    MTJOrder order = found.order();
    assertThat(order).isNull();
  }
}

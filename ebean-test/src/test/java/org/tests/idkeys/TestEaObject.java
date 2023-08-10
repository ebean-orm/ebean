package org.tests.idkeys;

import io.ebean.DB;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.idkeys.db.EaObject;

import static org.assertj.core.api.Assertions.assertThat;

class TestEaObject extends BaseTestCase {

  @Test
  void insert() {
    EaObject bean = new EaObject("fred");
    DB.save(bean);
    assertThat(bean.id()).isGreaterThan(0);
  }
}

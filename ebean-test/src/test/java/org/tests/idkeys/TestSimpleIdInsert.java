package org.tests.idkeys;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.ESimple;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestSimpleIdInsert extends BaseTestCase {

  @Test
  void test() {
    ESimple e = new ESimple();
    e.setName("name");
    DB.save(e);

    assertNotNull(e.getId());
  }

}

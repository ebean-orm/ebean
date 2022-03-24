package org.tests.model.interfaces;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;

public class TestSelfMany extends BaseTestCase {

  @Test
  public void self_manyToMany() {

    SelfManyMany m = new SelfManyMany("1");
    DB.save(m);
  }
}

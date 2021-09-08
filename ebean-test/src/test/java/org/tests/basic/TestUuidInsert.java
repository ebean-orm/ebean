package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.tests.model.basic.TUuidEntity;
import org.junit.jupiter.api.Test;

public class TestUuidInsert extends BaseTestCase {

  @Test
  public void test() {

    TUuidEntity e = new TUuidEntity();
    e.setName("bana");

    DB.save(e);

    TUuidEntity e2 = DB.find(TUuidEntity.class, e.getId());
    e2.setName("apple");

    DB.save(e2);

    DB.delete(e2);
  }
}

package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.TUuidEntity;
import org.junit.Test;

public class TestUuidInsert extends BaseTestCase {

  @Test
  public void test() {

    TUuidEntity e = new TUuidEntity();
    e.setName("bana");

    Ebean.save(e);

    TUuidEntity e2 = Ebean.find(TUuidEntity.class, e.getId());
    e2.setName("apple");

    Ebean.save(e2);

    Ebean.delete(e2);
  }
}

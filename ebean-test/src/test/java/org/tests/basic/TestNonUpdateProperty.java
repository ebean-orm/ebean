package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.tests.model.basic.MNonUpdPropEntity;
import org.junit.jupiter.api.Test;

public class TestNonUpdateProperty extends BaseTestCase {

  @Test
  public void test() {

    MNonUpdPropEntity e = new MNonUpdPropEntity();
    e.setName("name");
    e.setNote("note");

    DB.save(e);

    MNonUpdPropEntity e2 = DB.find(MNonUpdPropEntity.class, e.getId());

    e2.setName("mod");
    DB.update(e2);

  }
}

package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.MNonUpdPropEntity;
import org.junit.Test;

public class TestNonUpdateProperty extends BaseTestCase {

  @Test
  public void test() {

    MNonUpdPropEntity e = new MNonUpdPropEntity();
    e.setName("name");
    e.setNote("note");

    Ebean.save(e);

    MNonUpdPropEntity e2 = Ebean.find(MNonUpdPropEntity.class, e.getId());

    e2.setName("mod");
    Ebean.update(e2);

  }
}

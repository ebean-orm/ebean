package com.avaje.tests.basic;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.MNonUpdPropEntity;

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

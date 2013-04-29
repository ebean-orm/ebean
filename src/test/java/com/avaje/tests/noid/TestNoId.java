package com.avaje.tests.noid;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Update;

public class TestNoId extends BaseTestCase {

  @Test
  public void testNoIdQuery() {

    Update<NoIdEntity> upd = Ebean.createUpdate(NoIdEntity.class, "delete from NoIdEntity");
    upd.execute();

    NoIdEntity e0 = new NoIdEntity();
    e0.setId(1);
    e0.setValue("one");

    NoIdEntity e1 = new NoIdEntity();
    e1.setId(2);
    e1.setValue("two");

    Ebean.save(e0);
    Ebean.save(e1);

    List<NoIdEntity> list = Ebean.createNamedQuery(NoIdEntity.class, "noid").findList();

    Assert.assertEquals(2, list.size());
    NoIdEntity noIdEntity0 = list.get(0);
    Assert.assertNotNull(noIdEntity0);
    Assert.assertEquals(noIdEntity0.getValue(), "one");

    NoIdEntity noIdEntity1 = list.get(1);
    Assert.assertNotNull(noIdEntity1);
    Assert.assertEquals(noIdEntity1.getValue(), "two");

  }
}

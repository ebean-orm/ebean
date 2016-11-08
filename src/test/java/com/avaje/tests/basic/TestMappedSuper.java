package com.avaje.tests.basic;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.TMapSuperEntity;
import org.junit.Assert;
import org.junit.Test;

public class TestMappedSuper extends BaseTestCase {

  @Test
  public void test() {

    TMapSuperEntity e = new TMapSuperEntity();
    e.setName("babana");

    Ebean.save(e);

    // select includes a transient property
    TMapSuperEntity e2 = Ebean.find(TMapSuperEntity.class)
      .where().idEq(e.getId())
      .select("id, name, myint, someObject, bananan")
      .findUnique();

    Assert.assertNotNull(e2);

    TMapSuperEntity eSaveDelete = new TMapSuperEntity();
    eSaveDelete.setName("babana");

    Ebean.save(eSaveDelete);

    Ebean.delete(eSaveDelete);
  }

}

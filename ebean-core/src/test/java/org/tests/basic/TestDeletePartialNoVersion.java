package org.tests.basic;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.TMapSuperEntity;
import org.junit.Assert;
import org.junit.Test;

public class TestDeletePartialNoVersion extends BaseTestCase {

  @Test
  public void testNoVersion() {

    TMapSuperEntity e = new TMapSuperEntity();
    e.setName("babanaone");

    Ebean.save(e);

    // select includes a transient property
    TMapSuperEntity e2 = Ebean.find(TMapSuperEntity.class)
      .where().idEq(e.getId())
      .select("id, name")
      .findOne();

    Assert.assertNotNull(e2);

    e2.setName("banaban2");
    Ebean.save(e2);

    Ebean.delete(e2);
  }


  public void testWithVersion() {

    TMapSuperEntity e = new TMapSuperEntity();
    e.setName("babanatwo");

    Ebean.save(e);

    // select includes a transient property
    TMapSuperEntity e2 = Ebean.find(TMapSuperEntity.class)
      .where().idEq(e.getId())
      .select("id, name, version")
      .findOne();

    Assert.assertNotNull(e2);

    e2.setName("banaban2two");
    Ebean.save(e2);

    Ebean.delete(e2);
  }
}

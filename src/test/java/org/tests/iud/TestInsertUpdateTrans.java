package org.tests.iud;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Transaction;
import org.junit.Assert;
import org.junit.Test;
import org.tests.model.basic.EBasicVer;

public class TestInsertUpdateTrans extends BaseTestCase {

  @Test
  public void test() {

    try (Transaction txn = Ebean.beginTransaction()) {

      EBasicVer e0 = new EBasicVer("onInsert");
      e0.setDescription("something");
      Ebean.save(e0);

      Assert.assertNotNull(e0.getId());
      Assert.assertNotNull(e0.getLastUpdate());
      Assert.assertEquals("onInsert", e0.getName());

      e0.setName("onUpdate");
      e0.setDescription("differentFromInsert");

      Ebean.save(e0);

      EBasicVer e1 = Ebean.find(EBasicVer.class, e0.getId());

      // we should fetch back the updated data (not inserted)
      Assert.assertEquals(e0.getId(), e1.getId());
      Assert.assertEquals("onUpdate", e1.getName());
      Assert.assertEquals("differentFromInsert", e1.getDescription());

      txn.commit();
    }
  }
}

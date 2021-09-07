package org.tests.iud;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasicVer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestInsertUpdateTrans extends BaseTestCase {

  @Test
  public void test() {

    try (Transaction txn = DB.beginTransaction()) {

      EBasicVer e0 = new EBasicVer("onInsert");
      e0.setDescription("something");
      DB.save(e0);

      assertNotNull(e0.getId());
      assertNotNull(e0.getLastUpdate());
      assertEquals("onInsert", e0.getName());

      e0.setName("onUpdate");
      e0.setDescription("differentFromInsert");

      DB.save(e0);

      EBasicVer e1 = DB.find(EBasicVer.class, e0.getId());

      // we should fetch back the updated data (not inserted)
      assertEquals(e0.getId(), e1.getId());
      assertEquals("onUpdate", e1.getName());
      assertEquals("differentFromInsert", e1.getDescription());

      txn.commit();
    }
  }
}

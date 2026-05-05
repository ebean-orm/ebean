package org.tests.basic;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.TMapSuperEntity;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestMappedSuper extends BaseTestCase {

  @Test
  public void test() {

    TMapSuperEntity e = new TMapSuperEntity();
    e.setName("babana");

    DB.save(e);

    // select includes a transient property
    TMapSuperEntity e2 = DB.find(TMapSuperEntity.class)
      .where().idEq(e.getId())
      .select("id, name, myint, someObject")
      .findOne();

    assertNotNull(e2);

    TMapSuperEntity eSaveDelete = new TMapSuperEntity();
    eSaveDelete.setName("babana");

    DB.save(eSaveDelete);

    DB.delete(eSaveDelete);
  }

}

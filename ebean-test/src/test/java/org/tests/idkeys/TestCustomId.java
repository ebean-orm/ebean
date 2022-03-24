package org.tests.idkeys;

import io.avaje.moduuid.ModUUID;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.ECustomId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestCustomId extends BaseTestCase {

  @Test
  public void insert() {

    ECustomId bean = new ECustomId("fred");
    DB.save(bean);
    assertNotNull(bean.getId());
  }

  @Test
  public void insert_when_hasValue() {

    ECustomId bean = new ECustomId("gotId");
    String explicitId = ModUUID.newShortId();
    bean.setId(explicitId);
    DB.save(bean);

    ECustomId found = DB.find(ECustomId.class, explicitId);
    assertEquals(found.getName(), bean.getName());
  }
}

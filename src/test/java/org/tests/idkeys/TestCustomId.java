package org.tests.idkeys;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.basic.ECustomId;
import org.avaje.moduuid.ModUUID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestCustomId extends BaseTestCase {

  @Test
  public void insert() {

    ECustomId bean = new ECustomId("fred");
    Ebean.save(bean);
    assertNotNull(bean.getId());
  }

  @Test
  public void insert_when_hasValue() {

    ECustomId bean = new ECustomId("gotId");
    String explicitId = ModUUID.newShortId();
    bean.setId(explicitId);
    Ebean.save(bean);

    ECustomId found = Ebean.find(ECustomId.class, explicitId);
    assertEquals(found.getName(), bean.getName());
  }
}

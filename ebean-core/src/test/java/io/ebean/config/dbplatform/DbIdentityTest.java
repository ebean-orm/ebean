package io.ebean.config.dbplatform;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DbIdentityTest {

  @Test
  public void getSelectLastInsertedId() {

    DbIdentity identity = new DbIdentity();
    identity.setSelectLastInsertedIdTemplate("lastid for {table}");

    assertEquals("lastid for customer",identity.getSelectLastInsertedId("customer"));
    assertEquals("lastid for contact",identity.getSelectLastInsertedId("contact"));

    identity.setSelectLastInsertedIdTemplate("A{table}B{table}C");
    assertEquals("AoneBoneC",identity.getSelectLastInsertedId("one"));

    identity.setSelectLastInsertedIdTemplate("{table}A{table}");
    assertEquals("oneAone",identity.getSelectLastInsertedId("one"));
  }

  @Test
  public void getSelectLastInsertedId_when_null() {

    DbIdentity identity = new DbIdentity();

    assertNull(identity.getSelectLastInsertedId("customer"));
    assertNull(identity.getSelectLastInsertedId("contact"));
  }

  @Test
  public void getSelectLastInsertedId_when_noPlaceHolder() {

    DbIdentity identity = new DbIdentity();
    identity.setSelectLastInsertedIdTemplate("lastid");

    assertEquals("lastid",identity.getSelectLastInsertedId("customer"));
    assertEquals("lastid",identity.getSelectLastInsertedId("contact"));
  }
}

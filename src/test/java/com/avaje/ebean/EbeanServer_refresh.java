package com.avaje.ebean;

import com.avaje.tests.model.basic.EBasic;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EbeanServer_refresh {

  @Test
  public void basic() {

    EbeanServer server = Ebean.getDefaultServer();

    EBasic basic = new EBasic("basic refresh");
    basic.setStatus(EBasic.Status.NEW);
    server.save(basic);

    int rows =
        server.update(EBasic.class)
        .set("status", EBasic.Status.ACTIVE)
        .where().idEq(basic.getId())
        .update();

    assertEquals(rows, 1);

    server.refresh(basic);
    assertEquals(basic.getStatus(), EBasic.Status.ACTIVE);
  }
}

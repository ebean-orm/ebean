package com.avaje.tests.update;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.tests.model.basic.EBasic;
import com.avaje.tests.model.basic.EBasic.Status;

public class TestStatelessUpdate extends BaseTestCase {

  @Test
  public void test() {

    // GlobalProperties.put("ebean.defaultUpdateNullProperties", "true");
    // GlobalProperties.put("ebean.defaultDeleteMissingChildren", "false");

    EbeanServer server = Ebean.getServer(null);

    EBasic e = new EBasic();
    e.setName("something");
    e.setStatus(Status.NEW);
    e.setDescription("wow");

    server.save(e);

    EBasic updateAll = new EBasic();
    updateAll.setId(e.getId());
    updateAll.setName("updAllProps");

    server.update(updateAll, null, false);

    EBasic updateDeflt = new EBasic();
    updateDeflt.setId(e.getId());
    updateDeflt.setName("updateDeflt");

    server.update(updateDeflt);

  }
}

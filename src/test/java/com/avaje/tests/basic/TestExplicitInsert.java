package com.avaje.tests.basic;

import java.util.List;

import com.avaje.tests.model.basic.MyEBasicConfigStartup;
import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.tests.model.basic.EBasic;

import static org.junit.Assert.assertEquals;

public class TestExplicitInsert extends BaseTestCase {

  @Test
  public void test() throws InterruptedException {

    Thread.sleep(100);

    MyEBasicConfigStartup.resetCounters();

    EBasic b = new EBasic();
    b.setName("exp insert");
    b.setDescription("explicit insert");
    b.setStatus(EBasic.Status.ACTIVE);

    EbeanServer server = Ebean.getServer(null);
    server.insert(b);

    Assert.assertNotNull(b.getId());

    EBasic b2 = server.find(EBasic.class, b.getId());
    b2.setId(null);

    b2.setName("force insert");
    server.insert(b2);

    Assert.assertNotNull(b2.getId());
    Assert.assertTrue(!b.getId().equals(b2.getId()));

    List<EBasic> list = server.find(EBasic.class).where().in("id", b.getId(), b2.getId()).findList();

    assertEquals(2, list.size());

    b2.setName("do an update");
    server.save(b2);

    server.delete(b);
    server.delete(b2);

    // just sleep a little to allow the background thread to fire
    Thread.sleep(100);

    assertEquals(2, MyEBasicConfigStartup.insertCount.get());
    assertEquals(1, MyEBasicConfigStartup.updateCount.get());
    assertEquals(2, MyEBasicConfigStartup.deleteCount.get());
  }

}

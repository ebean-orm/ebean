package com.avaje.tests.cache;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.tests.model.basic.EBasicVer;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestQueryCacheInsert extends BaseTestCase {

  @Test
  public void test() {

    EbeanServer server = Ebean.getServer(null);

    EBasicVer account = new EBasicVer("junk");
    server.save(account);

    List<EBasicVer> alist0 = server.find(EBasicVer.class).setUseQueryCache(true).findList();

    EBasicVer a2 = new EBasicVer("junk2");
    server.save(a2);
    awaitL2Cache();

    List<EBasicVer> alist1 = server.find(EBasicVer.class).setUseQueryCache(true).findList();

    assertEquals(alist0.size() + 1, alist1.size());
  }
}

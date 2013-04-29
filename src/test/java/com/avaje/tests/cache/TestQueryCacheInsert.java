package com.avaje.tests.cache;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.tests.model.basic.EBasicVer;

public class TestQueryCacheInsert extends BaseTestCase {

  @Test
  public void test() {

    EbeanServer server = Ebean.getServer(null);

    EBasicVer account = new EBasicVer();
    server.save(account);

    List<EBasicVer> alist0 = server.find(EBasicVer.class).setUseQueryCache(true).findList();

    EBasicVer a2 = new EBasicVer();
    server.save(a2);

    List<EBasicVer> alist1 = server.find(EBasicVer.class).setUseQueryCache(true).findList();

    Assert.assertEquals(alist0.size() + 1, alist1.size());
    // List<EBasicVer> noQueryCacheList = server.find(EBasicVer.class)
    // .setUseQueryCache(false)
    // .findList();
    // Assert.assertTrue(sizeOne != noQueryCacheList.size());
  }
}

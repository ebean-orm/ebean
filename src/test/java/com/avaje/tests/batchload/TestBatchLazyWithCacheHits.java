package com.avaje.tests.batchload;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.cache.ServerCacheManager;
import com.avaje.ebean.config.GlobalProperties;
import com.avaje.tests.model.basic.UUOne;

public class TestBatchLazyWithCacheHits extends BaseTestCase {

  private UUOne insert(String name) {
    UUOne one = new UUOne();
    one.setName("test-BLWCH-"+name);
    Ebean.save(one);
    return one;
  }

  @Test
  public void testOnCacheHit() {

    GlobalProperties.put("ebean.lazyLoadBatchSize", "5");
    
    ArrayList<UUOne> inserted = new ArrayList<UUOne>();
    String[] names = "A,B,C,D,E,F,G,H,I,J".split(",");
    for (int i = 0; i < names.length; i++) {
      inserted.add(insert(names[i]));
    }
    
    ServerCacheManager serverCacheManager = Ebean.getServerCacheManager();
    serverCacheManager.setCaching(UUOne.class, true);
    
    UUOne b = Ebean.find(UUOne.class)
      .setId(inserted.get(1).getId())
      .setUseCache(true)
      .findUnique();

    Assert.assertNotNull(b);
    
    UUOne b2 = Ebean.find(UUOne.class)
        .where().idEq(inserted.get(1).getId())
        .setUseCache(true)
        .findUnique();
    
    Assert.assertNotNull(b2);
    
    UUOne c = Ebean.find(UUOne.class)
        .where().idEq(inserted.get(2).getId())
        .setUseCache(true)
        .findUnique();
    
    Assert.assertNotNull(c);
    
    UUOne c2 = Ebean.find(UUOne.class)
        .where().idEq(inserted.get(2).getId())
        .setUseCache(true)
        .findUnique();
    
    Assert.assertNotNull(c2);
  
    
    
    List<UUOne> list = Ebean.find(UUOne.class)
        //.setDefaultLazyLoadBatchSize(5)
        .setUseCache(true)
        .select("id")
        .where().startsWith("name", "test-BLWCH-")
        .order("name")
        .findList();

   System.out.println(list);
   for (UUOne uuOne : list) {
     System.out.println(uuOne.getName());
   }
   list.get(0).getName();

  }

}

package com.avaje.tests.update;

import java.sql.Timestamp;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.EBasicVer;

public class TestMarkAsDirty extends BaseTestCase {

  @Test
  public void test() throws InterruptedException {
    
    EBasicVer bean = new EBasicVer("markAsDirty");
    Ebean.save(bean);
    
    Timestamp lastUpdate = bean.getLastUpdate();
    Assert.assertNotNull(lastUpdate);
    
    Thread.sleep(100);
    
    // ensure the update occurs and version property is updated/incremented
    Ebean.markAsDirty(bean);
    Ebean.save(bean);

    Timestamp lastUpdate2 = bean.getLastUpdate();
    Assert.assertNotNull(lastUpdate2);
    Assert.assertNotEquals(lastUpdate, lastUpdate2);
    
  }
  
}

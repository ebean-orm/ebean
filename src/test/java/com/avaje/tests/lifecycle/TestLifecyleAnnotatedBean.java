package com.avaje.tests.lifecycle;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.EBasicWithLifecycle;

public class TestLifecyleAnnotatedBean extends BaseTestCase {

  @Test
  public void test() {
    
    EBasicWithLifecycle bean = new EBasicWithLifecycle();
    bean.setName("hello there");
    
    Ebean.getServerCacheManager();
    Ebean.save(bean);
    Assert.assertEquals("prePersist,postPersist,", bean.getBuffer());

    EBasicWithLifecycle beanWasLoaded = Ebean.find(EBasicWithLifecycle.class, bean.getId());
    Assert.assertEquals("postLoad", beanWasLoaded.getBuffer().toString());
    
    bean.setName("Changed");
    Ebean.save(bean);
    
    Ebean.delete(bean);
    
    Assert.assertEquals("prePersist,postPersist,preUpdate,postUpdate,preRemove,postRemove", bean.getBuffer());
    
  }
  
}

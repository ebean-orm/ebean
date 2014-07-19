package com.avaje.ebeaninternal.server.deploy;


import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.ResetBasicData;
import com.avaje.tests.model.mappedsuper.ASimpleBean;

public class TestNotEnhancedMappedSuper extends BaseTestCase {

  @Test
  public void simpleBean_mappedSuperNotEnhanced_ok() {
    
//    //GlobalProperties.put("ebean.search.packages", "com.avaje.tests.model.mappedsuper");
    
    ResetBasicData.reset();
    
    ASimpleBean bean = new ASimpleBean();
    bean.setName("junk");
    
    Ebean.save(bean);
    
    Assert.assertNotNull(bean.getId());
  }
  
}

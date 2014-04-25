package com.avaje.tests.contructor;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.BeanState;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.tests.model.basic.MProtectedConstructBean;

public class TestProtectedConstructor extends BaseTestCase {

  @Test
  public void test() {
    
    EbeanServer server = Ebean.getServer(null);
    
    // check that we can construc a bean with a protected constructor
    MProtectedConstructBean bean = server.createEntityBean(MProtectedConstructBean.class);
    Assert.assertNotNull(bean);

    // Note1 that the enhancement ClassAdapterEntity line 239 makes a default constructor publically accessible
    // Note2 the ClassAdpater will call DefaultConstructor.add() to add a default constructor if it doesn't exist
    
    
    EntityBean entityBean = (EntityBean)bean;
    Object newBeanInstance = entityBean._ebean_newInstance();
    
    Assert.assertNotNull(newBeanInstance);
    BeanState beanState = Ebean.getBeanState(newBeanInstance);
    Assert.assertTrue(beanState.isNew());
    Assert.assertNotSame(entityBean, newBeanInstance);
    
  }
  
}

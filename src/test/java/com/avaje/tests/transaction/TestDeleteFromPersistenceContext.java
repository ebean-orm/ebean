package com.avaje.tests.transaction;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebeaninternal.api.SpiTransaction;
import com.avaje.tests.model.basic.EBasicVer;
import com.avaje.tests.model.basic.ResetBasicData;

public class TestDeleteFromPersistenceContext extends BaseTestCase {

  @Test
  public void testDeleteBean() {
    
    ResetBasicData.reset();
    
    EBasicVer bean = new EBasicVer();
    bean.setName("Please Delete Me");
    
    Ebean.save(bean);
    
    SpiTransaction transaction = (SpiTransaction)Ebean.beginTransaction();
    try {
      
      EBasicVer bean2 = Ebean.find(EBasicVer.class, bean.getId());
      Assert.assertNotSame(bean, bean2);
      
      EBasicVer bean3 = Ebean.find(EBasicVer.class, bean.getId());
      // same instance from PersistenceContext 
      Assert.assertSame(bean2, bean3);
      
      Object bean4 = transaction.getPersistenceContext().get(EBasicVer.class, bean.getId());
      Assert.assertSame(bean2, bean4);
      
      Ebean.delete(bean2);

      Object bean5 = transaction.getPersistenceContext().get(EBasicVer.class, bean.getId());
      Assert.assertNull("Bean is deleted from PersistenceContext",bean5);
      
      EBasicVer bean6 = Ebean.find(EBasicVer.class).where().eq("id", bean.getId()).findUnique();
      Assert.assertNull("Bean where id eq is not found "+bean6, bean6);
      
      EBasicVer bean7 = Ebean.find(EBasicVer.class, bean.getId());
      Assert.assertNull("Bean is not expected to be found? "+bean7, bean7);
      
    } finally {
      Ebean.endTransaction();
    }
    
  }
  
}

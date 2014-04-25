package com.avaje.tests.iud;

import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.BeanState;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.EBasicVer;

public class TestInsertQueryUpdate extends BaseTestCase {
  
  @Test
  public void test() {
    
    EBasicVer e0 = new EBasicVer();
    e0.setName("name0");
    e0.setDescription("desc0");
    Ebean.save(e0);
    
    EBasicVer e1 = Ebean.find(EBasicVer.class)
        .select("name")
        .setId(e0.getId())
        .findUnique();
      
    BeanState beanState = Ebean.getBeanState(e1);
    Set<String> loadedProps = beanState.getLoadedProps();
    Assert.assertFalse(loadedProps.contains("description"));
    //lastUpdate
    
    e1.setName("name1");
    Ebean.save(e1);

    e1.setDescription("desc1");
    Ebean.save(e1);

  }

}

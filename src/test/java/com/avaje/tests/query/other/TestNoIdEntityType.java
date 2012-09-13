package com.avaje.tests.query.other;

import junit.framework.Assert;

import org.junit.Test;

import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.NoIdEntityType;

public class TestNoIdEntityType {

  @Test
  public void testFindById() {

    try {
      // this should fail for this entity 
      Ebean.find(NoIdEntityType.class, 10);
      Assert.assertTrue(false);
      
    } catch (IllegalStateException e){
      // expecting this exception
      Assert.assertTrue(true);
    }
    
    NoIdEntityType noId = new NoIdEntityType();
    noId.setName("foo");
    
    Ebean.save(noId);
        
    try {
      // this should fail as no @Id property
      Ebean.delete(noId);
      Assert.assertTrue(false);
      
    } catch (IllegalStateException e){
      // expecting this exception
      Assert.assertTrue(true);      
    }
    
  }
}

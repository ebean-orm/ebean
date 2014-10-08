package com.avaje.tests.query.other;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.EBasic;

public class TestWhereLikeWithSlash  extends BaseTestCase {

  @Test
  public void test() {
  
    EBasic basic = new EBasic();
    basic.setName("slash\\monkey");
    
    Ebean.save(basic);
    
    
    Query<EBasic> query = Ebean.find(EBasic.class).where().like("name", "slash\\mon%").query();
    
    List<EBasic> list = query.findList();
    
    Assert.assertEquals(1, list.size());
    
  }
  
}

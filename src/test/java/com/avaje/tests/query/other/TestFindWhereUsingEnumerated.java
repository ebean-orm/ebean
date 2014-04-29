package com.avaje.tests.query.other;

import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.basic.MNonEnum;
import com.avaje.tests.model.basic.MNonUpdPropEntity;

public class TestFindWhereUsingEnumerated extends BaseTestCase {

  @Test
  public void test() {
    
    Ebean.find(MNonUpdPropEntity.class).where().eq("nonEnum", MNonEnum.BEGIN).findList();
    
  }
  
}

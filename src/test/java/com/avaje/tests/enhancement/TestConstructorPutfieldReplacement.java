package com.avaje.tests.enhancement;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.bean.EntityBeanIntercept;
import com.avaje.tests.model.basic.PFile;
import com.avaje.tests.model.basic.PFileContent;

public class TestConstructorPutfieldReplacement extends BaseTestCase {

  @Test
  public void test() {
    
    PFile persistentFile = new PFile("test.txt", new PFileContent("test".getBytes()));

    EntityBean eb = (EntityBean)persistentFile;
    EntityBeanIntercept ebi = eb._ebean_getIntercept();
    
    int namePos = ebi.findProperty("name");
    int fileContentPos = ebi.findProperty("fileContent");
    
    Assert.assertTrue(ebi.isLoadedProperty(namePos));
    Assert.assertTrue(ebi.isLoadedProperty(fileContentPos));
    
  }
  
}

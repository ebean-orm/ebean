package com.avaje.tests.update;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.ebean.text.json.JsonWriteOptions;
import com.avaje.tests.model.basic.UUOne;
import com.avaje.tests.model.basic.UUTwo;

public class TestJsonStatelessUpdate extends BaseTestCase {

  @Test
  public void test() {
    
    UUOne one = new UUOne();
    one.setName("oneName");

    Ebean.save(one);

    UUTwo two = new UUTwo();
    two.setMaster(one);
    two.setName("twoName");
    
    Ebean.save(two);
    
    UUTwo twoX = Ebean.find(UUTwo.class, two.getId());
    
    JsonContext jsonContext = Ebean.createJsonContext();
    
    JsonWriteOptions writeOptions = JsonWriteOptions.parsePath("(id,name,master(*))");
    String jsonString = jsonContext.toJsonString(twoX, true, writeOptions);
    
    System.out.println(jsonString);
    jsonString = jsonString.replace("twoName", "twoNameModified");
    jsonString = jsonString.replace("oneName", "oneNameModified");
    
    
    UUTwo two2 = jsonContext.toBean(UUTwo.class, jsonString);
    
    Assert.assertEquals(twoX.getId(), two2.getId());
    Assert.assertEquals("twoNameModified", two2.getName());
    Assert.assertEquals("oneNameModified", two2.getMaster().getName());

    // The update below cascades to also save "master" and that fails
    // as it thinks it should INSERT master rather than UPDATE master
    
    Ebean.update(two2);


    // confirm the properties where updated as expected
    UUTwo twoConfirm = Ebean.find(UUTwo.class, two.getId());

    Assert.assertEquals("twoNameModified", twoConfirm.getName());
    Assert.assertEquals("oneNameModified", twoConfirm.getMaster().getName());
        
  }
  
}

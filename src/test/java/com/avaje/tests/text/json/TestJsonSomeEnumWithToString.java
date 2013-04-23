package com.avaje.tests.text.json;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.tests.model.basic.SomeEnum;
import com.avaje.tests.model.basic.SomeEnumBean;

import junit.framework.TestCase;

public class TestJsonSomeEnumWithToString extends TestCase {
  
  public void testJsonConversion() {
    
    SomeEnumBean bean = new SomeEnumBean();
    bean.setId(100l);
    bean.setName("Some name");
    bean.setSomeEnum(SomeEnum.ALPHA);
    
    JsonContext json = Ebean.createJsonContext();
    String jsonContent = json.toJsonString(bean);
    
    SomeEnumBean bean2 = json.toBean(SomeEnumBean.class, jsonContent);
    
    assertEquals(bean.getSomeEnum(), bean2.getSomeEnum());
    assertEquals(bean.getName(), bean2.getName());
    assertEquals(bean.getId(), bean2.getId());
      
  }
  

}

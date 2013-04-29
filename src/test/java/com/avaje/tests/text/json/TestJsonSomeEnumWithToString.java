package com.avaje.tests.text.json;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.tests.model.basic.SomeEnum;
import com.avaje.tests.model.basic.SomeEnumBean;

public class TestJsonSomeEnumWithToString extends BaseTestCase {

  @Test
  public void testJsonConversion() {

    SomeEnumBean bean = new SomeEnumBean();
    bean.setId(100l);
    bean.setName("Some name");
    bean.setSomeEnum(SomeEnum.ALPHA);

    JsonContext json = Ebean.createJsonContext();
    String jsonContent = json.toJsonString(bean);

    SomeEnumBean bean2 = json.toBean(SomeEnumBean.class, jsonContent);

    Assert.assertEquals(bean.getSomeEnum(), bean2.getSomeEnum());
    Assert.assertEquals(bean.getName(), bean2.getName());
    Assert.assertEquals(bean.getId(), bean2.getId());

  }

}

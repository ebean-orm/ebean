package com.avaje.tests.text.json;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.tests.model.basic.SomeEnum;
import com.avaje.tests.model.basic.SomeEnumBean;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class TestJsonSomeEnumWithToString extends BaseTestCase {

  @Test
  public void testJsonConversion() throws IOException {

    SomeEnumBean bean = new SomeEnumBean();
    bean.setId(100l);
    bean.setName("Some name");
    bean.setSomeEnum(SomeEnum.ALPHA);

    JsonContext json = Ebean.json();
    String jsonContent = json.toJson(bean);

    SomeEnumBean bean2 = json.toBean(SomeEnumBean.class, jsonContent);

    Assert.assertEquals(bean.getSomeEnum(), bean2.getSomeEnum());
    Assert.assertEquals(bean.getName(), bean2.getName());
    Assert.assertEquals(bean.getId(), bean2.getId());

  }

}

package org.tests.text.json;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.text.json.JsonContext;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.SomeEnum;
import org.tests.model.basic.SomeEnumBean;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestJsonSomeEnumWithToString extends BaseTestCase {

  @Test
  public void testJsonConversion() {

    SomeEnumBean bean = new SomeEnumBean();
    bean.setId(100l);
    bean.setName("Some name");
    bean.setSomeEnum(SomeEnum.ALPHA);

    JsonContext json = DB.json();
    String jsonContent = json.toJson(bean);

    SomeEnumBean bean2 = json.toBean(SomeEnumBean.class, jsonContent);

    assertEquals(bean.getSomeEnum(), bean2.getSomeEnum());
    assertEquals(bean.getName(), bean2.getName());
    assertEquals(bean.getId(), bean2.getId());
  }

}

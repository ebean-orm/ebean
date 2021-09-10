package org.tests.json.transientproperties;

import io.ebean.DB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTransientObjectProperty {

  @Test
  public void testToBeanToJson() throws Exception {

    String rawJson = "{\"name\":\"entityBeanName\",\"basic\":true,\"someBean\":{\"name\":\"transientBeanName\",\"baz\":\"foo\"}}";

    EJsonTransientObject bean = DB.json().toBean(EJsonTransientObject.class, rawJson);

    assertEquals("entityBeanName", bean.getName());
    assertEquals(true, bean.getBasic());
    assertEquals("transientBeanName", bean.getSomeBean().name);
    assertEquals("foo", bean.getSomeBean().baz);

    String asJson = DB.json().toJson(bean);

    assertEquals(rawJson, asJson);
  }

}

package org.tests.json.transientproperties;

import io.ebean.Ebean;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestTransientObjectProperty {

  @Test
  public void testToBeanToJson() throws Exception {

    String rawJson = "{\"name\":\"entityBeanName\",\"basic\":true,\"someBean\":{\"name\":\"transientBeanName\",\"baz\":\"foo\"}}";

    EJsonTransientObject bean = Ebean.json().toBean(EJsonTransientObject.class, rawJson);

    assertEquals("entityBeanName", bean.getName());
    assertEquals(true, bean.getBasic());
    assertEquals("transientBeanName", bean.getSomeBean().name);
    assertEquals("foo", bean.getSomeBean().baz);

    String asJson = Ebean.json().toJson(bean);

    assertEquals(rawJson, asJson);
  }

}

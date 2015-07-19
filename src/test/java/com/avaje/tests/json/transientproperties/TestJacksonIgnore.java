package com.avaje.tests.json.transientproperties;

import com.avaje.ebean.Ebean;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class TestJacksonIgnore {

  @Test
  public void testJacksonJsonIgnoreAnnotation() throws Exception {

    EJsonJacksonIgnore bean = new EJsonJacksonIgnore();
    bean.setId(99L);
    bean.setName("foo");
    bean.setBasic(Boolean.TRUE);

    String asJson = Ebean.json().toJson(bean);

    // ignored on write
    assertFalse(asJson.contains("basic"));

    EJsonJacksonIgnore bean1 = Ebean.json().toBean(EJsonJacksonIgnore.class, asJson);

    assertNull(bean1.getBasic());
    assertEquals(bean.getId(), bean1.getId());
    assertEquals(bean.getName(), bean1.getName());

    // ignored on read
    String jsonFull = "{\"id\":99,\"name\":\"foo\",\"basic\":true}";

    bean1 = Ebean.json().toBean(EJsonJacksonIgnore.class, jsonFull);

    assertNull(bean1.getBasic());
    assertEquals(bean.getId(), bean1.getId());
    assertEquals(bean.getName(), bean1.getName());

  }

}

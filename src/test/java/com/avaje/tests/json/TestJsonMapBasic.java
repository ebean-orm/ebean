package com.avaje.tests.json;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.text.json.EJson;
import com.avaje.tests.model.json.EBasicJsonMap;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TestJsonMapBasic extends BaseTestCase {

  @Test
  public void testInsertUpdateDelete() throws IOException {

    String s0 = "{\"docId\":18,\"contentId\":\"asd\",\"active\":true,\"contentType\":\"pg-hello\",\"content\":{\"name\":\"rob\",\"age\":45}}";
    //String s1 = "{\"docId\":19,\"contentId\":\"asd\",\"active\":true,\"contentType\":\"pg-hello\",\"content\":{\"name\":\"rob\",\"age\":45}}";

    Map<String, Object> content = EJson.parseObject(s0);

    EBasicJsonMap bean = new EBasicJsonMap();
    bean.setName("one");
    bean.setContent(content);

    Ebean.save(bean);

    EBasicJsonMap bean1 = Ebean.find(EBasicJsonMap.class, bean.getId());

    assertEquals(bean.getId(), bean1.getId());
    assertEquals(bean.getName(), bean1.getName());
    assertEquals(bean.getContent().get("contentType"), bean1.getContent().get("contentType"));
    assertEquals(18L, bean1.getContent().get("docId"));

    bean1.setName("just change name");
    Ebean.save(bean1);

    // content changes detected - dirty state so included in update
    Map<String, Object> content1 = bean1.getContent();
    content1.put("additional", "newValue");
    content1.put("docId", 99L);
    bean1.setName("two");
    Ebean.save(bean1);

    EBasicJsonMap bean2 = Ebean.find(EBasicJsonMap.class, bean.getId());

    // name changed and docId changed
    assertEquals("two", bean2.getName());
    assertEquals(99L, bean2.getContent().get("docId"));
    assertEquals("newValue", bean2.getContent().get("additional"));

    content1.put("additional", "modValue");
    bean1.setName("three");
    bean1.setContent(content1);
    Ebean.save(bean1);

    EBasicJsonMap bean3 = Ebean.find(EBasicJsonMap.class, bean.getId());

    assertEquals("three", bean3.getName());
    assertEquals(99L, bean3.getContent().get("docId"));
    assertEquals("modValue", bean3.getContent().get("additional"));
  }
}

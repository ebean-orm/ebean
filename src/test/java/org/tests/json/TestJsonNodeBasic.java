package org.tests.json;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.json.EBasicJsonNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.*;

public class TestJsonNodeBasic extends BaseTestCase {

  @Test
  public void testInsertUpdateDelete() throws IOException {

    String s0 = "{\"docId\":18,\"contentId\":\"asd\",\"active\":true,\"contentType\":\"pg-hello\",\"content\":{\"name\":\"rob\",\"age\":45}}";
    String s1 = "{\"docId\":99,\"contentId\":\"asd\",\"active\":true,\"contentType\":\"pg-hello\",\"content\":{\"name\":\"rob\",\"age\":45}}";

    ObjectMapper objectMapper = new ObjectMapper();

    JsonNode content = objectMapper.readTree(s0);

    EBasicJsonNode bean = new EBasicJsonNode();
    bean.setName("one");
    bean.setContent(content);

    Ebean.save(bean);

    EBasicJsonNode bean1 = Ebean.find(EBasicJsonNode.class, bean.getId());

    assertEquals(bean.getId(), bean1.getId());
    assertEquals(bean.getName(), bean1.getName());

    assertEquals(bean.getContent().path("contentType").asText(), bean1.getContent().path("contentType").asText());
    assertEquals(18L, bean1.getContent().get("docId").asLong());

    bean1.setName("just change name");
    Ebean.save(bean1);

    JsonNode content1 = objectMapper.readTree(s1);

    bean1.setName("two");
    bean1.setContent(content1);
    Ebean.save(bean1);

    EBasicJsonNode bean2 = Ebean.find(EBasicJsonNode.class, bean.getId());

    // name changed and docId changed
    assertEquals("two", bean2.getName());
    assertEquals(99L, bean2.getContent().path("docId").asLong());

    // content persisted even though it has not changed (as it's loaded)
    // we can't do dirty detection on JsonNode (like we do on Map<String,Object>)
    bean1.setName("three");
    Ebean.save(bean1);

    EBasicJsonNode bean3 = Ebean.find(EBasicJsonNode.class, bean.getId());

    assertEquals("three", bean3.getName());
    assertEquals(99L, bean3.getContent().path("docId").asLong());

    // write whole bean with content as JSON
    String fullBeanJson = Ebean.json().toJson(bean3);

    // parse JSON into whole bean with content
    EBasicJsonNode beanJsonRestored = Ebean.json().toBean(EBasicJsonNode.class, fullBeanJson);

    assertEquals(99L, beanJsonRestored.getContent().path("docId").asLong());
  }

  @Test
  public void testLazyLoading() throws IOException {

    String s0 = "{\"docId\":19,\"contentId\":\"asd\",\"content\":{\"name\":\"rob\",\"age\":45}}";

    ObjectMapper objectMapper = new ObjectMapper();

    JsonNode content = objectMapper.readTree(s0);

    EBasicJsonNode bean = new EBasicJsonNode();
    bean.setName("lazyLoadTest");
    bean.setContent(content);
    Ebean.save(bean);

    EBasicJsonNode bean1 = Ebean.find(EBasicJsonNode.class)
      .select("name")
      .setId(bean.getId())
      .findUnique();

    Set<String> loadedProps = Ebean.getBeanState(bean1).getLoadedProps();
    assertTrue(loadedProps.contains("name"));
    assertFalse(loadedProps.contains("content"));

    JsonNode lazyLoadedContent = bean1.getContent();
    assertNotNull(lazyLoadedContent);

  }
}

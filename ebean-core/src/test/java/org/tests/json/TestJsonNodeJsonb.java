package org.tests.json;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.json.EBasicJsonNodeJsonB;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.*;

public class TestJsonNodeJsonb extends BaseTestCase {

  @Test
  public void testInsertUpdateDelete() throws IOException {

    String s0 = "{\"docId\":18,\"contentId\":\"asd\",\"active\":true,\"contentType\":\"pg-hello\",\"content\":{\"name\":\"rob\",\"age\":45}}";

    ObjectMapper objectMapper = new ObjectMapper();

    JsonNode content = objectMapper.readTree(s0);

    EBasicJsonNodeJsonB bean = new EBasicJsonNodeJsonB();
    bean.setName("one");
    bean.setContent(content);

    Ebean.save(bean);

    EBasicJsonNodeJsonB bean1 = Ebean.find(EBasicJsonNodeJsonB.class, bean.getId());

    assertEquals(bean.getId(), bean1.getId());
    assertEquals(bean.getName(), bean1.getName());

    assertEquals(bean.getContent().path("contentType").asText(), bean1.getContent().path("contentType").asText());
    assertEquals(18L, bean1.getContent().get("docId").asLong());

  }

  @Test
  public void testLazyLoading() throws IOException {

    String s0 = "{\"docId\":19,\"contentId\":\"asd\",\"content\":{\"name\":\"rob\",\"age\":45}}";

    ObjectMapper objectMapper = new ObjectMapper();

    JsonNode content = objectMapper.readTree(s0);

    EBasicJsonNodeJsonB bean = new EBasicJsonNodeJsonB();
    bean.setName("lazyLoadTest");
    bean.setContent(content);
    Ebean.save(bean);

    EBasicJsonNodeJsonB bean1 = Ebean.find(EBasicJsonNodeJsonB.class)
      .select("name")
      .setId(bean.getId())
      .findOne();

    Set<String> loadedProps = Ebean.getBeanState(bean1).getLoadedProps();
    assertTrue(loadedProps.contains("name"));
    assertFalse(loadedProps.contains("content"));

    JsonNode lazyLoadedContent = bean1.getContent();
    assertNotNull(lazyLoadedContent);

  }
}

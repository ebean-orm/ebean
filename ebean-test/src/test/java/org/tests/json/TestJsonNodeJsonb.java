package org.tests.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.json.EBasicJsonNodeJsonB;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestJsonNodeJsonb extends BaseTestCase {

  @Test
  public void testInsertUpdateDelete() throws IOException {

    String s0 = "{\"docId\":18,\"contentId\":\"asd\",\"active\":true,\"contentType\":\"pg-hello\",\"content\":{\"name\":\"rob\",\"age\":45}}";

    ObjectMapper objectMapper = new ObjectMapper();

    JsonNode content = objectMapper.readTree(s0);

    EBasicJsonNodeJsonB bean = new EBasicJsonNodeJsonB();
    bean.setName("one");
    bean.setContent(content);

    DB.save(bean);

    EBasicJsonNodeJsonB bean1 = DB.find(EBasicJsonNodeJsonB.class, bean.getId());

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
    DB.save(bean);

    EBasicJsonNodeJsonB bean1 = DB.find(EBasicJsonNodeJsonB.class)
      .select("name")
      .setId(bean.getId())
      .findOne();

    Set<String> loadedProps = DB.beanState(bean1).loadedProps();
    assertTrue(loadedProps.contains("name"));
    assertFalse(loadedProps.contains("content"));

    assertNotNull(bean1.getContent());
  }
}

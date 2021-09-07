package org.tests.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.tests.model.basic.Order;
import org.tests.model.json.EBasicJsonNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    DB.save(bean);

    EBasicJsonNode bean1 = DB.find(EBasicJsonNode.class, bean.getId());

    assertEquals(bean.getId(), bean1.getId());
    assertEquals(bean.getName(), bean1.getName());

    assertEquals(bean.getContent().path("contentType").asText(), bean1.getContent().path("contentType").asText());
    assertEquals(18L, bean1.getContent().get("docId").asLong());

    bean1.setName("just change name");
    DB.save(bean1);

    JsonNode content1 = objectMapper.readTree(s1);

    bean1.setName("two");
    bean1.setContent(content1);
    DB.save(bean1);

    EBasicJsonNode bean2 = DB.find(EBasicJsonNode.class, bean.getId());

    // name changed and docId changed
    assertEquals("two", bean2.getName());
    assertEquals(99L, bean2.getContent().path("docId").asLong());

    // content persisted even though it has not changed (as it's loaded)
    // we can't do dirty detection on JsonNode (like we do on Map<String,Object>)
    bean1.setName("three");
    DB.save(bean1);

    EBasicJsonNode bean3 = DB.find(EBasicJsonNode.class, bean.getId());

    assertEquals("three", bean3.getName());
    assertEquals(99L, bean3.getContent().path("docId").asLong());

    // write whole bean with content as JSON
    String fullBeanJson = DB.json().toJson(bean3);

    // parse JSON into whole bean with content
    EBasicJsonNode beanJsonRestored = DB.json().toBean(EBasicJsonNode.class, fullBeanJson);

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
    DB.save(bean);

    EBasicJsonNode bean1 = DB.find(EBasicJsonNode.class)
      .select("name")
      .setId(bean.getId())
      .findOne();

    Set<String> loadedProps = DB.beanState(bean1).loadedProps();
    assertTrue(loadedProps.contains("name"));
    assertFalse(loadedProps.contains("content"));

    assertNotNull(bean1.getContent());
  }

  @Test
  public void testJacksonEnum_WRITE_ENUMS_USING_INDEX() throws IOException {

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);

    JsonFactory jsonFactory = objectMapper.getFactory();
    StringWriter writer = new StringWriter();
    JsonGenerator generator = jsonFactory.createGenerator(writer);

    generator.writeStartObject();
    generator.writeFieldName("status");
    generator.writeObject(Order.Status.APPROVED);
    generator.writeEndObject();
    generator.flush();

    assertThat(writer.toString()).isEqualTo("{\"status\":1}");

    JsonParser parser = jsonFactory.createParser("1");

    Order.Status status = parser.readValueAs(Order.Status.class);
    assertThat(status).isEqualTo(Order.Status.APPROVED);
  }
}

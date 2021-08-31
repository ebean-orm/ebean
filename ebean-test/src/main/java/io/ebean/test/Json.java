package io.ebean.test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.List;

/**
 * Helper to convert to and from json using Jackson object mapper and
 * perform some useful asserts based on json content.
 */
public class Json {

  /**
   * For reading resource json content into a bean, list or jsonNode in a fluid style.
   */
  public static class Resource {
    private final String resourcePath;

    private Resource(String resourcePath) {
      this.resourcePath = resourcePath;
    }

    /**
     * Return as a plain bean.
     */
    <T> T asBean(Class<T> cls) {
      return Json.read(cls, readResource(resourcePath));
    }

    /**
     * Return as a list of beans.
     */
    <T> List<T> asList(Class<T> cls) {
      return Json.readList(cls, readResource(resourcePath));
    }

    /**
     * Return as a JsonNode.
     */
    JsonNode asNode() {
      return Json.readNodeFromResource(resourcePath);
    }
  }

  private static final ObjectMapper MAPPER = initMapper();

  /**
   * For fluid style reading resource json content and return as a
   * bean, list of bean or JsonNode.
   * <pre>{@code
   *
   *   PlainBean bean =
   *     Json.resource("/example/plain-list.json").asBean(PlainBean.class);
   *
   *   List<PlainBean> list =
   *     Json.resource("/example/plain-list.json").asList(PlainBean.class);
   *
   *   JsonNode jsonNode =
   *     Json.resource("/example/plain-list.json").asJsonNode();
   *
   * }</pre>
   *
   * @param resourcePath The resource path where the json content is read from
   * @return The resource to convert to a bean or jsonNode etc
   */
  public static Resource resource(String resourcePath) {
    return new Resource(resourcePath);
  }

  /**
   * Assert all the fields in the expectedJson are present in actualJson and values match.
   */
  public static void assertContains(String actualJson, String expectedJson) {
    assertContains(readNode(actualJson), readNode(expectedJson));
  }

  /**
   * Assert all the fields in the expectedJson are present in actualJson and values match.
   */
  public static void assertContains(JsonNode actualJsonNode, JsonNode expectedJsonNode) {
    JsonAssertContains.assertContains(actualJsonNode, expectedJsonNode);
  }

  /**
   * Read the content for the given resource path.
   */
  public static String readResource(String resourcePath) {
    return IOUtils.readResource(resourcePath);
  }

  /**
   * Return a bean from json content of a resource path.
   */
  public static <T> T readFromResource(Class<T> type, String resourcePath) {
    return read(type, readResource(resourcePath));
  }

  /**
   * Return a typed object from json content.
   */
  public static <T> T read(Class<T> type, String json) {
    try {
      return MAPPER.readValue(json, type);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Return a list of a given type from json content.
   */
  public static <T> List<T> readList(Class<T> type, String json) {
    final CollectionType collectionType = MAPPER.getTypeFactory().constructCollectionType(List.class, type);
    try {
      return MAPPER.readValue(json, collectionType);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Parse json into a generic JsonNode structure from resource.
   */
  public static JsonNode readNodeFromResource(String resourcePath) {
    return readNode(IOUtils.readResource(resourcePath));
  }

  /**
   * Parse json into a generic JsonNode structure.
   */
  public static JsonNode readNode(String json) {
    try {
      return MAPPER.readTree(json);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Serialize object to string
   */
  public static String toJsonString(Object bean) {
    try {
      return MAPPER.writeValueAsString(bean);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static ObjectMapper initMapper() {
    return new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .configure(SerializationFeature.INDENT_OUTPUT, true)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
      .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
      .configure(SerializationFeature.INDENT_OUTPUT, true)
      .setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

}

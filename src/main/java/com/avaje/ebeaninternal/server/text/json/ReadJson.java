package com.avaje.ebeaninternal.server.text.json;

import com.avaje.ebean.text.json.JsonReadBeanVisitor;
import com.avaje.ebean.text.json.JsonReadOptions;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

/**
 * Context for JSON read processing.
 */
public class ReadJson {

  /**
   * Jackson parser.
   */
  final JsonParser parser;

  /**
   * Stack of the path - used to find the appropriate JsonReadBeanVisitor.
   */
  final PathStack pathStack;

  /**
   * Map of the JsonReadBeanVisitor keyed by path.
   */
  final Map<String, JsonReadBeanVisitor<?>> visitorMap;

  final Object objectMapper;

  /**
   * Construct with parser and readOptions.
   */
  public ReadJson(JsonParser parser, JsonReadOptions readOptions, Object objectMapper) {

    this.parser = parser;
    this.objectMapper = objectMapper;

    // only create visitorMap, pathStack if needed ...
    this.visitorMap = (readOptions == null) ? null : readOptions.getVisitorMap();
    this.pathStack = (visitorMap == null) ? null : new PathStack();
  }

  /**
   * Return the objectMapper used for this request.
   */
  public ObjectMapper getObjectMapper() {
    if (objectMapper == null) {
      throw new IllegalStateException(
          "Jackson ObjectMapper required but has not set. The ObjectMapper can be set on"
          +" either the ServerConfig or on JsonReadOptions.");
    }
    return (ObjectMapper)objectMapper;
  }

  /**
   * Return the JsonParser.
   */
  public JsonParser getParser() {
    return parser;
  }

  /**
   * Return the next JsonToken from the underlying parser.
   */
  public JsonToken nextToken() throws IOException {
    return parser.nextToken();
  }

  /**
   * Push the path onto the stack (traversing a 1-M or M-1 etc)
   */
  public void pushPath(String path) {
    if (pathStack != null) {
      pathStack.pushPathKey(path);
    }
  }

  /**
   * Pop the path stack.
   */
  public void popPath() {
    if (pathStack != null) {
      pathStack.pop();
    }
  }

  /**
   * If there is a JsonReadBeanVisitor registered to the current path then
   * call it's visit method with the bean and unmappedProperties.
   */
  @SuppressWarnings(value = "unchecked")
  public void beanVisitor(Object bean, Map<String, Object> unmappedProperties) {
    if (visitorMap != null) {
      JsonReadBeanVisitor visitor = visitorMap.get(pathStack.peekWithNull());
      if (visitor != null) {
        visitor.visit(bean, unmappedProperties);
      }
    }
  }

  /**
   * Read the property value using Jackson ObjectMapper.
   * <p/>
   * Typically this is used to read Transient properties where the type is unknown to Ebean.
   */
  public Object readValueUsingObjectMapper(Class<?> propertyType) throws IOException {
      return getObjectMapper().readValue(parser, propertyType);
  }
}

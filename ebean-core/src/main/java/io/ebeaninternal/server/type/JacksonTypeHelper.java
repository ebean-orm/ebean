package io.ebeaninternal.server.type;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;

class JacksonTypeHelper {

  private final AnnotatedField field;
  private final ObjectMapper objectMapper;
  private final JavaType javaType;
  private final DeserializationConfig deserConfig;
  private final AnnotationIntrospector ai;

  JacksonTypeHelper(AnnotatedField field, ObjectMapper objectMapper) {
    this.field = field;
    this.objectMapper = objectMapper;
    this.javaType = field.getType();
    this.deserConfig = objectMapper.getDeserializationConfig();
    this.ai = deserConfig.getAnnotationIntrospector();
  }

  JavaType type() {
    if (ai == null || javaType == null || javaType.hasRawClass(Object.class)) {
      return javaType;
    } else {
      try {
        return ai.refineDeserializationType(deserConfig, field, javaType);
      } catch (JsonMappingException e) {
        throw new RuntimeException(e);
      }
    }
  }

  ObjectWriter objectWriter() {
    if (ai == null || javaType == null || javaType.hasRawClass(Object.class)) {
      return objectMapper.writerFor(javaType);
    } else {
      try {
        JavaType serType = ai.refineSerializationType(objectMapper.getSerializationConfig(), field, javaType);
        return objectMapper.writerFor(serType);
      } catch (JsonMappingException e) {
        throw new RuntimeException(e);
      }
    }
  }
}

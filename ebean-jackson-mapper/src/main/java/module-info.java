import io.ebean.jackson.mapper.ScalarJsonJacksonMapper;

module io.ebean.jackson.mapper {
  requires io.ebean.core.type;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;

  provides io.ebean.core.type.ScalarJsonMapper with ScalarJsonJacksonMapper;
}

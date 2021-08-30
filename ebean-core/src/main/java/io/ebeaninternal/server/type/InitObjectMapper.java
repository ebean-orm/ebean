package io.ebeaninternal.server.type;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Initialise the Jackson ObjectMapper.
 */
final class InitObjectMapper {

  /**
   * Create and return the default ObjectMapper.
   */
  static Object init() {
    SimpleModule module = new SimpleModule();
    module.addAbstractTypeMapping(Set.class, LinkedHashSet.class);
    return new ObjectMapper().registerModule(module);
  }
}

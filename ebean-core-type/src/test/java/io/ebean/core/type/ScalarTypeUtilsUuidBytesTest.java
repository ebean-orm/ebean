package io.ebean.core.type;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScalarTypeUtilsUuidBytesTest {

  @Test
  void testConversion() {
    UUID id = UUID.randomUUID();

    byte[] bytes = ScalarTypeUtils.uuidToBytes(id, false);
    assertEquals(16, bytes.length);

    UUID id2 = ScalarTypeUtils.uuidFromBytes(bytes, false);
    assertEquals(id, id2);
  }

  @Test
  void testConversionOptimized() {
    UUID id = UUID.randomUUID();

    byte[] bytes = ScalarTypeUtils.uuidToBytes(id, true);
    assertEquals(16, bytes.length);

    UUID id2 = ScalarTypeUtils.uuidFromBytes(bytes, true);
    assertEquals(id, id2);
  }

}

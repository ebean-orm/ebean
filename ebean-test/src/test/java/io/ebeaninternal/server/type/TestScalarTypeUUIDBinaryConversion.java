package io.ebeaninternal.server.type;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestScalarTypeUUIDBinaryConversion {

  @Test
  public void testConversion() {
    UUID id = UUID.randomUUID();

    byte[] bytes = ScalarTypeUUIDBinary.convertToBytes(id, false);
    assertEquals(16, bytes.length);

    UUID id2 = ScalarTypeUUIDBinary.convertFromBytes(bytes, false);
    assertEquals(id, id2);
  }

  @Test
  public void testConversionOptimized() {
    UUID id = UUID.randomUUID();

    byte[] bytes = ScalarTypeUUIDBinary.convertToBytes(id, true);
    assertEquals(16, bytes.length);

    UUID id2 = ScalarTypeUUIDBinary.convertFromBytes(bytes, true);
    assertEquals(id, id2);
  }

}

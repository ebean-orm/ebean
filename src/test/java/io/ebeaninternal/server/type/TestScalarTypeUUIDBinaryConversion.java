package io.ebeaninternal.server.type;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class TestScalarTypeUUIDBinaryConversion {

  @Test
  public void testConversion() {

    UUID id = UUID.randomUUID();

    byte[] bytes = ScalarTypeUUIDBinary.convertToBytes(id);
    Assert.assertEquals(16, bytes.length);

    UUID id2 = ScalarTypeUUIDBinary.convertFromBytes(bytes);
    Assert.assertEquals(id, id2);
  }

}

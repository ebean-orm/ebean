package com.avaje.ebeaninternal.server.type;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class TestScalarTypeUUIDBinaryConversion {

  @Test
  public void testConversion() {
    
    UUID id = UUID.randomUUID();
    
    byte[] bytes = ScalarTypeUUIDBinary.convertToBytes(id);
    Assert.assertEquals(16, bytes.length);
    
    UUID id2 = (UUID)ScalarTypeUUIDBinary.convertFromBytes(bytes);
    Assert.assertEquals(id, id2);
  }
  
}

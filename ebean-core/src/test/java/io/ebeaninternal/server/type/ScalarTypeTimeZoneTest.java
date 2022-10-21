package io.ebeaninternal.server.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScalarTypeTimeZoneTest {

  ScalarTypeTimeZone type = new ScalarTypeTimeZone();

  @Test
  void getLength() {
    assertEquals(32, type.length());
  }
}

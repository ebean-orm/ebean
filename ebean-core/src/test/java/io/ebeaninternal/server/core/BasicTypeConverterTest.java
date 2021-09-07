package io.ebeaninternal.server.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BasicTypeConverterTest {

  @Test
  public void toBoolean_when_1_long() throws Exception {
    assertTrue(BasicTypeConverter.toBoolean(1L, "T"));
  }

  @Test
  public void toBoolean_when_1_int() throws Exception {
    assertTrue(BasicTypeConverter.toBoolean(1, "T"));
  }

  @Test
  public void toBoolean_when_0() throws Exception {
    assertFalse(BasicTypeConverter.toBoolean(0L, "T"));
  }

  @Test
  public void toBoolean_when_TrueValue() throws Exception {
    assertTrue(BasicTypeConverter.toBoolean("T", "T"));
  }

  @Test
  public void toBoolean_when_notTrueValue() throws Exception {
    assertFalse(BasicTypeConverter.toBoolean("F", "T"));
  }

}

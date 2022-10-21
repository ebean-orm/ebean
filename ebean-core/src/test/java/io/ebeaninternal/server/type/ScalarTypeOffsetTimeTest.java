package io.ebeaninternal.server.type;

import org.junit.jupiter.api.Test;

import java.time.OffsetTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScalarTypeOffsetTimeTest {

  ScalarTypeOffsetTime type = new ScalarTypeOffsetTime();

  @Test
  public void testGetLength() throws Exception {

    assertEquals(25, type.length());
  }

  @Test
  public void testFormatParse() throws Exception {

    OffsetTime now = OffsetTime.now();
    String value = type.formatValue(now);
    OffsetTime offsetTime = type.parse(value);

    assertEquals(now, offsetTime);
  }

  @Test
  public void testConvertDb() throws Exception {

    OffsetTime now = OffsetTime.now();
    String value = type.convertToDbString(now);
    OffsetTime offsetTime = type.convertFromDbString(value);

    assertEquals(now, offsetTime);
  }

}

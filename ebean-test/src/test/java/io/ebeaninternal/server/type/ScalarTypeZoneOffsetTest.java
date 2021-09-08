package io.ebeaninternal.server.type;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScalarTypeZoneOffsetTest {

  ScalarTypeZoneOffset type = new ScalarTypeZoneOffset();

  @Test
  public void testGetLength() throws Exception {

    assertEquals(60, type.getLength());
  }

  @Test
  public void testFormatParse() throws Exception {

    ZonedDateTime now = ZonedDateTime.now();
    ZoneOffset offset = now.getOffset();

    String value = type.formatValue(offset);

    ZoneOffset val1 = type.parse(value);
    assertEquals(offset, val1);
  }


  @Test
  public void testConvertDb() throws Exception {

    ZonedDateTime now = ZonedDateTime.now();
    ZoneOffset offset = now.getOffset();

    String value = type.convertToDbString(offset);

    ZoneId val1 = type.convertFromDbString(value);
    assertEquals(offset, val1);
  }

}

package io.ebeaninternal.server.type;

import org.junit.Test;

import java.time.ZoneId;

import static org.junit.Assert.assertEquals;

public class ScalarTypeZoneIdTest {

  ScalarTypeZoneId type = new ScalarTypeZoneId();

  @Test
  public void testGetLength() throws Exception {

    assertEquals(60, type.getLength());
  }

  @Test
  public void testFormatParse() throws Exception {

    ZoneId zoneId = ZoneId.systemDefault();
    String value = type.formatValue(zoneId);

    ZoneId val1 = type.parse(value);
    assertEquals(zoneId, val1);
  }


  @Test
  public void testConvertDb() throws Exception {

    ZoneId zoneId = ZoneId.systemDefault();
    String value = type.convertToDbString(zoneId);

    ZoneId val1 = type.convertFromDbString(value);
    assertEquals(zoneId, val1);
  }

}

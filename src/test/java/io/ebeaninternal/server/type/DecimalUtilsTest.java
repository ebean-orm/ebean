package io.ebeaninternal.server.type;

import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

import static org.junit.Assert.assertEquals;

public class DecimalUtilsTest {

  @Test
  public void testToDecimal() throws Exception {

    Instant now = Instant.now();
    BigDecimal value = DecimalUtils.toDecimal(now);

    Instant instant = DecimalUtils.toInstant(value);
    Timestamp timestamp = DecimalUtils.toTimestamp(value);
    BigDecimal decimal = DecimalUtils.toDecimal(timestamp);

    Instant instant1 = timestamp.toInstant();

    assertEquals(instant, instant1);
    assertEquals(value, decimal);
    assertEquals(now, instant);
  }

  @Test
  public void testDuration() throws Exception {


    Duration duration = Duration.ofSeconds(323, 1500000);

    BigDecimal bigDecimal = DecimalUtils.toDecimal(duration);
    Duration duration1 = DecimalUtils.toDuration(bigDecimal);

    assertEquals(duration, duration1);
    assertEquals("PT5M23.0015S", duration1.toString());

  }
}

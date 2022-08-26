package io.ebean.core.type;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DecimalUtilsTest {

  @Test
  void testToDecimal() {
    Instant now = Instant.now();
    Timestamp sourceTimestamp = Timestamp.from(now);

    BigDecimal decimal = DecimalUtils.toDecimal(sourceTimestamp);
    Timestamp timestamp = DecimalUtils.toTimestamp(decimal);

    assertEquals(now, timestamp.toInstant());
    assertEquals(sourceTimestamp, timestamp);
  }

  @Test
  void testDuration() {
    Duration duration = Duration.ofSeconds(323, 1500000);

    BigDecimal bigDecimal = DecimalUtils.toDecimal(duration);
    Duration duration1 = DecimalUtils.toDuration(bigDecimal);

    assertEquals(duration, duration1);
    assertEquals("PT5M23.0015S", duration1.toString());
  }
}

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

    BigDecimal decimal = ScalarTypeUtils.toDecimal(sourceTimestamp);
    Timestamp timestamp = ScalarTypeUtils.toTimestamp(decimal);

    assertEquals(now, timestamp.toInstant());
    assertEquals(sourceTimestamp, timestamp);
  }

  @Test
  void testDuration() {
    Duration duration = Duration.ofSeconds(323, 1500000);

    BigDecimal bigDecimal = ScalarTypeUtils.toDecimal(duration);
    Duration duration1 = ScalarTypeUtils.toDuration(bigDecimal);

    assertEquals(duration, duration1);
    assertEquals("PT5M23.0015S", duration1.toString());
  }
}

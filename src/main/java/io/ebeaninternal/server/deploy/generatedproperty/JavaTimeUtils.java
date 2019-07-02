package io.ebeaninternal.server.deploy.generatedproperty;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Helper methods for Java time conversion.
 */
class JavaTimeUtils {

  /**
   * Return the system millis time as a LocalDateTime.
   */
  public static Object toInstant(long systemMillis) {
    return Instant.ofEpochMilli(systemMillis);
  }

  /**
   * Return the system millis time as a LocalDateTime.
   */
  static Object toLocalDateTime(long systemMillis) {
    return new Timestamp(systemMillis).toLocalDateTime();
  }

  /**
   * Return the system millis time as a OffsetDateTime.
   */
  static Object toOffsetDateTime(long systemMillis) {
    return OffsetDateTime.ofInstant(Instant.ofEpochMilli(systemMillis), ZoneId.systemDefault());
  }

  /**
   * Return the system millis time as a ZonedDateTime.
   */
  static Object toZonedDateTime(long systemMillis) {
    return ZonedDateTime.ofInstant(Instant.ofEpochMilli(systemMillis), ZoneId.systemDefault());
  }
}

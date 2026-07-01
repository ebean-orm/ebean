package io.ebean.test;

import io.ebeaninternal.server.deploy.BeanProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates random values for entity bean properties in tests.
 * <p>
 * The primary entry point is {@link #generate(BeanProperty)}, which is
 * property-aware (e.g. caps String values at the column's max length).
 * The secondary entry point {@link #generate(Class)} works on the Java type
 * alone and is useful when no property metadata is available.
 * </p>
 * <p>
 * All per-type factory methods ({@link #randomString(String, int)},
 * {@link #randomBigDecimal(int, int)}, {@link #randomLong()}, etc.) are
 * {@code protected} so that subclasses can override individual types without
 * replacing the full dispatch logic.
 * </p>
 * <p>
 * Returns {@code null} for types that are not mapped (exotic / unknown types) —
 * the caller is expected to set those fields manually.
 * </p>
 */
public class RandomValueGenerator {

  /**
   * Generate a random value for the given bean property.
   * <p>
   * For {@code String} properties, the value is capped at the column's
   * {@link BeanProperty#dbLength()} when that length is positive.
   * For {@code BigDecimal} properties, precision and scale from the column
   * definition are used.
   * </p>
   */
  public Object generate(BeanProperty prop) {
    Class<?> type = prop.type();
    if (type == String.class) {
      return randomString(prop.name(), prop.dbLength());
    }
    if (type == BigDecimal.class) {
      return randomBigDecimal(prop.dbLength(), prop.dbScale());
    }
    return generate(type);
  }

  /**
   * Generate a random value for the given Java type, without property metadata.
   * <p>
   * String values produced here use a fixed 8-character length. Use
   * {@link #generate(BeanProperty)} when column-length constraints matter.
   * </p>
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Object generate(Class<?> type) {
    if (type == null) return null;
    if (type == String.class) return randomString(null, 0);
    if (type == Boolean.class || type == boolean.class) return randomBoolean();
    if (type == UUID.class) return randomUUID();
    if (type == Instant.class) return randomInstant();
    if (type == OffsetDateTime.class) return randomOffsetDateTime();
    if (type == ZonedDateTime.class) return randomZonedDateTime();
    if (type == LocalDate.class) return randomLocalDate();
    if (type == LocalDateTime.class) return randomLocalDateTime();
    if (type == Long.class || type == long.class) return randomLong();
    if (type == Integer.class || type == int.class) return randomInt();
    if (type == Short.class || type == short.class) return randomShort();
    if (type == BigDecimal.class) return randomBigDecimal(0, -1);
    if (type == Double.class || type == double.class) return randomDouble();
    if (type == Float.class || type == float.class) return randomFloat();
    if (type.isEnum()) return randomEnum(type);
    return null; // exotic/unknown type — caller must set this field manually
  }

  /** Return a random {@code long} in [1, 100_000). */
  protected long randomLong() {
    return ThreadLocalRandom.current().nextLong(1, 100_000L);
  }

  /** Return a random {@code int} in [1, 1_000). */
  protected int randomInt() {
    return ThreadLocalRandom.current().nextInt(1, 1_000);
  }

  /** Return a random {@code short} in [1, 100). */
  protected short randomShort() {
    return (short) ThreadLocalRandom.current().nextInt(1, 100);
  }

  /** Return a random {@code boolean} — defaults to {@code true}. */
  protected boolean randomBoolean() {
    return true;
  }

  /** Return a random {@link UUID}. */
  protected UUID randomUUID() {
    return UUID.randomUUID();
  }

  /** Return a random {@link Instant} — defaults to now. */
  protected Instant randomInstant() {
    return Instant.now();
  }

  /** Return a random {@link OffsetDateTime} — defaults to now. */
  protected OffsetDateTime randomOffsetDateTime() {
    return OffsetDateTime.now();
  }

  /** Return a random {@link ZonedDateTime} — defaults to now. */
  protected ZonedDateTime randomZonedDateTime() {
    return ZonedDateTime.now();
  }

  /** Return a random {@link LocalDate} — defaults to today. */
  protected LocalDate randomLocalDate() {
    return LocalDate.now();
  }

  /** Return a random {@link LocalDateTime} — defaults to now. */
  protected LocalDateTime randomLocalDateTime() {
    return LocalDateTime.now();
  }

  /** Return a random {@code double} in [1, 100). */
  protected double randomDouble() {
    return ThreadLocalRandom.current().nextDouble(1, 100);
  }

  /** Return a random {@code float} in [1, 100). */
  protected float randomFloat() {
    return (float) ThreadLocalRandom.current().nextDouble(1, 100);
  }

  /**
   * Return a value for the given enum type — defaults to the first declared constant.
   * Returns {@code null} if the enum has no constants.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  protected Object randomEnum(Class<?> type) {
    Object[] constants = type.getEnumConstants();
    return constants.length > 0 ? constants[0] : null;
  }

  /**
   * Generate a random string, optionally capped at {@code maxLength}.
   * <p>
   * When the property name contains "email" (case-insensitive), a value of the
   * form {@code <prefix>@domain.com} is returned, truncated to fit {@code maxLength}.
   * Otherwise a UUID-derived string is returned, truncated to {@code maxLength}
   * (defaulting to 8 characters when {@code maxLength} is 0 or negative).
   * </p>
   */
  protected String randomString(String propName, int maxLength) {
    String base = UUID.randomUUID().toString().replace("-", ""); // 32 chars
    if (propName != null && propName.toLowerCase().contains("email")) {
      String email = base.substring(0, 8) + "@domain.com";
      if (maxLength > 0 && email.length() > maxLength) {
        int localLen = maxLength - "@domain.com".length();
        email = (localLen > 0 ? base.substring(0, localLen) : base.substring(0, 1)) + "@domain.com";
      }
      return email;
    }
    if (maxLength <= 0) {
      return base.substring(0, 8);
    }
    int len = Math.min(maxLength, base.length());
    return base.substring(0, len);
  }

  /**
   * Generate a random {@link BigDecimal} that fits within the given precision and scale.
   * <p>
   * {@code precision} is the total number of significant digits ({@code dbLength});
   * {@code scale} is the number of decimal places ({@code dbScale}).
   * When precision is 0 or negative (unknown), a default of 6 integer digits is used.
   * When scale is negative (unknown), a default scale of 2 is used.
   * </p>
   */
  protected BigDecimal randomBigDecimal(int precision, int scale) {
    int actualScale = scale >= 0 ? scale : 2;
    int intDigits = precision > 0 ? Math.max(1, precision - actualScale) : 6;
    long maxInt = (long) Math.pow(10, intDigits) - 1;
    long intPart = ThreadLocalRandom.current().nextLong(1, maxInt + 1);
    if (actualScale == 0) {
      return BigDecimal.valueOf(intPart);
    }
    long scaleFactor = (long) Math.pow(10, actualScale);
    long fracPart = ThreadLocalRandom.current().nextLong(0, scaleFactor);
    double value = intPart + (double) fracPart / scaleFactor;
    return BigDecimal.valueOf(value).setScale(actualScale, RoundingMode.HALF_UP);
  }
}

package io.ebean.test;

import io.ebeaninternal.server.deploy.BeanProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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
   * </p>
   */
  public Object generate(BeanProperty prop) {
    Class<?> type = prop.type();
    if (type == String.class) {
      return randomString(prop.dbLength());
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
    if (type == String.class) return randomString(0);
    if (type == Long.class || type == long.class) return ThreadLocalRandom.current().nextLong(1, 100_000L);
    if (type == Integer.class || type == int.class) return ThreadLocalRandom.current().nextInt(1, 1_000);
    if (type == Short.class || type == short.class) return (short) ThreadLocalRandom.current().nextInt(1, 100);
    if (type == Boolean.class || type == boolean.class) return Boolean.TRUE;
    if (type == UUID.class) return UUID.randomUUID();
    if (type == Instant.class) return Instant.now();
    if (type == OffsetDateTime.class) return OffsetDateTime.now();
    if (type == LocalDate.class) return LocalDate.now();
    if (type == LocalDateTime.class) return LocalDateTime.now();
    if (type == BigDecimal.class) return BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(1, 100));
    if (type == Double.class || type == double.class) return ThreadLocalRandom.current().nextDouble(1, 100);
    if (type == Float.class || type == float.class) return (float) ThreadLocalRandom.current().nextDouble(1, 100);
    if (type.isEnum()) {
      Object[] constants = type.getEnumConstants();
      return constants.length > 0 ? constants[0] : null;
    }
    return null; // exotic/unknown type — caller must set this field manually
  }

  /**
   * Generate a random string, optionally capped at {@code maxLength}.
   * <p>
   * When {@code maxLength} is 0 or negative (unknown / unlimited), an 8-character
   * UUID-derived string is returned. Otherwise the value is a UUID-derived string
   * truncated to {@code maxLength} characters.
   * </p>
   */
  private String randomString(int maxLength) {
    String base = UUID.randomUUID().toString().replace("-", ""); // 32 chars
    if (maxLength <= 0) {
      return base.substring(0, 8);
    }
    int len = Math.min(maxLength, base.length());
    return base.substring(0, len);
  }
}

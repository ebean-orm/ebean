package org.tests.dtomapping;

import java.util.UUID;

/**
 * Deliberately <b>not</b> registered via {@code @DtoConverters} - only ever referenced by an
 * explicit per-property {@code @DtoConvert} (requirement r21's precedence rule: an explicit
 * {@code @DtoConvert} always wins over a registered package-level type-pair default, and can
 * point at any converter method, not just a registered one).
 */
public final class UuidShortCodeConverter {

  private UuidShortCodeConverter() {
  }

  public static String toShortCode(UUID uuid) {
    return uuid == null ? null : uuid.toString().substring(0, 8);
  }
}

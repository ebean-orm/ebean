package org.tests.dtomapping;

import java.util.UUID;

/**
 * Registered package-wide via {@code @DtoConverters} (requirement r21, "Section E: Type-pair
 * (package-level) custom scalar conversion" in docs/dto-mapping-requirements.md) - {@link
 * #toHex(UUID)} is auto-dispatched to any {@code UUID -> String} property with no per-property
 * {@code @DtoConvert} of its own, mirroring MapStruct's type-signature auto-matching.
 */
public final class UuidConverters {

  private UuidConverters() {
  }

  /** Auto-dispatched default for any un-annotated {@code UUID -> String} property. */
  public static String toHex(UUID uuid) {
    return uuid == null ? null : uuid.toString().replace("-", "");
  }
}

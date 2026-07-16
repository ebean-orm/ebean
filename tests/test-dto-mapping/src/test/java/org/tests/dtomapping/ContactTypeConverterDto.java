package org.tests.dtomapping;

import io.ebean.annotation.DtoConvert;
import io.ebean.annotation.DtoPath;

/**
 * Exercises {@code @DtoConverters} package-level type-pair auto-dispatch (requirement r21,
 * "Section E: Type-pair (package-level) custom scalar conversion" in
 * docs/dto-mapping-requirements.md) - {@code UUID -> String} is registered once (see
 * {@code package-info.java}, {@link UuidConverters}) and auto-applied to any matching property
 * with no per-property {@code @DtoConvert} needed.
 */
public class ContactTypeConverterDto {

  private final long id;

  /** Plain same-name property - {@code Contact.referenceCode} (a {@code UUID}) auto-converted via {@link UuidConverters#toHex}. */
  private final String referenceCode;

  /** Renamed via {@code @DtoPath} from the same field - proves auto-dispatch also applies on the renamed/multi-hop resolution path. */
  @DtoPath("referenceCode")
  private final String referenceCodeRenamed;

  /** Explicit {@code @DtoConvert} on the same {@code UUID -> String} field - proves it overrides the registered package default. */
  @DtoPath("referenceCode")
  @DtoConvert(value = UuidShortCodeConverter.class, method = "toShortCode")
  private final String referenceCodeShort;

  public ContactTypeConverterDto(long id, String referenceCode, String referenceCodeRenamed, String referenceCodeShort) {
    this.id = id;
    this.referenceCode = referenceCode;
    this.referenceCodeRenamed = referenceCodeRenamed;
    this.referenceCodeShort = referenceCodeShort;
  }

  public long getId() {
    return id;
  }

  public String getReferenceCode() {
    return referenceCode;
  }

  public String getReferenceCodeRenamed() {
    return referenceCodeRenamed;
  }

  public String getReferenceCodeShort() {
    return referenceCodeShort;
  }
}

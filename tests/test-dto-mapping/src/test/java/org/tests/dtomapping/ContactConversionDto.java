package org.tests.dtomapping;

import io.ebean.annotation.DtoConvert;
import io.ebean.annotation.DtoPath;

/**
 * Standalone (not nested under {@link ContactDto}) DTO exercising {@code @DtoConvert} - both the
 * static-dispatch (requirement r13, {@link #active}) and instance-dispatch (requirement r14,
 * {@link #secretCode}) cases - see {@code docs/dto-mapping-requirements.md} "Section E: Custom
 * property conversion".
 */
public class ContactConversionDto {

  private final long id;
  private final String firstName;

  /** {@code Contact.status} (a raw {@code Short}) converted via the static {@link ContactConversions#toActive}. */
  @DtoPath("status")
  @DtoConvert(value = ContactConversions.class, method = "toActive")
  private final boolean active;

  @DtoConvert(value = SecretCipher.class, method = "decode")
  private final String secretCode;

  public ContactConversionDto(long id, String firstName, boolean active, String secretCode) {
    this.id = id;
    this.firstName = firstName;
    this.active = active;
    this.secretCode = secretCode;
  }

  public long getId() {
    return id;
  }

  public String getFirstName() {
    return firstName;
  }

  public boolean isActive() {
    return active;
  }

  public String getSecretCode() {
    return secretCode;
  }
}

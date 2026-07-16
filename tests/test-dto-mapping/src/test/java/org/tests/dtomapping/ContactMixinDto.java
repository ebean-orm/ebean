package org.tests.dtomapping;

/**
 * Stand-in for a DTO that can't be annotated directly (e.g. generated from an OpenAPI spec,
 * regenerated on every build) - carries no {@code @DtoPath}/{@code @DtoRef}/{@code @DtoConvert}
 * annotations of its own at all. {@link ContactMixinDtoMixin} overlays them instead (requirement
 * "Section E: Custom property conversion" / {@code @DtoMixin} in
 * docs/dto-mapping-requirements.md).
 */
public class ContactMixinDto {

  private final long id;
  private final String firstName;
  private final boolean active;
  private final String secretCode;
  private final CustomerRefDto owner;

  public ContactMixinDto(long id, String firstName, boolean active, String secretCode, CustomerRefDto owner) {
    this.id = id;
    this.firstName = firstName;
    this.active = active;
    this.secretCode = secretCode;
    this.owner = owner;
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

  public CustomerRefDto getOwner() {
    return owner;
  }
}

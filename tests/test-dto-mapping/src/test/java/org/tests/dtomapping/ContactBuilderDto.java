package org.tests.dtomapping;

import io.ebean.annotation.DtoRef;

/**
 * Builder-constructed DTO for requirement r18 - forced via {@code @DtoMapping(builder = ALWAYS)}
 * on the {@code package-info.java} declaration. Follows the {@code avaje-recordbuilder}
 * convention by hand (no dependency on that library needed just to test detection): a static
 * no-arg {@code builder()} factory method, a fluent (returns-itself) same-named setter per
 * property, and a no-arg {@code build()} method returning the target type.
 */
public class ContactBuilderDto {

  private final Long id;
  private final String firstName;
  private final String lastName;
  private final Short status;
  private final String secretCode;

  @DtoRef
  private final Long customerId;

  private ContactBuilderDto(Builder builder) {
    this.id = builder.id;
    this.firstName = builder.firstName;
    this.lastName = builder.lastName;
    this.status = builder.status;
    this.secretCode = builder.secretCode;
    this.customerId = builder.customerId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Long getId() {
    return id;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public Short getStatus() {
    return status;
  }

  public String getSecretCode() {
    return secretCode;
  }

  public Long getCustomerId() {
    return customerId;
  }

  public static final class Builder {

    private Long id;
    private String firstName;
    private String lastName;
    private Short status;
    private String secretCode;
    private Long customerId;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder firstName(String firstName) {
      this.firstName = firstName;
      return this;
    }

    public Builder lastName(String lastName) {
      this.lastName = lastName;
      return this;
    }

    public Builder status(Short status) {
      this.status = status;
      return this;
    }

    public Builder secretCode(String secretCode) {
      this.secretCode = secretCode;
      return this;
    }

    public Builder customerId(Long customerId) {
      this.customerId = customerId;
      return this;
    }

    public ContactBuilderDto build() {
      return new ContactBuilderDto(this);
    }
  }
}

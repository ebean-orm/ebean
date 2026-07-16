package org.tests.dtomapping;

import io.ebean.annotation.DtoIgnore;

import java.util.List;

/**
 * Builder-constructed DTO exercising {@code @DtoIgnore} - {@code externalRef} and
 * {@code auditNotes} have no corresponding getter on {@link org.tests.dtomapping.model.Contact}
 * at all (unlike a plain excluded/{@code @DtoConvert}-backed property, which still has some
 * source expression - these have none), modelling a property only ever populated later by
 * application code from an entirely different source (e.g. a separate ad-hoc query or service
 * call), as {@code Fleet.assignedMachines}/{@code assignedDrivers} do in central-access.
 */
public class ContactIgnoreDto {

  private final Long id;
  private final String firstName;
  private final String lastName;

  @DtoIgnore
  private final String externalRef;

  @DtoIgnore
  private final List<String> auditNotes;

  private ContactIgnoreDto(Builder builder) {
    this.id = builder.id;
    this.firstName = builder.firstName;
    this.lastName = builder.lastName;
    this.externalRef = builder.externalRef;
    this.auditNotes = builder.auditNotes;
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

  public String getExternalRef() {
    return externalRef;
  }

  public List<String> getAuditNotes() {
    return auditNotes;
  }

  public static final class Builder {

    private Long id;
    private String firstName;
    private String lastName;
    private String externalRef;
    private List<String> auditNotes;

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

    public Builder externalRef(String externalRef) {
      this.externalRef = externalRef;
      return this;
    }

    public Builder auditNotes(List<String> auditNotes) {
      this.auditNotes = auditNotes;
      return this;
    }

    public ContactIgnoreDto build() {
      return new ContactIgnoreDto(this);
    }
  }
}

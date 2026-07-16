package org.tests.dtomapping;

import io.ebean.annotation.DtoIgnore;

import java.util.List;

/**
 * Plain mutable JavaBean-shaped DTO (public no-arg constructor + public setters, mirroring
 * JAXB/XSD-generated legacy SOAP types) exercising setter-based construction (requirement r22,
 * "Section G: Setter-based (mutable JavaBean) target construction" in
 * docs/dto-mapping-requirements.md) - detected automatically ({@code setter = AUTO}, the default)
 * since this type has no positional constructor matching its properties, only the no-arg one.
 * <p>
 * {@code externalRef}/{@code auditNotes} are {@code @DtoIgnore} - unlike the builder-based
 * {@link ContactIgnoreDto}, no {@code mapToBuilder(...)}-style accessor is needed to populate them
 * afterwards: the mapped target is already fully mutable, so a caller just calls its public
 * setters directly, e.g. {@code dto.setExternalRef(loadExternalRef(...))} - see
 * {@code TestDtoSetterConstruction}.
 */
public class ContactSetterDto {

  private long id;
  private String firstName;
  private String lastName;

  @DtoIgnore
  private String externalRef;

  @DtoIgnore
  private List<String> auditNotes;

  public ContactSetterDto() {
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getExternalRef() {
    return externalRef;
  }

  public void setExternalRef(String externalRef) {
    this.externalRef = externalRef;
  }

  public List<String> getAuditNotes() {
    return auditNotes;
  }

  public void setAuditNotes(List<String> auditNotes) {
    this.auditNotes = auditNotes;
  }
}

package org.tests.dtomapping;

/**
 * Mutable JavaBean-shaped DTO exercising setter-based construction (requirement r22) with
 * <b>fluent-style</b> setters - each returns {@code this} rather than {@code void}, e.g.
 * {@code public ContactSetterFluentDto setFirstName(String firstName) { this.firstName =
 * firstName; return this; }}. Setter detection accepts either shape (see
 * {@code DtoMappingReader.findSetter}) since the generated code always calls the setter as a bare
 * statement and discards any return value - see {@code TestDtoSetterConstruction}.
 */
public class ContactSetterFluentDto {

  private long id;
  private String firstName;
  private String lastName;

  public ContactSetterFluentDto() {
  }

  public long getId() {
    return id;
  }

  public ContactSetterFluentDto setId(long id) {
    this.id = id;
    return this;
  }

  public String getFirstName() {
    return firstName;
  }

  public ContactSetterFluentDto setFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public String getLastName() {
    return lastName;
  }

  public ContactSetterFluentDto setLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }
}

package org.tests.dtomapping;

/**
 * Minimal "leaf" DTO for {@link org.tests.dtomapping.model.Contact} - deliberately has no further
 * nested relations of its own (unlike {@link ContactDto}, which also maps {@code customer}), so
 * {@link ComputedNestedDto}'s bare {@code fetch("contacts")} (everything {@code Contact} itself
 * owns, no deeper paths) is sufficient to satisfy it without needing to reason about propagating a
 * nested mapper's own fetch requirements up through a computed getter - see {@link ComputedNestedDto}.
 * <p>
 * {@code @DtoMapping(source = Contact.class, target = ContactLeafDto.class)} is declared on
 * {@code package-info.java}.
 */
public class ContactLeafDto {

  private final Long id;
  private final String firstName;
  private final String lastName;

  public ContactLeafDto(Long id, String firstName, String lastName) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
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
}

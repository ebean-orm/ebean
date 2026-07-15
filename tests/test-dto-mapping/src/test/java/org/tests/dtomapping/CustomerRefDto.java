package org.tests.dtomapping;

/**
 * Shallow reference DTO for {@code Customer} - deliberately does <b>not</b> include a
 * {@code contacts} field (or any other back-reference), so that {@link ContactDto#getCustomer()}
 * can point back to its owning customer without re-introducing the {@code Customer -> Contact ->
 * Customer} cycle. This is the hand-written analogue of the {@code @DtoRef} escape hatch
 * described in docs/dto-mapping-design.md, just modelled as a distinct shallow DTO type here
 * rather than an id-only field.
 */
public class CustomerRefDto {

  private final Long id;
  private final String name;

  public CustomerRefDto(Long id, String name) {
    this.id = id;
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}

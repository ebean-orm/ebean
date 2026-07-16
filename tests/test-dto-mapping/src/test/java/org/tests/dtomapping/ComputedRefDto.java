package org.tests.dtomapping;

import io.ebean.annotation.DtoRef;

/**
 * Regression coverage for {@code @DtoRef} traversing a computed/derived association getter (no
 * backing field) - a variant of {@link ComputedPathDto} that exercises {@code @DtoRef}'s
 * {@code Kind.REF} branch rather than {@code @DtoPath}'s {@code SCALAR}/{@code NESTED_ONE}
 * branches.
 * <p>
 * {@code primaryContactId} derives its association name ("primaryContact") from the field name,
 * which resolves to {@code Customer#getPrimaryContact()} - a computed getter with no backing
 * field (see {@link org.tests.dtomapping.model.Customer}). Without the fix, {@code @DtoRef} never
 * checked whether the association had a backing field at all, so this compiled cleanly and
 * generated a broken {@code FetchGroup.select("primaryContact")} call - {@code "primaryContact"}
 * isn't a real Ebean property, so that would fail at runtime with {@code PersistenceException: No
 * property found}.
 * <p>
 * {@code @DtoMapping(source = Customer.class, target = ComputedRefDto.class)} is declared on
 * {@code package-info.java}.
 */
public class ComputedRefDto {

  private final Long id;

  @DtoRef(requires = "contacts")
  private final Long primaryContactId;

  public ComputedRefDto(Long id, Long primaryContactId) {
    this.id = id;
    this.primaryContactId = primaryContactId;
  }

  public Long getId() {
    return id;
  }

  public Long getPrimaryContactId() {
    return primaryContactId;
  }
}

package org.tests.dtomapping;

import io.ebean.annotation.DtoPath;

/**
 * Regression coverage for a single-hop {@code @DtoPath} rename traversing a computed/derived
 * getter whose return type matches a <b>registered nested DTO mapping</b> - a variant of
 * {@link ComputedPathDto} that exercises {@code DtoMapperWriter}'s {@code NESTED_ONE}/
 * {@code NESTED_MANY} branch rather than its {@code SCALAR} branch.
 * <p>
 * {@code primaryContact} traverses {@code Customer#getPrimaryContact()} (no backing field - see
 * {@link org.tests.dtomapping.model.Customer}), but its return type ({@code Contact}) has its own
 * registered {@code @DtoMapping} to {@link ContactLeafDto} - so the field type here is
 * {@code ContactLeafDto}, not a plain scalar. Without the fix, this single-hop case bypassed the
 * computed-segment {@code requires()} validation entirely (the {@code NESTED_ONE} lookup returned
 * early before it ran) and would have generated a broken {@code fetch("primaryContact",
 * contactLeafMapper.fetchGroup())} call - {@code "primaryContact"} isn't a real Ebean fetch path,
 * so that would fail at runtime with {@code PersistenceException: No property found}.
 * <p>
 * {@code @DtoMapping(source = Customer.class, target = ComputedNestedDto.class)} is declared on
 * {@code package-info.java}.
 */
public class ComputedNestedDto {

  private final Long id;

  @DtoPath(value = "primaryContact", requires = "contacts")
  private final ContactLeafDto primaryContact;

  public ComputedNestedDto(Long id, ContactLeafDto primaryContact) {
    this.id = id;
    this.primaryContact = primaryContact;
  }

  public Long getId() {
    return id;
  }

  public ContactLeafDto getPrimaryContact() {
    return primaryContact;
  }
}

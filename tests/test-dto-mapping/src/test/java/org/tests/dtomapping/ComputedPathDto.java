package org.tests.dtomapping;

import io.ebean.annotation.DtoPath;

/**
 * Regression coverage for {@code @DtoPath#requires()} - {@code primaryContactLastName} traverses
 * {@code Customer#getPrimaryContact()}, a computed/derived getter with no backing field (it picks
 * the first entry out of the {@code contacts} collection). Since the generator can't infer that
 * this getter's own data dependency is {@code contacts}, {@code requires = "contacts"} declares it
 * explicitly so the generated {@code FetchGroup} includes a bare {@code fetch("contacts")} call -
 * without it, {@code getPrimaryContact()} would run against an unfetched/lazy collection and either
 * fail or trigger an extra lazy-load query at map time.
 * <p>
 * See {@link org.tests.dtomapping.TestComputedPath} and {@code
 * DtoMapperComputedPathTest} (querybean-generator module) for the companion negative case - the
 * same shape of {@code @DtoPath} but with {@code requires} omitted, which must be a compile-time
 * error rather than a silent runtime failure.
 * <p>
 * {@code @DtoMapping(source = Customer.class, target = ComputedPathDto.class)} is declared on
 * {@code package-info.java}.
 */
public class ComputedPathDto {

  private final Long id;

  @DtoPath(value = "primaryContact.lastName", requires = "contacts")
  private final String primaryContactLastName;

  public ComputedPathDto(Long id, String primaryContactLastName) {
    this.id = id;
    this.primaryContactLastName = primaryContactLastName;
  }

  public Long getId() {
    return id;
  }

  public String getPrimaryContactLastName() {
    return primaryContactLastName;
  }
}

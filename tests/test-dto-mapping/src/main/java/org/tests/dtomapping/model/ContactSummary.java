package org.tests.dtomapping.model;

import io.ebean.annotation.Formula2;
import io.ebean.annotation.View;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Worked example: a read-only "view" entity mapped onto the *same* underlying table as
 * {@link Contact} (via {@code @View(name = "contact")}) rather than a real database view -
 * this deliberately generates no DDL of its own, it just reads {@link Contact}'s table
 * through a second, purpose-built entity shape.
 * <p>
 * This is the pattern proposed in docs/dto-mapping-design.md's "Ad-hoc computed/formula
 * properties" section as the preferred way to get a Blaze-Persistence-style computed DTO
 * property: model the computed value as a plain {@code @Formula2} on a dedicated read
 * entity, then map that entity to a DTO with the existing (unmodified) {@code @DtoMapping}
 * machinery - no new ad-hoc-SQL-on-DTO annotation required.
 */
@Entity
@View(name = "contact")
public class ContactSummary {

  @Id
  private Long id;

  private String firstName;
  private String lastName;

  /**
   * Computed purely from {@link #firstName} and {@link #lastName} - no join required, but
   * demonstrates the same {@code @Formula2} mechanism used for join-requiring formulas
   * elsewhere (see {@code AggStockProduct}/{@code ParentPerson} in ebean-test).
   */
  @Formula2("concat(firstName, ' ', lastName)")
  private String fullName;

  public Long getId() {
    return id;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getFullName() {
    return fullName;
  }
}

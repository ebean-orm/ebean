package org.tests.dtomapping.model;

import io.ebean.Model;
import io.ebean.annotation.Aggregation;
import io.ebean.annotation.Sum;
import io.ebean.annotation.View;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Per-customer contact aggregates, read through the same {@code contact} table as {@link Contact}
 * (rather than a real database view or a new table - {@code @View(name = "contact")} just points
 * this entity's base table at the existing one, so no new DDL is generated).
 * <p>
 * This is the worked example for Blaze-Persistence-style aggregate/group-by mappings (see
 * docs/dto-mapping-design.md, "Aggregate/group-by computed properties"): {@code contactCount} and
 * {@code engagementScore} are group-by aggregate formulas ({@code @Aggregation}/{@code @Sum}),
 * grouped implicitly by whichever non-aggregate properties end up selected - here {@code customer}.
 * The Blaze-Persistence parallel is an {@code @EntityView} with {@code @Mapping("SIZE(contacts)")}
 * / {@code @Mapping("SUM(contacts.engagementScore)")} correlated mappings.
 * <p>
 * {@code id} is declared (required so {@code @Aggregation("count(id)")} has a property to count)
 * but deliberately never selected/mapped - selecting it would defeat the aggregation by grouping
 * one row per contact instead of one row per customer.
 */
@Entity
@View(name = "contact")
public class ContactStats extends Model {

  @Id
  private Long id;

  @ManyToOne
  private Customer customer;

  @Aggregation("count(id)")
  private Long contactCount;

  @Sum
  private Integer engagementScore;

  public Customer getCustomer() {
    return customer;
  }

  public Long getContactCount() {
    return contactCount;
  }

  public Integer getEngagementScore() {
    return engagementScore;
  }
}

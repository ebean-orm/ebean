package org.tests.dtomapping;

import io.ebean.annotation.DtoRef;

/**
 * Flat DTO for {@link org.tests.dtomapping.model.ContactStats} - a group-by aggregation result
 * (contact count + summed engagement score per customer), not a graph fetch.
 * <p>
 * {@code customerId} uses {@code @DtoRef} - the generated fetch spec adds {@code customer} to the
 * root {@code select(...)}, which reads the FK column directly off the base table (no join) and
 * is exactly the property this aggregation query is grouped by. This doubles as the grouping key.
 */
public class ContactStatsDto {

  @DtoRef
  private final Long customerId;

  private final Long contactCount;
  private final Integer engagementScore;

  public ContactStatsDto(Long customerId, Long contactCount, Integer engagementScore) {
    this.customerId = customerId;
    this.contactCount = contactCount;
    this.engagementScore = engagementScore;
  }

  public Long getCustomerId() {
    return customerId;
  }

  public Long getContactCount() {
    return contactCount;
  }

  public Integer getEngagementScore() {
    return engagementScore;
  }
}

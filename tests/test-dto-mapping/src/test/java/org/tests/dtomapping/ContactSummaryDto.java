package org.tests.dtomapping;

/**
 * Plain DTO with no framework attachment - mapping target for {@code ContactSummary}, the
 * worked example of a {@code @Formula2}-backed computed property flowing into a DTO via a
 * dedicated {@code @View}-mapped read entity rather than a bespoke ad-hoc-SQL-on-DTO feature.
 */
public class ContactSummaryDto {

  private final Long id;
  private final String fullName;

  public ContactSummaryDto(Long id, String fullName) {
    this.id = id;
    this.fullName = fullName;
  }

  public Long getId() {
    return id;
  }

  public String getFullName() {
    return fullName;
  }
}

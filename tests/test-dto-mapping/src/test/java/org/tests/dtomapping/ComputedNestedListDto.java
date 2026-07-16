package org.tests.dtomapping;

import io.ebean.annotation.DtoPath;

import java.util.List;

/**
 * Regression coverage for a single-hop {@code @DtoPath} rename traversing a computed/derived
 * getter whose return type is a {@code List} matching a <b>registered nested DTO mapping</b> -
 * the {@code NESTED_MANY} counterpart to {@link ComputedNestedDto}'s {@code NESTED_ONE} case.
 * <p>
 * {@code recentContacts} traverses {@code Customer#getRecentContacts()} (no backing field - see
 * {@link org.tests.dtomapping.model.Customer}), returning a {@code List<Contact>} - its element
 * type has its own registered {@code @DtoMapping} to {@link ContactLeafDto}, so the field type
 * here is {@code List<ContactLeafDto>}. Shares the exact same codegen path as
 * {@link ComputedNestedDto} ({@code DtoMapperWriter}'s {@code NESTED_ONE}/{@code NESTED_MANY}
 * branch both route computed segments to {@code extraFetchPaths} identically) - this is coverage
 * confirming the {@code NESTED_MANY} variant actually works end-to-end, not a bug fix.
 * <p>
 * {@code @DtoMapping(source = Customer.class, target = ComputedNestedListDto.class)} is declared
 * on {@code package-info.java}.
 */
public class ComputedNestedListDto {

  private final Long id;

  @DtoPath(value = "recentContacts", requires = "contacts")
  private final List<ContactLeafDto> recentContacts;

  public ComputedNestedListDto(Long id, List<ContactLeafDto> recentContacts) {
    this.id = id;
    this.recentContacts = recentContacts;
  }

  public Long getId() {
    return id;
  }

  public List<ContactLeafDto> getRecentContacts() {
    return recentContacts;
  }
}

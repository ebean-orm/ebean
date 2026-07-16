package org.tests.dtomapping;

import io.ebean.annotation.DtoPath;

/**
 * Regression coverage for {@code @DtoPath(requires = {})} - {@code idBadge} traverses {@code
 * Customer#getIdBadge()}, a computed/derived getter with no backing field, but one that genuinely
 * needs nothing extra fetched (it derives purely from {@code id}, which is always fetched as a
 * matter of course). The explicit empty array confirms that to the generator, as opposed to
 * omitting {@code requires} entirely (a compile error - see {@link ComputedPathDto} for the case
 * that does need a real fetch path, and {@code DtoMapperComputedPathTest} in the
 * querybean-generator module for the negative/omitted case).
 * <p>
 * {@code @DtoMapping(source = Customer.class, target = ComputedPathNoFetchDto.class)} is declared
 * on {@code package-info.java}.
 */
public class ComputedPathNoFetchDto {

  private final Long id;

  @DtoPath(value = "idBadge", requires = {})
  private final String idBadge;

  public ComputedPathNoFetchDto(Long id, String idBadge) {
    this.id = id;
    this.idBadge = idBadge;
  }

  public Long getId() {
    return id;
  }

  public String getIdBadge() {
    return idBadge;
  }
}

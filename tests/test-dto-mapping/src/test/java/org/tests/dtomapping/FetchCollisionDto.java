package org.tests.dtomapping;

import io.ebean.annotation.DtoPath;

/**
 * Regression coverage for the priority between a bare, full {@code requires()} fetch and a
 * sibling property's narrowed {@code @DtoPath} fetch of the exact same path - see
 * {@link org.tests.dtomapping.model.Customer#getBillingSummary()}.
 * <p>
 * {@code billingCity} narrows the {@code billingAddress} fetch down to just {@code city} (a plain
 * {@code @DtoPath("billingAddress.city")}). {@code billingSummary} traverses the computed
 * {@code getBillingSummary()} getter (no backing field), declaring {@code requires =
 * "billingAddress"} - a bare, full fetch of the exact same path. {@code FetchGroup}'s builder
 * replaces (not merges) same-path fetch calls, so without prioritizing the full fetch over the
 * narrow one, {@code line1} (needed internally by {@code getBillingSummary()}, but not by
 * {@code billingCity}) would silently never be loaded - calling {@code getBillingSummary()} would
 * then throw {@code LazyInitialisationException} rather than return a value.
 * <p>
 * {@code @DtoMapping(source = Customer.class, target = FetchCollisionDto.class)} is declared on
 * {@code package-info.java}.
 */
public class FetchCollisionDto {

  private final Long id;

  @DtoPath("billingAddress.city")
  private final String billingCity;

  @DtoPath(value = "billingSummary", requires = "billingAddress")
  private final String billingSummary;

  public FetchCollisionDto(Long id, String billingCity, String billingSummary) {
    this.id = id;
    this.billingCity = billingCity;
    this.billingSummary = billingSummary;
  }

  public Long getId() {
    return id;
  }

  public String getBillingCity() {
    return billingCity;
  }

  public String getBillingSummary() {
    return billingSummary;
  }
}

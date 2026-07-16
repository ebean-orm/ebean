package org.tests.dtomapping;

import io.ebean.annotation.DtoPath;

/**
 * Regression coverage for a primitive-typed DTO field derived via a multi-hop {@code @DtoPath}
 * through a <b>nullable</b> ToOne relation ({@code Customer#getBillingAddress()} can be
 * {@code null}) - the generated null-guarded getter chain (e.g. {@code (x == null ? null :
 * x.getId())}) types as boxed {@code Long}, which would auto-unbox to a {@code
 * NullPointerException} when passed to a primitive constructor parameter if not handled.
 * <p>
 * {@code billingAddressId} exercises the default behaviour (silently defaults to {@code 0}) - see
 * {@link org.tests.dtomapping.PrimitiveNullPathFailOnNullDto} for the {@code failOnNull = true}
 * counterpart, and {@link org.tests.dtomapping.TestPrimitiveNullPath} for both cases.
 * <p>
 * {@code @DtoMapping(source = Contact.class, target = PrimitiveNullPathDto.class)} is declared
 * on {@code package-info.java}.
 */
public class PrimitiveNullPathDto {

  @DtoPath("customer.billingAddress.id")
  private final long billingAddressId;

  public PrimitiveNullPathDto(long billingAddressId) {
    this.billingAddressId = billingAddressId;
  }

  public long getBillingAddressId() {
    return billingAddressId;
  }
}

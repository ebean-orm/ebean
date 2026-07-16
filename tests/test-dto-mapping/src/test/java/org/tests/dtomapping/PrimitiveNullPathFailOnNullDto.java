package org.tests.dtomapping;

import io.ebean.annotation.DtoPath;

/**
 * Companion to {@link PrimitiveNullPathDto} isolating {@code @DtoPath(failOnNull = true)} in its
 * own DTO type - kept separate so a null intermediate hop's exception doesn't also abort
 * construction of {@link PrimitiveNullPathDto}'s default (silently-zero) field in the same
 * constructor call. See {@link org.tests.dtomapping.TestPrimitiveNullPath}.
 * <p>
 * {@code @DtoMapping(source = Contact.class, target = PrimitiveNullPathFailOnNullDto.class)} is
 * declared on {@code package-info.java}.
 */
public class PrimitiveNullPathFailOnNullDto {

  @DtoPath(value = "customer.billingAddress.id", failOnNull = true)
  private final long billingAddressId;

  public PrimitiveNullPathFailOnNullDto(long billingAddressId) {
    this.billingAddressId = billingAddressId;
  }

  public long getBillingAddressId() {
    return billingAddressId;
  }
}

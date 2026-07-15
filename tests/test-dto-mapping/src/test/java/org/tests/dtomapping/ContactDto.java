package org.tests.dtomapping;

import io.ebean.annotation.DtoPath;
import io.ebean.annotation.DtoRef;

public class ContactDto {

  private final long id;
  private final String firstName;
  private final String lastName;
  /** Direct scalar mapping of a primitive {@code boolean} source field - see {@link org.tests.dtomapping.model.Contact#isActive()}. */
  private final boolean active;
  private final CustomerRefDto customer;

  /** {@code @DtoRef} id-only shortcut - {@code s.getCustomer().getId()}, no nested mapper. */
  @DtoRef
  private final Long customerId;

  /**
   * {@code @DtoPath} multi-hop getter chain - {@code s.getCustomer().getBillingAddress().getCity()},
   * null-guarded at each hop. Deliberately a 2-hop path through {@code customer} (rather than
   * repeating the {@code billingAddress.line1} example already used elsewhere) so its fetch path
   * ({@code customer.billingAddress}) doesn't collide with the plain {@code customer} nested-DTO
   * fetch on this same class.
   */
  @DtoPath("customer.billingAddress.city")
  private final String customerCity;

  public ContactDto(long id, String firstName, String lastName, boolean active, CustomerRefDto customer, Long customerId, String customerCity) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.active = active;
    this.customer = customer;
    this.customerId = customerId;
    this.customerCity = customerCity;
  }

  public long getId() {
    return id;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public boolean isActive() {
    return active;
  }

  public CustomerRefDto getCustomer() {
    return customer;
  }

  public Long getCustomerId() {
    return customerId;
  }

  public String getCustomerCity() {
    return customerCity;
  }
}

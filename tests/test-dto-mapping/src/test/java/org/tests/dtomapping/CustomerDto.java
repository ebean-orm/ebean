package org.tests.dtomapping;

import java.util.List;

/**
 * Plain DTO with no framework attachment - target of the nested DTO graph mapping spike for
 * issue #2540. Nests {@link AddressDto} for the {@code billingAddress} ToOne relationship and
 * {@link ContactDto} for the {@code contacts} ToMany relationship.
 */
public class CustomerDto {

  private final Long id;
  private final String name;
  private final AddressDto billingAddress;
  private final List<ContactDto> contacts;

  public CustomerDto(Long id, String name, AddressDto billingAddress, List<ContactDto> contacts) {
    this.id = id;
    this.name = name;
    this.billingAddress = billingAddress;
    this.contacts = contacts;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public AddressDto getBillingAddress() {
    return billingAddress;
  }

  public List<ContactDto> getContacts() {
    return contacts;
  }
}

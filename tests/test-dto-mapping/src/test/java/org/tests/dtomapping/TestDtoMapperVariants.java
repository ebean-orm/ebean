package org.tests.dtomapping;

import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.dtomapping.model.Address;
import org.tests.dtomapping.model.Contact;
import org.tests.dtomapping.model.Customer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates named mapper variants (requirement r19, "Section G" in
 * docs/dto-mapping-requirements.md) - {@code CustomerDto} is registered twice: the base mapping,
 * plus {@code @DtoMapping(name = "noContacts", exclude = "contacts")}. Both share the very same
 * generated {@code CustomerDtoMapper} class; the variant is exposed as its {@code noContacts()}
 * accessor and selected via the {@code query.mapTo(Class, DtoMapper)} overload (requirement
 * r19's API addition) rather than a string-based lookup.
 */
class TestDtoMapperVariants {

  private final CustomerDtoMapper mapper = new CustomerDtoMapper();

  @Test
  void mapTo_withNoContactsVariant_expectContactsExcludedButAddressPopulated() {
    Address address = new Address("12 Test Street", "Auckland");
    address.save();
    Customer customer = new Customer("Acme");
    customer.setBillingAddress(address);
    customer.save();
    new Contact("Jane", "Doe", customer).save();

    CustomerDto dto = DB.find(Customer.class)
      .where().idEq(customer.getId())
      .mapTo(CustomerDto.class, mapper.noContacts())
      .findOne();

    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(customer.getId());
    assertThat(dto.getName()).isEqualTo("Acme");
    // billingAddress is NOT excluded by this variant - still populated
    assertThat(dto.getBillingAddress()).isNotNull();
    assertThat(dto.getBillingAddress().getCity()).isEqualTo("Auckland");
    // contacts IS excluded by this variant - empty, not null, and no fetch/join issued for it
    assertThat(dto.getContacts()).isEmpty();
  }

  @Test
  void mapTo_baseMapping_expectContactsStillPopulated() {
    Customer customer = new Customer("Acme");
    customer.save();
    new Contact("Jane", "Doe", customer).save();
    new Contact("John", "Doe", customer).save();

    CustomerDto dto = DB.find(Customer.class)
      .where().idEq(customer.getId())
      .mapTo(CustomerDto.class)
      .findOne();

    assertThat(dto.getContacts()).hasSize(2);
  }

  @Test
  void noContacts_expectOwnFetchGroupOmitsContactsFetch() {
    assertThat(mapper.noContacts().fetchGroup()).isNotNull();
    assertThat(mapper.fetchGroup()).isNotSameAs(mapper.noContacts().fetchGroup());
  }

  @Test
  void noContacts_whenSourceNull_expectNullDto() {
    assertThat(mapper.noContacts().map(null)).isNull();
  }
}

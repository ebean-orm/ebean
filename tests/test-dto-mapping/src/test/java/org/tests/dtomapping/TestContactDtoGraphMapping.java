package org.tests.dtomapping;

import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.dtomapping.model.Address;
import org.tests.dtomapping.model.Contact;
import org.tests.dtomapping.model.Customer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates the generated {@code ContactDtoMapper} standalone, and specifically the identity
 * de-duplication behaviour of {@code io.ebean.DtoMapContext} (requirements r1/r3): several
 * {@link Contact}s sharing the same parent {@link Customer} must map their {@code customer}
 * back-reference to the exact same {@link CustomerRefDto} instance rather than each producing an
 * equal-but-distinct copy.
 * <p>
 * {@link CustomerRefDto} exists (rather than reusing {@link CustomerDto}) so this back-reference
 * doesn't reintroduce the {@code Customer -> Contact -> Customer} cycle.
 */
class TestContactDtoGraphMapping {

  private final ContactDtoMapper mapper = new ContactDtoMapper();

  @Test
  void mapContacts_sharingSameCustomer_expectIdenticalCustomerRefInstance() {
    Address address = new Address("1 High Street", "Wellington");
    address.save();

    Customer customer = new Customer("Acme");
    customer.setBillingAddress(address);
    customer.save();
    new Contact("Jane", "Doe", customer).save();
    new Contact("John", "Doe", customer).save();

    Customer found = DB.find(Customer.class)
      .setUnmodifiable(true)
      .select("id,name")
      .fetch("contacts", "id,firstName,lastName,active,customer")
      .fetch("contacts.customer", "id,name")
      .fetch("contacts.customer.billingAddress", "id,city")
      .where().idEq(customer.getId())
      .findOne();

    List<Contact> contacts = found.getContacts();
    assertThat(contacts.size()).isGreaterThan(1);

    List<ContactDto> dtos = mapper.mapList(contacts);

    assertThat(dtos).hasSameSizeAs(contacts);
    for (int i = 0; i < dtos.size(); i++) {
      assertThat(dtos.get(i).getFirstName()).isEqualTo(contacts.get(i).getFirstName());
      // direct boolean scalar mapping - source's isActive() (not a guessed getActive())
      assertThat(dtos.get(i).isActive()).isTrue();
      assertThat(dtos.get(i).getCustomer().getId()).isEqualTo(customer.getId());
      assertThat(dtos.get(i).getCustomer().getName()).isEqualTo(customer.getName());
      // @DtoRef (id-only) and @DtoPath (multi-hop, through the customer association) properties
      assertThat(dtos.get(i).getCustomerId()).isEqualTo(customer.getId());
      assertThat(dtos.get(i).getCustomerCity()).isEqualTo("Wellington");
    }

    // identity de-dup: every contact's customer back-reference is the exact same DTO instance
    CustomerRefDto first = dtos.get(0).getCustomer();
    for (ContactDto dto : dtos) {
      assertThat(dto.getCustomer()).isSameAs(first);
    }
  }

  @Test
  void mapContact_whenSourceNull_expectNullDto() {
    assertThat(mapper.map(null)).isNull();
  }
}

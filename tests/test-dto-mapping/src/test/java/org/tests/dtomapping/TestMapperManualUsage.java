package org.tests.dtomapping;

import io.ebean.DtoMapContext;
import org.junit.jupiter.api.Test;
import org.tests.dtomapping.model.Address;
import org.tests.dtomapping.model.Contact;
import org.tests.dtomapping.model.Customer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every generated {@code XxxDtoMapper} is a plain public class - a public no-arg constructor
 * (or an explicit constructor taking its nested-mapper/converter dependencies) and a public
 * {@code map(...)}/{@code mapList(...)} - so it can be called directly, without going through
 * {@code query.mapTo()} at all. This is useful both as a normal API (map an entity graph you
 * already have on hand, from wherever it came from) and for fast, isolated unit tests of the
 * mapping logic itself - construct plain entities, call the mapper, assert on the DTO, with no
 * query execution and no {@code DtoMapperManager}/{@code ServiceLoader} registry involved.
 * <p>
 * See also {@link TestCustomerDtoGraphMapping}, which does the same thing against a manually run
 * (non-{@code mapTo}) query.
 */
class TestMapperManualUsage {

  @Test
  void map_directlyOnEntitiesJustCreated_withoutQueryOrMapTo() {
    Address address = new Address("1 Queen Street", "Auckland");
    address.save();
    Customer customer = new Customer("Acme");
    customer.setBillingAddress(address);
    customer.save();
    Contact contact = new Contact("Jane", "Doe", customer);
    contact.save();
    customer.getContacts().add(contact);

    // the very same in-memory objects just saved - no DB.find()/mapTo() query at all
    CustomerDto dto = new CustomerDtoMapper().map(customer);

    assertThat(dto.getName()).isEqualTo("Acme");
    assertThat(dto.getBillingAddress().getCity()).isEqualTo("Auckland");
    assertThat(dto.getContacts()).hasSize(1);
    assertThat(dto.getContacts().get(0).getFirstName()).isEqualTo("Jane");
  }

  @Test
  void mapList_sharedContext_identityDedupWithoutMapTo() {
    Customer customer = new Customer("Acme");
    customer.save();
    Contact jane = new Contact("Jane", "Doe", customer);
    jane.save();
    Contact john = new Contact("John", "Doe", customer);
    john.save();

    DtoMapContext context = new DtoMapContext();
    List<ContactDto> dtos = new ContactDtoMapper().mapList(List.of(jane, john), context);

    // both contacts share the same Customer instance - mapped via the same shared context - so
    // the nested CustomerRefDto instance is de-duplicated (same reference, not just equal)
    assertThat(dtos.get(0).getCustomer()).isSameAs(dtos.get(1).getCustomer());
  }

  @Test
  void map_withTestDoubleConverter_bypassingDtoConverterManager() {
    // the explicit constructor lets a test pass its own SecretCipher directly - no need to
    // register anything with DtoConverterManager for this kind of isolated unit test
    SecretCipher upperCasingTestCipher = String::toUpperCase;
    Customer customer = new Customer("Acme");
    customer.save();
    Contact contact = new Contact("Jane", "Doe", customer);
    contact.setStatus((short) 1);
    contact.setSecretCode("shh");
    contact.save();

    ContactConversionDto dto = new ContactConversionDtoMapper(upperCasingTestCipher).map(contact);

    assertThat(dto.isActive()).isTrue();
    assertThat(dto.getSecretCode()).isEqualTo("SHH");
  }
}

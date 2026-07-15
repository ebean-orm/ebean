package org.tests.dtomapping;

import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.dtomapping.model.Contact;
import org.tests.dtomapping.model.Customer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates builder-based target construction (requirement r18, "Section G" in
 * docs/dto-mapping-requirements.md) - {@link ContactBuilderDto} is registered with
 * {@code @DtoMapping(builder = ALWAYS)}, forcing the generated {@code ContactBuilderDtoMapper} to
 * construct the target via {@code ContactBuilderDto.builder()....build()} rather than a
 * positional constructor.
 */
class TestDtoBuilderConstruction {

  private final ContactBuilderDtoMapper mapper = new ContactBuilderDtoMapper();

  @Test
  void mapTo_expectBuilderConstructedDto() {
    Customer customer = new Customer("Acme");
    customer.save();
    Contact contact = new Contact("Jane", "Doe", customer);
    contact.setStatus((short) 1);
    contact.setSecretCode("shh");
    contact.save();

    ContactBuilderDto dto = DB.find(Contact.class)
      .where().idEq(contact.getId())
      .mapTo(ContactBuilderDto.class)
      .findOne();

    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(contact.getId());
    assertThat(dto.getFirstName()).isEqualTo("Jane");
    assertThat(dto.getLastName()).isEqualTo("Doe");
    assertThat(dto.getStatus()).isEqualTo((short) 1);
    assertThat(dto.getSecretCode()).isEqualTo("shh");
    assertThat(dto.getCustomerId()).isEqualTo(customer.getId());
  }

  @Test
  void map_whenSourceNull_expectNullDto() {
    assertThat(mapper.map(null)).isNull();
  }
}

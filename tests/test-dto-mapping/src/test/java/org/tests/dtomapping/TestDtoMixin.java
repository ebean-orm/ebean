package org.tests.dtomapping;

import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.dtomapping.model.Contact;
import org.tests.dtomapping.model.Customer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates {@code @DtoMixin} - overlaying {@code @DtoPath}/{@code @DtoConvert} annotations from
 * {@link ContactMixinDtoMixin} onto {@link ContactMixinDto}, which carries no annotations of its
 * own at all (a stand-in for a DTO generated elsewhere, e.g. from an OpenAPI spec).
 */
class TestDtoMixin {

  @Test
  void mapTo_expectMixinAnnotationsApplied() {
    Customer customer = new Customer("Acme");
    customer.save();
    Contact contact = new Contact("Jane", "Doe", customer);
    contact.setStatus((short) 1);
    contact.setSecretCode("terces");
    contact.save();

    ContactMixinDto dto = DB.find(Contact.class)
      .where().idEq(contact.getId())
      .mapTo(ContactMixinDto.class)
      .findOne();

    assertThat(dto).isNotNull();
    assertThat(dto.getFirstName()).isEqualTo("Jane");
    // @DtoPath("status") + static @DtoConvert, both declared on the mixin method
    assertThat(dto.isActive()).isTrue();
    // instance @DtoConvert declared on the mixin method, resolved via DtoConverterManager
    assertThat(dto.getSecretCode()).isEqualTo("secret");
  }

  @Test
  void mapTo_whenStatusZero_expectInactive() {
    Customer customer = new Customer("Beta");
    customer.save();
    Contact contact = new Contact("John", "Smith", customer);
    contact.setStatus((short) 0);
    contact.setSecretCode("abc");
    contact.save();

    ContactMixinDto dto = DB.find(Contact.class)
      .where().idEq(contact.getId())
      .mapTo(ContactMixinDto.class)
      .findOne();

    assertThat(dto.isActive()).isFalse();
  }
}

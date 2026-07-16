package org.tests.dtomapping;

import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.dtomapping.model.Contact;
import org.tests.dtomapping.model.Customer;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates {@code @DtoConverters} package-level type-pair auto-dispatch (requirement r21,
 * "Section E: Type-pair (package-level) custom scalar conversion" in
 * docs/dto-mapping-requirements.md) - {@link UuidConverters#toHex} is registered once for the
 * whole {@code org.tests.dtomapping} package (see {@code package-info.java}) and auto-applied to
 * any {@code UUID -> String} property with no per-property {@code @DtoConvert}, while an explicit
 * {@code @DtoConvert} on the same field still takes precedence.
 */
class TestDtoConverters {

  @Test
  void mapTo_expectAutoDispatchedTypeConverterApplied() {
    Customer customer = new Customer("Acme");
    customer.save();
    Contact contact = new Contact("Jane", "Doe", customer);
    UUID referenceCode = UUID.randomUUID();
    contact.setReferenceCode(referenceCode);
    contact.save();

    ContactTypeConverterDto dto = DB.find(Contact.class)
      .where().idEq(contact.getId())
      .mapTo(ContactTypeConverterDto.class)
      .findOne();

    assertThat(dto).isNotNull();
    // auto-dispatched, no @DtoConvert on the property itself
    assertThat(dto.getReferenceCode()).isEqualTo(UuidConverters.toHex(referenceCode));
    // auto-dispatched via the @DtoPath-renamed resolution path
    assertThat(dto.getReferenceCodeRenamed()).isEqualTo(UuidConverters.toHex(referenceCode));
    // explicit @DtoConvert overrides the registered package default
    assertThat(dto.getReferenceCodeShort()).isEqualTo(UuidShortCodeConverter.toShortCode(referenceCode));
  }
}

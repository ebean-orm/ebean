package org.tests.dtomapping;

import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.dtomapping.model.Contact;
import org.tests.dtomapping.model.Customer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates setter-based (mutable JavaBean) target construction (requirement r22, "Section G:
 * Setter-based (mutable JavaBean) target construction" in docs/dto-mapping-requirements.md) -
 * {@link ContactSetterDto} has only a no-arg constructor plus public setters (mirroring
 * JAXB/XSD-generated legacy SOAP types), so it's auto-detected ({@code setter = AUTO}) and mapped
 * via {@code new ContactSetterDto(); dto.setX(...); ...; return dto;} rather than a positional
 * constructor call or a builder chain.
 */
class TestDtoSetterConstruction {

  @Test
  void mapTo_expectSetterBasedConstruction() {
    Customer customer = new Customer("Acme");
    customer.save();
    Contact contact = new Contact("Jane", "Doe", customer);
    contact.save();

    ContactSetterDto dto = DB.find(Contact.class)
      .where().idEq(contact.getId())
      .mapTo(ContactSetterDto.class)
      .findOne();

    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(contact.getId());
    assertThat(dto.getFirstName()).isEqualTo("Jane");
    assertThat(dto.getLastName()).isEqualTo("Doe");
    // @DtoIgnore - not populated by the mapper itself (List defaults to List.of(), not null)
    assertThat(dto.getExternalRef()).isNull();
    assertThat(dto.getAuditNotes()).isEmpty();

    // the mapped target is already fully mutable - no special generated accessor is needed to
    // populate an @DtoIgnore property afterwards, unlike the builder strategy's mapToBuilder(...)
    dto.setExternalRef("EXT-123");
    dto.setAuditNotes(List.of("created via import"));

    assertThat(dto.getExternalRef()).isEqualTo("EXT-123");
    assertThat(dto.getAuditNotes()).containsExactly("created via import");
  }

  @Test
  void mapTo_expectFluentSetterBasedConstruction() {
    Customer customer = new Customer("Acme");
    customer.save();
    Contact contact = new Contact("Jane", "Doe", customer);
    contact.save();

    // ContactSetterFluentDto's setters return `this` (fluent-style) rather than void - detection
    // accepts either shape, and the generated code calls the setter as a bare statement either way
    ContactSetterFluentDto dto = DB.find(Contact.class)
      .where().idEq(contact.getId())
      .mapTo(ContactSetterFluentDto.class)
      .findOne();

    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(contact.getId());
    assertThat(dto.getFirstName()).isEqualTo("Jane");
    assertThat(dto.getLastName()).isEqualTo("Doe");
  }
}

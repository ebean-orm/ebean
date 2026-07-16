package org.tests.dtomapping;

import io.ebean.DB;
import io.ebean.DtoMapContext;
import org.junit.jupiter.api.Test;
import org.tests.dtomapping.model.Contact;
import org.tests.dtomapping.model.Customer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates {@code @DtoIgnore} - a property permanently excluded from every mapping (base and
 * every named variant alike), always given its empty default rather than resolved from any
 * source getter/path, plus the generated {@code mapToBuilder(source)} accessor (emitted whenever
 * the target uses builder construction) that lets a caller set its real value - sourced from
 * elsewhere entirely, outside this mapper's own fetch graph - before finishing construction.
 */
class TestDtoIgnore {

  private final ContactIgnoreDtoMapper mapper = new ContactIgnoreDtoMapper();

  @Test
  void mapTo_expectIgnoredPropertiesDefaulted() {
    Customer customer = new Customer("Acme");
    customer.save();
    Contact contact = new Contact("Jane", "Doe", customer);
    contact.save();

    ContactIgnoreDto dto = DB.find(Contact.class)
      .where().idEq(contact.getId())
      .mapTo(ContactIgnoreDto.class)
      .findOne();

    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(contact.getId());
    assertThat(dto.getFirstName()).isEqualTo("Jane");
    assertThat(dto.getLastName()).isEqualTo("Doe");
    // @DtoIgnore properties always default, never resolved from Contact
    assertThat(dto.getExternalRef()).isNull();
    assertThat(dto.getAuditNotes()).isEmpty();
  }

  @Test
  void mapToBuilder_expectIgnoredPropertiesSettableBeforeBuild() {
    Customer customer = new Customer("Acme");
    customer.save();
    Contact contact = new Contact("Jane", "Doe", customer);
    contact.save();

    ContactIgnoreDto dto = mapper.mapToBuilder(contact)
      .externalRef("EXT-123")
      .auditNotes(List.of("created", "reviewed"))
      .build();

    assertThat(dto.getId()).isEqualTo(contact.getId());
    assertThat(dto.getFirstName()).isEqualTo("Jane");
    assertThat(dto.getExternalRef()).isEqualTo("EXT-123");
    assertThat(dto.getAuditNotes()).containsExactly("created", "reviewed");
  }

  @Test
  void mapToBuilder_withContext_expectSameBehaviour() {
    Customer customer = new Customer("Acme");
    customer.save();
    Contact contact = new Contact("Jane", "Doe", customer);
    contact.save();

    ContactIgnoreDto dto = mapper.mapToBuilder(contact, new DtoMapContext())
      .externalRef("EXT-456")
      .build();

    assertThat(dto.getExternalRef()).isEqualTo("EXT-456");
    assertThat(dto.getAuditNotes()).isEmpty();
  }

  @Test
  void mapToBuilder_whenSourceNull_expectNullBuilder() {
    assertThat(mapper.mapToBuilder(null)).isNull();
  }

  @Test
  void map_whenSourceNull_expectNullDto() {
    assertThat(mapper.map(null)).isNull();
  }
}

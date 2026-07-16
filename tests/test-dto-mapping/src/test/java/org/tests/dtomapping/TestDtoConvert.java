package org.tests.dtomapping;

import io.ebean.DB;
import io.ebean.DtoConverterManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.tests.dtomapping.model.Contact;
import org.tests.dtomapping.model.Customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Validates {@code @DtoConvert} (requirements r13/r14, "Section E: Custom property conversion"
 * in docs/dto-mapping-requirements.md) - both static dispatch (no registration,
 * {@link ContactConversions#toActive}) and instance dispatch (resolved via
 * {@link DtoConverterManager}, {@link SecretCipher#decode}).
 * <p>
 * {@link SecretCipher} is registered by {@link ContactConversionDbConfigProvider} (a
 * {@code DatabaseConfigProvider}, discovered via ServiceLoader before the {@code Database} is
 * built) rather than in a test {@code @BeforeAll} - {@code EbeanDtoMapperRegister}'s mapper
 * fields (including {@code ContactConversionDtoMapper}'s {@code SecretCipher} field) are all
 * constructed eagerly during {@code Database} startup, which can be triggered by whichever test
 * class in this module happens to run first.
 */
class TestDtoConvert {

  @Test
  void mapTo_expectStaticAndInstanceConversionsApplied() {
    Customer customer = new Customer("Acme");
    customer.save();
    Contact contact = new Contact("Jane", "Doe", customer);
    contact.setStatus((short) 1);
    contact.setSecretCode("terces");
    contact.save();

    ContactConversionDto dto = DB.find(Contact.class)
      .where().idEq(contact.getId())
      .mapTo(ContactConversionDto.class)
      .findOne();

    assertThat(dto).isNotNull();
    assertThat(dto.getFirstName()).isEqualTo("Jane");
    // static dispatch - Short(1) -> true, no registration needed
    assertThat(dto.isActive()).isTrue();
    // instance dispatch - resolved via DtoConverterManager, ReverseSecretCipher reverses the string
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

    ContactConversionDto dto = DB.find(Contact.class)
      .where().idEq(contact.getId())
      .mapTo(ContactConversionDto.class)
      .findOne();

    assertThat(dto.isActive()).isFalse();
  }

  @Test
  void get_whenTypeNotRegistered_expectPersistenceException() {
    assertThatThrownBy(() -> DtoConverterManager.get(UnregisteredConverter.class))
      .isInstanceOf(PersistenceException.class)
      .hasMessageContaining("No " + UnregisteredConverter.class.getName() + " registered")
      .hasMessageContaining("DtoConverterManager.put(UnregisteredConverter.class, ...)");
  }

  /** Deliberately never registered with {@link DtoConverterManager} - used to prove {@link DtoConverterManager#get} fails fast. */
  private interface UnregisteredConverter {
  }
}

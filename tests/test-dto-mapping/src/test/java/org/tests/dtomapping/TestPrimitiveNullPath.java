package org.tests.dtomapping;

import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.dtomapping.model.Contact;
import org.tests.dtomapping.model.Customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Regression test for {@link PrimitiveNullPathDto} - see its javadoc.
 */
class TestPrimitiveNullPath {

  @Test
  void mapTo_whenNullableRelationHopIsNull_defaultsPrimitiveToZero() {
    Customer customer = new Customer("NoBillingAddressCo");
    // deliberately leave billingAddress null
    customer.save();
    Contact contact = new Contact("Jane", "Doe", customer);
    contact.save();

    PrimitiveNullPathDto dto = DB.find(Contact.class)
      .where().idEq(contact.getId())
      .mapTo(PrimitiveNullPathDto.class)
      .findOne();

    assertThat(dto).isNotNull();
    assertThat(dto.getBillingAddressId()).isZero();
  }

  @Test
  void mapTo_whenNullableRelationHopIsNull_andFailOnNull_throws() {
    Customer customer = new Customer("NoBillingAddressCo2");
    customer.save();
    Contact contact = new Contact("John", "Doe", customer);
    contact.save();

    assertThatThrownBy(() -> DB.find(Contact.class)
      .where().idEq(contact.getId())
      .mapTo(PrimitiveNullPathFailOnNullDto.class)
      .findOne())
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("customer.billingAddress.id");
  }
}

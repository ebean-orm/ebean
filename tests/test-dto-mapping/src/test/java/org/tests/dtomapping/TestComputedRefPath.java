package org.tests.dtomapping;

import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.dtomapping.model.Contact;
import org.tests.dtomapping.model.Customer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test for {@link ComputedRefDto} - see its javadoc.
 */
class TestComputedRefPath {

  @Test
  void mapTo_whenDtoRefTraversesComputedGetter_requiresFetchesItsDependency() {
    Customer customer = new Customer("ComputedRefCo");
    customer.save();
    Contact contact = new Contact("Alice", "Smith", customer);
    contact.save();

    ComputedRefDto dto = DB.find(Customer.class)
      .where().idEq(customer.getId())
      .mapTo(ComputedRefDto.class)
      .findOne();

    assertThat(dto).isNotNull();
    assertThat(dto.getPrimaryContactId()).isEqualTo(contact.getId());
  }
}

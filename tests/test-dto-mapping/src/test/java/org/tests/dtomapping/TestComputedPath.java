package org.tests.dtomapping;

import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.dtomapping.model.Contact;
import org.tests.dtomapping.model.Customer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test for {@link ComputedPathDto} (the {@code @DtoPath#requires()} happy path) - see
 * its javadoc, and {@code DtoMapperComputedPathTest} (querybean-generator module) for the
 * companion negative/compile-error case.
 */
class TestComputedPath {

  @Test
  void mapTo_whenPathTraversesComputedGetter_requiresFetchesItsDependency() {
    Customer customer = new Customer("ComputedPathCo");
    customer.save();
    Contact contact = new Contact("Alice", "Smith", customer);
    contact.save();

    ComputedPathDto dto = DB.find(Customer.class)
      .where().idEq(customer.getId())
      .mapTo(ComputedPathDto.class)
      .findOne();

    assertThat(dto).isNotNull();
    assertThat(dto.getPrimaryContactLastName()).isEqualTo("Smith");
  }
}

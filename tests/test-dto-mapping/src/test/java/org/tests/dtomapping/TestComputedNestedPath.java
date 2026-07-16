package org.tests.dtomapping;

import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.dtomapping.model.Contact;
import org.tests.dtomapping.model.Customer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test for {@link ComputedNestedDto} - see its javadoc.
 */
class TestComputedNestedPath {

  @Test
  void mapTo_whenComputedGetterTargetsNestedDto_requiresFetchesItsDependency() {
    Customer customer = new Customer("ComputedNestedCo");
    customer.save();
    Contact contact = new Contact("Bob", "Jones", customer);
    contact.save();

    ComputedNestedDto dto = DB.find(Customer.class)
      .where().idEq(customer.getId())
      .mapTo(ComputedNestedDto.class)
      .findOne();

    assertThat(dto).isNotNull();
    assertThat(dto.getPrimaryContact()).isNotNull();
    assertThat(dto.getPrimaryContact().getFirstName()).isEqualTo("Bob");
    assertThat(dto.getPrimaryContact().getLastName()).isEqualTo("Jones");
  }
}

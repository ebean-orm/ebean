package org.tests.dtomapping;

import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.dtomapping.model.Contact;
import org.tests.dtomapping.model.Customer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test for {@link ComputedNestedListDto} - see its javadoc.
 */
class TestComputedNestedListPath {

  @Test
  void mapTo_whenComputedGetterReturnsListTargetingNestedDto_requiresFetchesItsDependency() {
    Customer customer = new Customer("ComputedNestedListCo");
    customer.save();
    Contact alice = new Contact("Alice", "Smith", customer);
    alice.save();
    Contact bob = new Contact("Bob", "Jones", customer);
    bob.save();

    ComputedNestedListDto dto = DB.find(Customer.class)
      .where().idEq(customer.getId())
      .mapTo(ComputedNestedListDto.class)
      .findOne();

    assertThat(dto).isNotNull();
    assertThat(dto.getRecentContacts()).hasSize(2);
    assertThat(dto.getRecentContacts().get(0).getFirstName()).isEqualTo("Alice");
    assertThat(dto.getRecentContacts().get(1).getFirstName()).isEqualTo("Bob");
  }
}

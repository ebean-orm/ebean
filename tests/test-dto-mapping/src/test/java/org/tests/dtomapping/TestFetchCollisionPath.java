package org.tests.dtomapping;

import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.dtomapping.model.Address;
import org.tests.dtomapping.model.Customer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test for {@link FetchCollisionDto} - see its javadoc. Proves the fix for the
 * priority between a bare, full {@code requires()} fetch and a sibling property's narrowed
 * {@code @DtoPath} fetch of the exact same path: without it, this test fails with
 * {@code LazyInitialisationException} ("line1" unfetched) rather than the assertions below.
 */
class TestFetchCollisionPath {

  @Test
  void mapTo_whenBareRequiresPathCollidesWithNarrowedSiblingFetch_fullFetchWins() {
    Address address = new Address("221B Baker Street", "London");
    address.save();
    Customer customer = new Customer("FetchCollisionCo");
    customer.setBillingAddress(address);
    customer.save();

    FetchCollisionDto dto = DB.find(Customer.class)
      .where().idEq(customer.getId())
      .mapTo(FetchCollisionDto.class)
      .findOne();

    assertThat(dto).isNotNull();
    assertThat(dto.getBillingCity()).isEqualTo("London");
    // only satisfiable if billingAddress was fully (not narrowly) fetched - line1 isn't part of
    // the sibling billingCity property's own narrowed fetch("billingAddress", "city")
    assertThat(dto.getBillingSummary()).isEqualTo("221B Baker Street, London");
  }
}

package org.tests.dtomapping;

import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.dtomapping.model.Customer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test for {@link ComputedPathNoFetchDto} (the {@code @DtoPath(requires = {})} explicit
 * empty case) - see its javadoc.
 */
class TestComputedPathNoFetch {

  @Test
  void mapTo_whenComputedGetterNeedsNoExtraFetch_explicitEmptyRequiresWorks() {
    Customer customer = new Customer("NoExtraFetchCo");
    customer.save();

    ComputedPathNoFetchDto dto = DB.find(Customer.class)
      .where().idEq(customer.getId())
      .mapTo(ComputedPathNoFetchDto.class)
      .findOne();

    assertThat(dto).isNotNull();
    assertThat(dto.getIdBadge()).isEqualTo("CUST-" + customer.getId());
  }
}

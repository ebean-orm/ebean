package org.tests.query;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TestFindOneOrEmpty extends BaseTestCase {

  @Test
  public void empty() {

    Optional<Customer> willBeEmpty = DB.find(Customer.class)
      .setId(Integer.MAX_VALUE)
      .findOneOrEmpty();

    assertThat(willBeEmpty).isEmpty();
  }

  @Test
  public void notEmpty() {

    ResetBasicData.reset();

    Optional<Customer> customer = DB.find(Customer.class)
      .setId(1)
      .findOneOrEmpty();

    assertThat(customer.isPresent()).isTrue();

    customer.ifPresent(customer1 -> {
      String name = customer1.getName();
      assertThat(name).isNotEmpty();
    });

  }
}

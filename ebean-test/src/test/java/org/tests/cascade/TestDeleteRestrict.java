package org.tests.cascade;

import io.ebean.DataIntegrityException;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Roland Praml, Foconis Analytics GmbH
 */
public class TestDeleteRestrict extends BaseTestCase {

  @Test
  void test() {
    ResetBasicData.reset();

    Customer customer = new Customer();
    customer.setName("Roland");
    server().save(customer);

    Order order = new Order();
    order.setCustomer(customer);
    server().save(order);

    assertThat(customer.getVersion()).isEqualTo(1L);
    assertThatThrownBy(() -> server().delete(customer)).isInstanceOf(DataIntegrityException.class);
    assertThat(customer.getVersion()).isEqualTo(1L);

    customer.setName("Roland-inactive");
    server().save(customer);

    // cleanup
    server().delete(order);
    server().delete(customer);
  }
}

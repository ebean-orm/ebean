package io.ebean.plugin;

import io.ebean.DB;
import io.ebean.Database;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;

import static org.assertj.core.api.Assertions.assertThat;

/**
 */
public class PropertyTest {

  static Database server = DB.getDefault();

  <T> BeanType<T> beanType(Class<T> cls) {
    return server.pluginApi().beanType(cls);
  }

  @Test
  public void getVal() throws Exception {

    Customer customer = new Customer();

    Order order = new Order();
    order.setCustomer(customer);
    order.setStatus(Order.Status.APPROVED);

    Property statusProperty = beanType(Order.class).property("status");
    assertThat(statusProperty.value(order)).isEqualTo(order.getStatus());

    Property customerProperty = beanType(Order.class).property("customer");
    assertThat(customerProperty.value(order)).isEqualTo(customer);
  }

  @Test
  public void isMany_when_not() {

    assertThat(beanType(Order.class).property("status").isMany()).isFalse();
    assertThat(beanType(Order.class).property("customer").isMany()).isFalse();
  }

  @Test
  public void isMany_when_true() {

    assertThat(beanType(Order.class).property("details").isMany()).isTrue();
  }

  @Test
  public void name() {
    assertThat(beanType(Order.class).property("status").name()).isEqualTo("status");
    assertThat(beanType(Order.class).property("customer").name()).isEqualTo("customer");
    assertThat(beanType(Order.class).property("details").name()).isEqualTo("details");
  }
}

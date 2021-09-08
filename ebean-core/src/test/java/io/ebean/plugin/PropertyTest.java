package io.ebean.plugin;

import io.ebean.DB;
import io.ebean.Database;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.junit.jupiter.api.Test;

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
    assertThat(statusProperty.getVal(order)).isEqualTo(order.getStatus());

    Property customerProperty = beanType(Order.class).property("customer");
    assertThat(customerProperty.getVal(order)).isEqualTo(customer);
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
    assertThat(beanType(Order.class).property("status").getName()).isEqualTo("status");
    assertThat(beanType(Order.class).property("customer").getName()).isEqualTo("customer");
    assertThat(beanType(Order.class).property("details").getName()).isEqualTo("details");
  }
}

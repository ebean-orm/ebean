package io.ebean.plugin;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

/**
 */
public class PropertyTest {

  static EbeanServer server = Ebean.getDefaultServer();

  <T> BeanType<T> beanType(Class<T> cls) {
    return server.getPluginApi().getBeanType(cls);
  }

  @Test
  public void getVal() throws Exception {

    Customer customer = new Customer();

    Order order = new Order();
    order.setCustomer(customer);
    order.setStatus(Order.Status.APPROVED);

    Property statusProperty = beanType(Order.class).getProperty("status");
    assertThat(statusProperty.getVal(order)).isEqualTo(order.getStatus());

    Property customerProperty = beanType(Order.class).getProperty("customer");
    assertThat(customerProperty.getVal(order)).isEqualTo(customer);
  }

  @Test
  public void isMany_when_not() {

    assertThat(beanType(Order.class).getProperty("status").isMany()).isFalse();
    assertThat(beanType(Order.class).getProperty("customer").isMany()).isFalse();
  }

  @Test
  public void isMany_when_true() {

    assertThat(beanType(Order.class).getProperty("details").isMany()).isTrue();
  }

  @Test
  public void name() {
    assertThat(beanType(Order.class).getProperty("status").getName()).isEqualTo("status");
    assertThat(beanType(Order.class).getProperty("customer").getName()).isEqualTo("customer");
    assertThat(beanType(Order.class).getProperty("details").getName()).isEqualTo("details");
  }
}

package com.avaje.ebean.plugin;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.tests.model.basic.Order;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class ExpressionPathTest {

  static EbeanServer server = Ebean.getDefaultServer();

  <T> BeanType<T> beanType(Class<T> cls) {
    return server.getPluginApi().getBeanType(cls);
  }

  @Test
  public void containsMany_when_many() throws Exception {

    BeanType<Order> beanType = beanType(Order.class);
    assertThat(beanType.getExpressionPath("details").containsMany()).isTrue();
  }

  @Test
  public void containsMany_when_manyChild() throws Exception {

    BeanType<Order> beanType = beanType(Order.class);
    assertThat(beanType.getExpressionPath("details.id").containsMany()).isTrue();
  }

  @Test
  public void containsMany_when_manyGrandChild() throws Exception {

    BeanType<Order> beanType = beanType(Order.class);
    assertThat(beanType.getExpressionPath("details.product.sku").containsMany()).isTrue();
  }

  @Test
  public void containsMany_when_one() throws Exception {

    BeanType<Order> beanType = beanType(Order.class);
    assertThat(beanType.getExpressionPath("customer.name").containsMany()).isFalse();
  }

  @Test
  public void containsMany_when_oneWithMany() throws Exception {

    BeanType<Order> beanType = beanType(Order.class);
    assertThat(beanType.getExpressionPath("customer.contacts").containsMany()).isTrue();
  }

  @Test
  public void containsMany_when_oneWithManyChild() throws Exception {

    BeanType<Order> beanType = beanType(Order.class);
    assertThat(beanType.getExpressionPath("customer.contacts.firstName").containsMany()).isTrue();
  }

  @Test
  public void set_when_basic() throws Exception {

    BeanType<Order> beanType = beanType(Order.class);
    Order order = new Order();
    beanType.getExpressionPath("id").set(order, 42);
    assertThat(order.getId()).isEqualTo(42);
  }

  @Test
  public void set_when_nested() throws Exception {

    BeanType<Order> beanType = beanType(Order.class);
    Order order = new Order();
    beanType.getExpressionPath("customer.name").set(order, "Rob");
    assertThat(order.getCustomer().getName()).isEqualTo("Rob");
  }

}
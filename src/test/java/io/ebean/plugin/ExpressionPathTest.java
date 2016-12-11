package io.ebean.plugin;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
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
    beanType.getExpressionPath("id").pathSet(order, 42);
    assertThat(order.getId()).isEqualTo(42);
  }

  @Test
  public void set_when_nested() throws Exception {

    BeanType<Order> beanType = beanType(Order.class);
    Order order = new Order();
    beanType.getExpressionPath("customer.name").pathSet(order, "Rob");
    assertThat(order.getCustomer().getName()).isEqualTo("Rob");
  }

  @Test
  public void test_dirty() throws Exception {

    BeanType<Customer> customerBeanType = beanType(Customer.class);
    BeanType<Order> orderBeanType = beanType(Order.class);


    Customer customer = new Customer();
    customer.setName("foo");
    server.save(customer);

    customer = server.find(Customer.class, customer.getId());
    assertThat(customer.getName()).isEqualTo("foo");

    customerBeanType.getExpressionPath("name").pathSet(customer, "bar");
    server.save(customer);

    customer = server.find(Customer.class, customer.getId());
    assertThat(customer.getName()).isEqualTo("bar");


    Order order = new Order();
    order.setCustomer(customer);

    server.save(order);

    order = server.find(Order.class, order.getId());

    ExpressionPath customerNamePath = orderBeanType.getExpressionPath("customer.name");
    assertThat(customerNamePath.pathGet(order)).isEqualTo("bar");

    customerNamePath.pathSet(order, "baz");
    server.save(order);

    order = server.find(Order.class, order.getId());
    assertThat(order.getCustomer().getName()).isEqualTo("baz");
  }

}

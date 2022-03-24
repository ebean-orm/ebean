package io.ebean.xtest.plugin;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.plugin.BeanType;
import io.ebean.plugin.ExpressionPath;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

import static org.assertj.core.api.Assertions.assertThat;


public class ExpressionPathTest {

  static Database server = DB.getDefault();

  <T> BeanType<T> beanType(Class<T> cls) {
    return server.pluginApi().beanType(cls);
  }

  @Test
  public void containsMany_when_many() throws Exception {

    BeanType<Order> beanType = beanType(Order.class);
    assertThat(beanType.expressionPath("details").containsMany()).isTrue();
  }

  @Test
  public void containsMany_when_manyChild() throws Exception {

    BeanType<Order> beanType = beanType(Order.class);
    assertThat(beanType.expressionPath("details.id").containsMany()).isTrue();
  }

  @Test
  public void containsMany_when_manyGrandChild() throws Exception {

    BeanType<Order> beanType = beanType(Order.class);
    assertThat(beanType.expressionPath("details.product.sku").containsMany()).isTrue();
  }

  @Test
  public void containsMany_when_one() throws Exception {

    BeanType<Order> beanType = beanType(Order.class);
    assertThat(beanType.expressionPath("customer.name").containsMany()).isFalse();
  }

  @Test
  public void containsMany_when_oneWithMany() throws Exception {

    BeanType<Order> beanType = beanType(Order.class);
    assertThat(beanType.expressionPath("customer.contacts").containsMany()).isTrue();
  }

  @Test
  public void containsMany_when_oneWithManyChild() throws Exception {

    BeanType<Order> beanType = beanType(Order.class);
    assertThat(beanType.expressionPath("customer.contacts.firstName").containsMany()).isTrue();
  }

  @Test
  public void set_when_basic() throws Exception {

    BeanType<Order> beanType = beanType(Order.class);
    Order order = new Order();
    beanType.expressionPath("id").pathSet(order, 42);
    assertThat(order.getId()).isEqualTo(42);
  }

  @Test
  public void set_when_nested() throws Exception {

    BeanType<Order> beanType = beanType(Order.class);
    Order order = new Order();
    beanType.expressionPath("customer.name").pathSet(order, "Rob");
    assertThat(order.getCustomer().getName()).isEqualTo("Rob");
  }

  @Test
  public void test_dirty() throws Exception {

    ResetBasicData.reset();

    BeanType<Customer> customerBeanType = beanType(Customer.class);
    BeanType<Order> orderBeanType = beanType(Order.class);


    Customer customer = new Customer();
    customer.setName("foo");
    server.save(customer);

    customer = server.find(Customer.class, customer.getId());
    assertThat(customer.getName()).isEqualTo("foo");

    customerBeanType.expressionPath("name").pathSet(customer, "bar");
    server.save(customer);

    customer = server.find(Customer.class, customer.getId());
    assertThat(customer.getName()).isEqualTo("bar");


    Order order = new Order();
    order.setCustomer(customer);

    server.save(order);

    order = server.find(Order.class, order.getId());

    ExpressionPath customerNamePath = orderBeanType.expressionPath("customer.name");
    assertThat(customerNamePath.pathGet(order)).isEqualTo("bar");

    customerNamePath.pathSet(order, "baz");
    server.save(order);

    order = server.find(Order.class, order.getId());
    assertThat(order.getCustomer().getName()).isEqualTo("baz");

    // cleanup
    server.delete(Order.class, order.getId());
    server.delete(Customer.class, customer.getId());
  }

}

package io.ebean.xtest.plugin;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.plugin.BeanType;
import io.ebean.plugin.Property;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


public class BeanTypeTest {

  private static Database db = DB.getDefault();

  private <T> BeanType<T> beanType(Class<T> cls) {
    return db.pluginApi().beanType(cls);
  }

  @Test
  public void getBeanType() {
    assertThat(beanType(Order.class).type()).isEqualTo(Order.class);
  }

  @Test
  public void getTypeAtPath_when_ManyToOne() {
    BeanType<Order> orderType = beanType(Order.class);
    BeanType<?> customerType = orderType.beanTypeAtPath("customer");
    assertThat(customerType.type()).isEqualTo(Customer.class);
  }

  @Test
  public void getTypeAtPath_when_OneToMany() {
    BeanType<Order> orderType = beanType(Order.class);
    BeanType<?> detailsType = orderType.beanTypeAtPath("details");
    assertThat(detailsType.type()).isEqualTo(OrderDetail.class);
  }

  @Test
  public void getTypeAtPath_when_nested() {
    BeanType<Order> orderType = beanType(Order.class);
    BeanType<?> productType = orderType.beanTypeAtPath("details.product");
    assertThat(productType.type()).isEqualTo(Product.class);
  }

  @Test
  public void getTypeAtPath_when_simpleType() {
    assertThrows(RuntimeException.class, () -> beanType(Order.class).beanTypeAtPath("status"));
  }

  @Test
  public void createBean() {
    assertThat(beanType(Order.class).createBean()).isNotNull();
  }

  @Test
  public void property() {

    Order order = new Order();
    order.setStatus(Order.Status.APPROVED);
    Property statusProperty = beanType(Order.class).property("status");

    assertThat(statusProperty.value(order)).isEqualTo(order.getStatus());
  }

  @Test
  public void getBaseTable() {

    assertThat(beanType(Order.class).baseTable()).isEqualTo("o_order");
  }

  @Test
  public void beanId_and_getBeanId() {

    Order order = new Order();
    order.setId(42);

    Object id1 = beanType(Order.class).id(order);
    assertThat(id1).isEqualTo(order.getId());
  }

  @Test
  public void setBeanId() {

    Order order = new Order();
    beanType(Order.class).setId(order, 42);

    assertThat(42).isEqualTo(order.getId());
  }

}

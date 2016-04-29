package com.avaje.ebeaninternal.server.deploy;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.bean.EntityBean;
import com.avaje.ebean.plugin.Property;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import org.junit.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class BeanDescriptorTest extends BaseTestCase {

  BeanDescriptor<Customer> customerDesc = spiEbeanServer().getBeanDescriptor(Customer.class);

  @Test
  public void createReference() {

    Customer bean = customerDesc.createReference(null, 42, null);
    assertThat(bean.getId()).isEqualTo(42);
    assertThat(server().getBeanState(bean).isReadOnly()).isFalse();
  }

  @Test
  public void createReference_whenReadOnly() {

    Customer bean = customerDesc.createReference(Boolean.TRUE, 42, null);
    assertThat(server().getBeanState(bean).isReadOnly()).isTrue();
  }

  @Test
  public void createReference_whenNotReadOnly() {

    Customer bean = customerDesc.createReference(Boolean.FALSE, 42, null);
    assertThat(server().getBeanState(bean).isReadOnly()).isFalse();
  }

  @Test
  public void allProperties() {

    BeanDescriptor<Order> desc = getBeanDescriptor(Order.class);
    Collection<? extends Property> props = desc.allProperties();

    assertThat(props).extracting("name").contains("id", "status", "orderDate", "shipDate");
  }

  @Test
  public void merge_when_empty() {

    Customer from = new Customer();
    from.setId(42);
    from.setName("rob");

    Customer to = new Customer();
    customerDesc.merge((EntityBean)from, (EntityBean)to);

    assertThat(to.getId()).isEqualTo(42);
    assertThat(to.getName()).isEqualTo("rob");
  }

}
package io.ebeaninternal.server.deploy;

import io.ebean.BaseTestCase;
import io.ebean.bean.EntityBean;
import io.ebean.plugin.Property;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class BeanDescriptorTest extends BaseTestCase {

  BeanDescriptor<Customer> customerDesc = spiEbeanServer().getBeanDescriptor(Customer.class);

  @Test
  public void createReference() {

    Customer bean = customerDesc.createReference(null, false, 42, null);
    assertThat(bean.getId()).isEqualTo(42);
    assertThat(server().getBeanState(bean).isReadOnly()).isFalse();
  }

  @Test
  public void createReference_whenReadOnly() {

    Customer bean = customerDesc.createReference(Boolean.TRUE, false, 42, null);
    assertThat(server().getBeanState(bean).isReadOnly()).isTrue();
  }

  @Test
  public void createReference_whenNotReadOnly() {

    Customer bean = customerDesc.createReference(Boolean.FALSE, false, 42, null);
    assertThat(server().getBeanState(bean).isReadOnly()).isFalse();

    bean = customerDesc.createReference(42, null);
    assertThat(server().getBeanState(bean).isReadOnly()).isFalse();
  }

  @Test
  public void createReference_when_disabledLazyLoad() {

    Customer bean = customerDesc.createReference(Boolean.FALSE, true, 42, null);
    assertThat(server().getBeanState(bean).isDisableLazyLoad()).isTrue();
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
    customerDesc.merge((EntityBean) from, (EntityBean) to);

    assertThat(to.getId()).isEqualTo(42);
    assertThat(to.getName()).isEqualTo("rob");
  }

}

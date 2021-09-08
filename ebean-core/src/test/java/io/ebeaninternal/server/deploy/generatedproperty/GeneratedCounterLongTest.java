package io.ebeaninternal.server.deploy.generatedproperty;

import io.ebean.BaseTestCase;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanProperty;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class GeneratedCounterLongTest extends BaseTestCase {

  private GeneratedCounterLong counter = new GeneratedCounterLong();

  private BeanProperty version = getBeanDescriptor(Customer.class).beanProperty("version");

  @Test
  public void when_null_expect_IllegalStateException() {
    Customer customer = new Customer();
    assertThrows(IllegalStateException.class, () -> counter.getUpdateValue(version, (EntityBean)customer, System.currentTimeMillis()));
  }

  @Test
  public void when_set_expect_incremented() {
    Customer customer = new Customer();
    customer.setVersion(7L);
    Object value = counter.getUpdateValue(version, (EntityBean) customer, System.currentTimeMillis());

    assertThat(value).isEqualTo(8L);
  }

}

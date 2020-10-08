package io.ebean.plugin;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import org.tests.model.basic.Customer;
import org.junit.Test;
import org.tests.model.basic.VwCustomer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class SpiServerTest extends BaseTestCase {

  @Test
  public void test() {

    EbeanServer defaultServer = Ebean.getDefaultServer();
    SpiServer pluginApi = defaultServer.getPluginApi();

    BeanType<Customer> beanType = pluginApi.getBeanType(Customer.class);
    assertEquals("o_customer", beanType.getBaseTable());
    assertNotNull(pluginApi.getDatabasePlatform());
    assertNull(beanType.getFindController());
    assertNotNull(beanType.getPersistController());
    assertNull(beanType.getPersistListener());
    assertNull(beanType.getQueryAdapter());

    assertTrue(beanType.isValidExpression("name"));
    assertTrue(beanType.isValidExpression("contacts.firstName"));
    assertTrue(beanType.isValidExpression("contacts.group.name"));
    assertFalse(beanType.isValidExpression("junk"));
    assertFalse(beanType.isValidExpression("Name"));
    assertFalse(beanType.isValidExpression("contacts.name"));

    Customer customer = new Customer();
    customer.setId(42);

    assertEquals(42, beanType.getBeanId(customer));

    List<? extends BeanType<?>> beanTypes = pluginApi.getBeanTypes("o_customer");
    assertEquals(2, beanTypes.size());

    BeanType<VwCustomer> vwBeanType = pluginApi.getBeanType(VwCustomer.class);

    assertThat(beanTypes.contains(beanType)).isTrue();
    assertThat(beanTypes.contains(vwBeanType)).isTrue();

    List<? extends BeanType<?>> allTypes = pluginApi.getBeanTypes();
    assertFalse(allTypes.isEmpty());
  }

}
